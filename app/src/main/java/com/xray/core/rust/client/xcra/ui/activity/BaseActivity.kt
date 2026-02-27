package com.xray.core.rust.client.xcra.ui.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.extension.serializable
import com.xray.core.rust.client.xcra.extension.toast
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.handler.NotificationHandler
import com.xray.core.rust.client.xcra.service.TestService
import com.xray.core.rust.client.xcra.util.Utils

abstract class BaseActivity : ScannerActivity() {
    private var pendingAction: Action = Action.NONE

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                when (pendingAction) {
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

    abstract fun updateNodeItem(uuid: String)
    abstract fun updateActiveNode(ping: Long)
}