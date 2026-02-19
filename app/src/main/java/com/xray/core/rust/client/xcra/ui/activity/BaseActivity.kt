package com.xray.core.rust.client.xcra.ui.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.extension.serializable
import com.xray.core.rust.client.xcra.extension.toast
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.handler.NotificationHandler
import com.xray.core.rust.client.xcra.service.TestService
import com.xray.core.rust.client.xcra.util.Utils
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanCustomCode
import io.github.g00fy2.quickie.config.ScannerConfig

abstract class BaseActivity : ComponentActivity() {
    private var pendingAction: Action = Action.NONE

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                when (pendingAction) {
                    Action.IMPORT_QR_CODE_CONFIG ->
                        launchScanner()

                    Action.POST_NOTIFICATIONS -> {
                        NotificationHandler.createNotificationChannels(this)
                    }

                    else -> {}
                }
            } else {
                toast(R.string.toast_permission_denied)
            }
            pendingAction = Action.NONE
        }
    private var broadcastReceiver: BroadcastReceiver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {

                val action = intent.getStringExtra(TestService.BROADCAST_TYPE)
                if (action == TestService.BROADCAST_TYPE_CONFIG) {
                    val testResultPair =
                        intent.serializable<Pair<String, Long>>(TestService.BROADCAST_MESSAGE)
                            ?: return
                    DatabaseHandler.encodeNodeTestDelayMillis(
                        testResultPair.first,
                        testResultPair.second
                    )
                    updateNodeItem(testResultPair.first)
                }
                if (action == TestService.BROADCAST_TYPE_ACTIVE) {
                    val ping = intent.getLongExtra(TestService.BROADCAST_MESSAGE, -1)
                    updateActiveNode(ping)
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(TestService.BROADCAST_ACTION)

        ContextCompat.registerReceiver(
            application,
            broadcastReceiver,
            intentFilter,
            Utils.receiverFlags()
        )
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (_: java.lang.Exception) {
        }
    }

    private enum class Action {
        NONE,
        IMPORT_QR_CODE_CONFIG,
        POST_NOTIFICATIONS
    }

    protected fun requestNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                pendingAction = Action.POST_NOTIFICATIONS
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    protected fun openScanner() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            launchScanner()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private val scanQrCode = registerForActivityResult(ScanCustomCode(), ::handleResult)


    protected fun launchScanner() {
        scanQrCode.launch(
            ScannerConfig.build {
                setHapticSuccessFeedback(true) // enable (default) or disable haptic feedback when a barcode was detected
                setShowTorchToggle(true) // show or hide (default) torch/flashlight toggle button
                setShowCloseButton(true) // show or hide (default) close button
            }
        )
    }

    private fun handleResult(result: QRResult) {
        if (result is QRResult.QRSuccess) {
            scanResult(result.content.rawValue.orEmpty())
        } else {
            scanResult(null)
        }
    }

    abstract fun scanResult(text: String?)
    abstract fun updateNodeItem(uuid: String)
    abstract fun updateActiveNode(ping: Long)
}