package com.xray.core.rust.client.xcra.ui.activity

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.net.VpnService
import android.os.Bundle
import android.os.IBinder
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.xray.core.rust.client.xcra.VpnServiceInterface
import com.xray.core.rust.client.xcra.enums.VpnState
import com.xray.core.rust.client.xcra.extension.toastError
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.service.XcraVpnService
import com.xray.core.rust.client.xcra.service.XcraVpnService.Companion.BROADCAST_ACTION
import com.xray.core.rust.client.xcra.util.Utils

abstract class VpnActivity : BaseActivity(), ServiceConnection {
    private val vpnPermissionLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                startVPNService()
            } else {
                toastError("VPN permission required for establish vpn connection")
            }
        }
    protected var vpnState: VpnState = VpnState.DISCONNECTED
    private var vpnServiceInterface: VpnServiceInterface? = null
    private var broadcastReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val state = intent.getIntExtra(
                    XcraVpnService.BROADCAST_STATE,
                    VpnState.DISCONNECTED.value
                )
                vpnState = VpnState.fromInt(state) ?: VpnState.DISCONNECTED
                handleVpn(vpnState)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        val intent = Intent(this, XcraVpnService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)


        val intentFilter = IntentFilter(BROADCAST_ACTION)

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
            unbindService(this)
        } catch (_: java.lang.Exception) {
        }
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (_: java.lang.Exception) {
        }
    }

    abstract fun handleVpn(vpnState: VpnState)

    protected fun baseToggleVpn() {
        val state: VpnState =
            vpnServiceInterface?.getState()?.let { VpnState.fromInt(it) } ?: VpnState.DISCONNECTED
        if (state == VpnState.DISCONNECTED) {
            requestStartVpn()
        } else if (state == VpnState.CONNECTED) {
            stopVPNService()
        }
    }

    protected fun baseStartVpn() {
        val state: VpnState =
            vpnServiceInterface?.getState()?.let { VpnState.fromInt(it) } ?: VpnState.DISCONNECTED
        if (state == VpnState.DISCONNECTED) {
            requestStartVpn()
        }
    }

    protected fun baseStopVpn() {
        val state: VpnState =
            vpnServiceInterface?.getState()?.let { VpnState.fromInt(it) } ?: VpnState.DISCONNECTED
        if (state == VpnState.CONNECTED) {
            stopVPNService()
        }
    }

    private fun requestStartVpn() {
        val vpnIntent = try {
            VpnService.prepare(this)
        } catch (_: Exception) {
            null
        }
        if (vpnIntent != null) {
            vpnPermissionLauncher.launch(vpnIntent)
        } else {
            startVPNService()
        }
    }


    private fun startVPNService() {
        DatabaseHandler.getSelectNodeUUID() ?: return
        val intent = Intent(this, XcraVpnService::class.java)
        intent.action = XcraVpnService.ACTION_START
        startService(intent)
    }


    private fun stopVPNService() {
        val intent = Intent(this, XcraVpnService::class.java)
        intent.action = XcraVpnService.ACTION_STOP
        startService(intent)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (name?.className == XcraVpnService::class.java.name) {
            vpnServiceInterface = VpnServiceInterface.Stub.asInterface(service)
            vpnState =
                vpnServiceInterface?.getState()?.let { VpnState.fromInt(it) }
                    ?: VpnState.DISCONNECTED
            handleVpn(vpnState)
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        if (name?.className == XcraVpnService::class.java.name) {
            vpnServiceInterface = null
        }
    }
}