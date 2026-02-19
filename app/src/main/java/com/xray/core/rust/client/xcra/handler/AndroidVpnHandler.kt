package com.xray.core.rust.client.xcra.handler

import android.content.pm.PackageManager
import android.net.ProxyInfo
import android.net.VpnService.Builder
import android.os.Build
import android.os.ParcelFileDescriptor
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.BuildConfig
import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.util.IpUtil

object AndroidVpnHandler {
    fun start(builder: Builder, sessionName: String): ParcelFileDescriptor? {
        // Configure network settings (addresses, routing and DNS)
        configureNetworkSettings(builder, sessionName)
        // Configure app-specific settings (session name and per-app proxy)
        configurePerAppProxy(builder)

        configurePlatformFeatures(builder)
        try {
            return builder.establish()!!
        } catch (_: Exception) {
        }
        return null
    }

    fun stop(fileDescriptor: ParcelFileDescriptor) {
        try {
            fileDescriptor.close()
        } catch (e: Exception) {
            App.logService("fileDescriptor exception:" + (e.message))
            e.fillInStackTrace()
        }
    }


    private fun configureNetworkSettings(builder: Builder, sessionName: String) {
        val vpnConfig = DatabaseHandler.getCurrentVpnInterfaceAddressConfig()
        val bypassLan = DatabaseHandler.getVpnBypassLan()

        // Configure IPv4 settings
        builder.setMtu(DatabaseHandler.getVpnMtu())
        builder.addAddress(vpnConfig.ipv4Client, 30)

        // Configure routing rules
        if (bypassLan) {
            AppConfig.ROUTED_IP_LIST.forEach {
                val addr = it.split('/')
                builder.addRoute(addr[0], addr[1].toInt())
            }
        } else {
            builder.addRoute("0.0.0.0", 0)
        }

        // Configure IPv6 if enabled
        if (DatabaseHandler.getVpnIpv6Enable()) {
            builder.addAddress(vpnConfig.ipv6Client, 126)
            if (bypassLan) {
                builder.addRoute("2000::", 3) // Currently only 1/8 of total IPv6 is in use
            } else {
                builder.addRoute("::", 0)
            }
        }


        DatabaseHandler.getVpnDnsServers().forEach {
            if (IpUtil.isPureIpAddress(it)) {
                builder.addDnsServer(it)
            }
        }

        builder.setSession(sessionName)
    }

    /**
     * Configures per-app proxy rules for the VPN builder.
     *
     * - If per-app proxy is not enabled, disallow the VPN service's own package.
     * - If no apps are selected, disallow the VPN service's own package.
     * - If bypass mode is enabled, disallow all selected apps (including self).
     * - If proxy mode is enabled, only allow the selected apps (excluding self).
     *
     * @param builder The VPN Builder to configure.
     */
    private fun configurePerAppProxy(builder: Builder) {
        val selfPackageName = BuildConfig.APPLICATION_ID

        // If per-app proxy is not enabled, disallow the VPN service's own package and return
        if (!DatabaseHandler.decodeSettingsBool(AppConfig.PREF_PER_APP_PROXY)) {
            return
        }

        // If no apps are selected, disallow the VPN service's own package and return
        val apps = DatabaseHandler.decodeSettingsStringSet(AppConfig.PREF_PER_APP_PROXY_APPS_SET)
        if (apps.isNullOrEmpty()) {

            return
        }
        apps.remove(selfPackageName)

        val bypassApps = DatabaseHandler.decodeSettingsBool(AppConfig.PREF_BYPASS_APPS)
        apps.forEach {
            try {
                if (bypassApps) {
                    // In bypass mode, disallow the selected apps
                    builder.addDisallowedApplication(it)
                } else {
                    // In proxy mode, only allow the selected apps
                    builder.addAllowedApplication(it)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                App.log("Failed to configure app in VPN: $e")
            }
        }
    }

    /**
     * Configures platform-specific VPN features for different Android versions.
     *
     * @param builder The VPN Builder to configure
     */
    private fun configurePlatformFeatures(builder: Builder) {
//        // Android P (API 28) and above: Configure network callbacks
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            try {
//                connectivity.requestNetwork(defaultNetworkRequest, defaultNetworkCallback)
//            } catch (e: Exception) {
//                Log.e(AppConfig.TAG, "Failed to request default network", e)
//            }
//        }
//
        // Android Q (API 29) and above: Configure metering and HTTP proxy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
            if (DatabaseHandler.decodeSettingsBool(AppConfig.PREF_VPN_INTERFACE_APPEND_HTTP_PROXY)) {
                builder.setHttpProxy(
                    ProxyInfo.buildDirectProxy(
                        AppConfig.LOOPBACK_IP,
                        DatabaseHandler.getHttpPort()
                    )
                )
            }
        }
    }
}