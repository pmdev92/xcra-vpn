package com.xray.core.rust.client.xcra.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.extension.toast
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanCustomCode
import io.github.g00fy2.quickie.config.ScannerConfig

abstract class ScannerActivity : ComponentActivity() {
    private var pendingAction: Action = Action.NONE

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                when (pendingAction) {
                    Action.IMPORT_QR_CODE ->
                        launchScanner()

                    else -> {}
                }
            } else {
                toast(R.string.toast_permission_denied)
            }
            pendingAction = Action.NONE
        }


    private enum class Action {
        NONE,
        IMPORT_QR_CODE
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
            pendingAction = Action.IMPORT_QR_CODE
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
}