package com.xray.core.rust.client.xcra.ui.model

import android.app.Application
import android.webkit.URLUtil
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.extension.toastError
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.handler.DatabaseHandler.decodeSettingsBool
import com.xray.core.rust.client.xcra.handler.DatabaseHandler.decodeSettingsString
import com.xray.core.rust.client.xcra.util.IpUtil
import com.xray.core.rust.client.xcra.util.Utils


class SettingsViewModel(
    application: Application,
) :
    AndroidViewModel(application) {
    var proxySharing by mutableStateOf(false)
        private set
    var autoUpdate by mutableStateOf(false)
        private set
    var autoUpdateInterval by mutableStateOf(0)
        private set
    var resolveAddress by mutableStateOf(false)
        private set
    var socksPort by mutableStateOf(0)
        private set
    var logLevel by mutableStateOf("")
        private set
    var allowInsecure by mutableStateOf("")
        private set
    var nodeTestUrl by mutableStateOf("")
        private set
    var vpnInterfaceAddress by mutableStateOf("")
        private set
    var vpnInterfaceDns by mutableStateOf("")
        private set
    var vpnInterfaceMTU by mutableStateOf(0)
        private set
    var vpnInterfaceIpv6Enable by mutableStateOf(false)
        private set
    var vpnInterfaceBypassLanEnable by mutableStateOf(false)
        private set
    var vpnInterfaceHttpProxyEnable by mutableStateOf(false)
        private set
    var vpnInterfaceLogLevel by mutableStateOf("")
        private set
    var vpnInterfaceTcpTimeout by mutableStateOf(0)
        private set
    var vpnInterfaceUdpTimeout by mutableStateOf(0)
        private set

    init {

        autoUpdateInterval = Utils.parseInt(
            decodeSettingsString(AppConfig.PREF_AUTO_UPDATE_INTERVAL),
            AppConfig.DEFAULT_AUTO_UPDATE_INTERVAL
        )
        autoUpdate = decodeSettingsBool(AppConfig.PREF_AUTO_UPDATE_SUBSCRIPTION)

        socksPort = Utils.parseInt(
            decodeSettingsString(AppConfig.PREF_SOCKS_PORT),
            AppConfig.DEFAULT_PORT_SOCKS
        )
        proxySharing = decodeSettingsBool(AppConfig.PREF_PROXY_SHARING)
        resolveAddress = decodeSettingsBool(AppConfig.PREF_RESOLVE_ADDRESS)
        logLevel = decodeSettingsString(AppConfig.PREF_LOG_LEVEL) ?: AppConfig.DEFAULT_LOG_LEVEL
        allowInsecure =
            decodeSettingsString(AppConfig.PREF_ALLOW_INSECURE) ?: AppConfig.DEFAULT_ALLOW_INSECURE
        nodeTestUrl =
            decodeSettingsString(AppConfig.PREF_NODE_TEST_URL) ?: AppConfig.DEFAULT_NODE_TEST_URL


        vpnInterfaceAddress =
            decodeSettingsString(AppConfig.PREF_VPN_INTERFACE_ADDRESS)
                ?: AppConfig.DEFAULT_VPN_INTERFACE_ADDRESS
        vpnInterfaceDns =
            decodeSettingsString(AppConfig.PREF_VPN_INTERFACE_DNS)
                ?: AppConfig.DEFAULT_VPN_INTERFACE_DNS

        vpnInterfaceMTU = Utils.parseInt(
            decodeSettingsString(AppConfig.PREF_VPN_INTERFACE_MTU),
            AppConfig.DEFAULT_VPN_INTERFACE_MTU
        )
        vpnInterfaceIpv6Enable =
            decodeSettingsBool(AppConfig.PREF_VPN_INTERFACE_IPV6)
        vpnInterfaceBypassLanEnable =
            decodeSettingsBool(AppConfig.PREF_VPN_INTERFACE_BYPASS_LAN)

        vpnInterfaceHttpProxyEnable =
            decodeSettingsBool(AppConfig.PREF_VPN_INTERFACE_APPEND_HTTP_PROXY)
        vpnInterfaceLogLevel =
            decodeSettingsString(AppConfig.PREF_VPN_INTERFACE_LOG_LEVEL)
                ?: AppConfig.DEFAULT_VPN_TUNNEL_LOG_LEVEL
        vpnInterfaceTcpTimeout = Utils.parseInt(
            decodeSettingsString(AppConfig.PREF_VPN_INTERFACE_TCP_RW_TIMEOUT),
            AppConfig.DEFAULT_VPN_INTERFACE_TCP_RW_TIMEOUT
        )
        vpnInterfaceUdpTimeout = Utils.parseInt(
            decodeSettingsString(AppConfig.PREF_VPN_INTERFACE_UDP_RW_TIMEOUT),
            AppConfig.DEFAULT_VPN_INTERFACE_UDP_RW_TIMEOUT
        )
    }

    fun updateSocksPort(value: String) {
        val port = Utils.parseInt(
            value,
            AppConfig.DEFAULT_PORT_SOCKS
        )
        if (socksPort != port) {
            if (port in 1..<65535) {
                socksPort = port
                DatabaseHandler.encodeSettings(AppConfig.PREF_SOCKS_PORT, socksPort)
            } else {
                application.toastError(R.string.toast_failure)
            }
        }
    }

    fun updateLogLevel(value: String) {
        DatabaseHandler.encodeSettings(AppConfig.PREF_LOG_LEVEL, value)
        logLevel = value
    }

    fun updateVpnInterfaceAddress(value: String) {
        DatabaseHandler.encodeSettings(AppConfig.PREF_VPN_INTERFACE_ADDRESS, value)
        vpnInterfaceAddress = value
    }

    fun updateVpnInterfaceDns(value: String) {
        if (IpUtil.isPureIpAddress(value)) {
            DatabaseHandler.encodeSettings(AppConfig.PREF_VPN_INTERFACE_DNS, value)
            vpnInterfaceDns = value
        } else {
            application.toastError(R.string.toast_failure)
        }
    }

    fun updateVpnInterfaceMTU(value: String) {
        val mtu = Utils.parseInt(
            value,
            AppConfig.DEFAULT_PORT_SOCKS
        )
        if (vpnInterfaceMTU != mtu) {
            DatabaseHandler.encodeSettings(AppConfig.PREF_VPN_INTERFACE_MTU, mtu)
        }
    }

    fun updateResolveAddress(it: Boolean) {
        DatabaseHandler.encodeSettings(AppConfig.PREF_RESOLVE_ADDRESS, it)
        resolveAddress = it
    }

    fun updateProxySharing(it: Boolean) {
        DatabaseHandler.encodeSettings(AppConfig.PREF_PROXY_SHARING, it)
        proxySharing = it
    }

    fun updateNodeTestUrl(value: String) {
        if (URLUtil.isValidUrl(value)) {
            DatabaseHandler.encodeSettings(AppConfig.PREF_LOG_LEVEL, value)
            logLevel = value
        } else {
            application.toastError(R.string.toast_failure)
        }
    }

    fun updateVpnInterfaceIpv6Enable(it: Boolean) {
        DatabaseHandler.encodeSettings(AppConfig.PREF_VPN_INTERFACE_IPV6, it)
        vpnInterfaceIpv6Enable = it
    }

    fun updateVpnInterfaceBypassLanEnable(it: Boolean) {
        DatabaseHandler.encodeSettings(AppConfig.PREF_VPN_INTERFACE_BYPASS_LAN, it)
        vpnInterfaceBypassLanEnable = it
    }

    fun updateVpnInterfaceHttpProxyEnable(it: Boolean) {
        DatabaseHandler.encodeSettings(AppConfig.PREF_VPN_INTERFACE_APPEND_HTTP_PROXY, it)
        vpnInterfaceHttpProxyEnable = it
    }

    fun updateVpnInterfaceLogLevel(value: String) {
        DatabaseHandler.encodeSettings(AppConfig.PREF_VPN_INTERFACE_LOG_LEVEL, value)
        vpnInterfaceLogLevel = value
    }

    fun updateVpnInterfaceTcpTimeout(value: String) {
        val timeout = Utils.parseInt(
            value,
            AppConfig.DEFAULT_VPN_INTERFACE_TCP_RW_TIMEOUT
        )
        if (vpnInterfaceTcpTimeout != timeout) {
            if (timeout > 0) {
                vpnInterfaceTcpTimeout = timeout
                DatabaseHandler.encodeSettings(
                    AppConfig.PREF_VPN_INTERFACE_TCP_RW_TIMEOUT,
                    timeout
                )
            } else {
                application.toastError(R.string.toast_failure)
            }
        }
    }

    fun updateVpnInterfaceUdpTimeout(value: String) {
        val timeout = Utils.parseInt(
            value,
            AppConfig.DEFAULT_VPN_INTERFACE_UDP_RW_TIMEOUT
        )
        if (vpnInterfaceUdpTimeout != timeout) {
            if (timeout > 0) {
                vpnInterfaceUdpTimeout = timeout
                DatabaseHandler.encodeSettings(
                    AppConfig.PREF_VPN_INTERFACE_UDP_RW_TIMEOUT,
                    timeout
                )
            } else {
                application.toastError(R.string.toast_failure)
            }
        }
    }

    fun updateAllowInsecure(value: String) {
        DatabaseHandler.encodeSettings(AppConfig.PREF_ALLOW_INSECURE, value)
        allowInsecure = value
    }

    fun updateAutoUpdate(it: Boolean) {
        DatabaseHandler.encodeSettings(AppConfig.PREF_AUTO_UPDATE_SUBSCRIPTION, it)
        autoUpdate = it
    }

    fun updateAutoUpdateInterval(value: String) {
        val interval = Utils.parseInt(
            value,
            AppConfig.DEFAULT_AUTO_UPDATE_INTERVAL
        )
        if (autoUpdateInterval != interval) {
            if (interval >= 15) {
                autoUpdateInterval = interval
                DatabaseHandler.encodeSettings(
                    AppConfig.PREF_AUTO_UPDATE_INTERVAL,
                    interval
                )
            } else {
                application.toastError(R.string.toast_failure)
            }
        }
    }
}

val LocalSettingsViewModel = staticCompositionLocalOf<SettingsViewModel> {
    error("SettingsViewModel not provided")
}


object SettingsViewModelAccessor {
    /**
     * Retrieves the current [SettingsViewModel] at the call site's position in the hierarchy.
     */
    val settingsViewModel: SettingsViewModel
        @Composable @ReadOnlyComposable get() = LocalSettingsViewModel.current

}

@Composable
fun SettingsViewModel(
    settingsViewModel: SettingsViewModel,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalSettingsViewModel.provides(settingsViewModel)) {
        content()
    }
}