package com.xray.core.rust.client.xcra.handler

import android.content.Context
import android.os.ParcelFileDescriptor
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.util.Utils
import java.io.File

class HevTunHandler {
    companion object {
        init {
            System.loadLibrary("hev-socks5-tunnel")
        }
    }

    @Suppress("FunctionName")
    private external fun TProxyStartService(configPath: String?, fd: Int)

    @Suppress("FunctionName")
    private external fun TProxyStopService()

    @Suppress("FunctionName")
    private external fun TProxyGetStats(): LongArray?

    private val sync = Any()
    private var isRunning = true


    fun start(context: Context, parcelFileDescriptor: ParcelFileDescriptor): Boolean {
        synchronized(sync) {
            if (!isRunning) {
                return false
            }
        }

        val path = context.filesDir.toString() + "/hev.yml"
        Utils.printToFile(File(path), buildConfig())
        App.logService("tun line yml:" + buildConfig())
        App.logService("tun fd:" + parcelFileDescriptor.fd)

        TProxyStartService(path, parcelFileDescriptor.fd)

        return true
    }

    fun stop() {
        synchronized(sync) {
            if (isRunning) {
                TProxyStopService()
            }
            isRunning = false
        }
        App.logService("tun stop")
    }

    private fun buildConfig(): String {
        val socksPort = DatabaseHandler.getSocksPort()
        val vpnConfig = DatabaseHandler.getCurrentVpnInterfaceAddressConfig()
        val mtu = DatabaseHandler.getVpnMtu()
        val isIpv6 = DatabaseHandler.getVpnIpv6Enable()
        return buildString {
            appendLine("tunnel:")
            appendLine("  mtu: $mtu")
            appendLine("  ipv4: ${vpnConfig.ipv4Client}")

            if (isIpv6) {
                appendLine("  ipv6: '${vpnConfig.ipv6Client}'")
            }

            appendLine("socks5:")
            appendLine("  port: $socksPort")
            appendLine("  address: ${AppConfig.LOOPBACK_IP}")
            appendLine("  udp: 'udp'")

            appendLine("misc:")
            appendLine("  tcp-read-write-timeout: ${DatabaseHandler.decodeSettingsString(AppConfig.PREF_VPN_INTERFACE_TCP_RW_TIMEOUT) ?: AppConfig.DEFAULT_VPN_INTERFACE_TCP_RW_TIMEOUT}")
            appendLine("  udp-read-write-timeout: ${DatabaseHandler.decodeSettingsString(AppConfig.PREF_VPN_INTERFACE_UDP_RW_TIMEOUT) ?: AppConfig.DEFAULT_VPN_INTERFACE_UDP_RW_TIMEOUT}")
            appendLine("  log-level: ${DatabaseHandler.decodeSettingsString(AppConfig.PREF_VPN_INTERFACE_LOG_LEVEL) ?: "warn"}")
        }
    }
}