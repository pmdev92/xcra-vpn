package com.xray.core.rust.client.xcra.service

import android.annotation.SuppressLint
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.ParcelFileDescriptor
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.BuildConfig
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.VpnServiceInterface
import com.xray.core.rust.client.xcra.dto.NodeItem
import com.xray.core.rust.client.xcra.enums.VpnState
import com.xray.core.rust.client.xcra.extension.toast
import com.xray.core.rust.client.xcra.handler.AndroidVpnHandler
import com.xray.core.rust.client.xcra.handler.CoreConfigHandler
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.handler.HevTunHandler
import com.xray.core.rust.client.xcra.handler.NotificationHandler
import com.xray.core.rust.client.xcra.handler.XrayCoreHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("VpnServicePolicy")
class XcraVpnService : VpnService() {
    companion object {

        const val PACKAGE: String = BuildConfig.APPLICATION_ID
        const val BROADCAST_ACTION: String = "${PACKAGE}_VPN_BROADCAST_ACTION"
        const val BROADCAST_STATE: String = "${PACKAGE}_VPN_BROADCAST_STATE"

        const val ACTION_START: String = "ACTION_START"
        const val ACTION_STOP: String = "ACTION_STOP"
        const val ACTION_RESTART: String = "ACTION_RESTART"

    }

    private val mBinder: IBinder = LocalBinder(this)
    private var xrayCoreHandler: XrayCoreHandler? = null
    private var hevTunHandler: HevTunHandler? = null
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var nodeItem: NodeItem? = null

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder {
        val iBinder = super.onBind(intent) ?: return mBinder
        return iBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        App.log("onStartCommand")
        val action = intent?.action
        if (ACTION_START == action) {
            serviceScope.launch {
                startCore()
            }
        } else if (ACTION_STOP == action) {
            serviceScope.launch {
                stopCore()
            }
        } else if (ACTION_RESTART == action) {
            serviceScope.launch {
                stopCore()
                delay(500)
                startCore()
            }
        }
        return START_NOT_STICKY
    }

    override fun onRevoke() {
        stopCore()
        super.onRevoke()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startCore(): Boolean {
        setConnectionState(VpnState.CONNECTING)
        val uuid = DatabaseHandler.getSelectNodeUUID() ?: return false
        val config = CoreConfigHandler.getConfig4Connect(uuid)
        try {
            if (!config.status) {
                setConnectionState(VpnState.DISCONNECTED)
                Handler(mainLooper).post {
                    toast(R.string.config_create_error)
                }
                return false
            }
            nodeItem = config.nodeItem
            xrayCoreHandler = XrayCoreHandler(this)
            xrayCoreHandler?.start(config.json)
            parcelFileDescriptor = AndroidVpnHandler.start(Builder(), nodeItem?.remarks.orEmpty())
            hevTunHandler = HevTunHandler()
            hevTunHandler?.start(this, parcelFileDescriptor!!)
            setConnectionState(VpnState.CONNECTED)
            return true
        } catch (e: Exception) {
            e.fillInStackTrace()
            e.message?.let {
                App.log(it)
            }
        }
        return false
    }

    private fun stopCore() {
        if (vpnState == VpnState.CONNECTED) {
            setConnectionState(VpnState.DISCONNECTING)
            hevTunHandler?.stop()
            parcelFileDescriptor?.let {
                AndroidVpnHandler.stop(it)
            }
        }
        setConnectionState(VpnState.DISCONNECTED)
    }


    @Synchronized
    fun setConnectionState(vpnState: VpnState) {
        if (vpnState == VpnState.CONNECTED && this.vpnState != VpnState.CONNECTED) {
            val message = nodeItem?.remarks ?: "Connected"
            NotificationHandler.sendNotificationConnected(this, message)
        } else if (vpnState == VpnState.DISCONNECTED && this.vpnState != VpnState.DISCONNECTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                stopForeground(true)
            }
        }
        this.vpnState = vpnState
        sendBroadCast()
    }

    private fun sendBroadCast() {
        val intent = Intent(BROADCAST_ACTION)
        intent.putExtra(BROADCAST_STATE, vpnState.value)
        sendBroadcast(intent)
    }


    internal var vpnState: VpnState = VpnState.DISCONNECTED

    class LocalBinder(val xcraVpnService: XcraVpnService) : VpnServiceInterface.Stub() {
        override fun getState(): Int {
            return xcraVpnService.vpnState.value
        }
    }
}