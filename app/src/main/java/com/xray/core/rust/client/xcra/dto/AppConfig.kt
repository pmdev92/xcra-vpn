package com.xray.core.rust.client.xcra.dto

object AppConfig {
    const val REPOSITORY = "pmdev92/xcra-vpn"
    const val GITHUB_RAW_URL = "https://raw.githubusercontent.com"
    const val GITHUB_URL = "https://github.com"
    const val APP_URL = "$GITHUB_URL/REPOSITORY"
    const val APP_ISSUES_URL = "${APP_URL}/issues"
    const val TG_CHANNEL_URL = "https://t.me/..."
    const val APP_PRIVACY_POLICY = "$GITHUB_RAW_URL/$REPOSITORY/master/CR.md"

    const val APP_API_URL = "https://api.github.com/repos/REPOSITORY/releases"

    const val DIR_BACKUPS = "backups"
    const val ALLOW_INSECURE_FORCE_SECURE = "Force Secure"
    const val ALLOW_INSECURE_FORCE_INSECURE = "Force Insecure"
    const val ALLOW_INSECURE_FOLLOW = "Follow Configuration"
    const val TLS = "tls"
    const val REALITY = "reality"
    const val DEFAULT_VMESS_SECURITY = "auto"
    const val DEFAULT_PORT_SOCKS = 10808
    const val DEFAULT_LOG_LEVEL = "warning"
    const val DEFAULT_NODE_TEST_URL = "https://www.gstatic.com/generate_204"
    const val DEFAULT_VPN_INTERFACE_ADDRESS = "10.10.15.x"
    const val DEFAULT_VPN_INTERFACE_DNS = "1.1.1.1"
    const val DEFAULT_VPN_INTERFACE_MTU = 1500
    const val DEFAULT_VPN_TUNNEL_LOG_LEVEL = "warn"
    const val DEFAULT_VPN_INTERFACE_TCP_RW_TIMEOUT = 30000
    const val DEFAULT_VPN_INTERFACE_UDP_RW_TIMEOUT = 200000
    const val DEFAULT_AUTO_UPDATE_INTERVAL = 1440
    const val DEFAULT_ALLOW_INSECURE = ALLOW_INSECURE_FOLLOW

    const val SHARE_IP = "0.0.0.0"
    const val LOOPBACK_IP = "127.0.0.1"
    val PRIVATE_IP_LIST = arrayListOf(
        "0.0.0.0/8",
        "10.0.0.0/8",
        "127.0.0.0/8",
        "172.16.0.0/12",
        "192.168.0.0/16",
        "169.254.0.0/16",
        "224.0.0.0/4"
    )

    //minimum list https://serverfault.com/a/304791
    val ROUTED_IP_LIST = arrayListOf(
        "0.0.0.0/5",
        "8.0.0.0/7",
        "11.0.0.0/8",
        "12.0.0.0/6",
        "16.0.0.0/4",
        "32.0.0.0/3",
        "64.0.0.0/2",
        "128.0.0.0/3",
        "160.0.0.0/5",
        "168.0.0.0/6",
        "172.0.0.0/12",
        "172.32.0.0/11",
        "172.64.0.0/10",
        "172.128.0.0/9",
        "173.0.0.0/8",
        "174.0.0.0/7",
        "176.0.0.0/4",
        "192.0.0.0/9",
        "192.128.0.0/11",
        "192.160.0.0/13",
        "192.169.0.0/16",
        "192.170.0.0/15",
        "192.172.0.0/14",
        "192.176.0.0/12",
        "192.192.0.0/10",
        "193.0.0.0/8",
        "194.0.0.0/7",
        "196.0.0.0/6",
        "200.0.0.0/5",
        "208.0.0.0/4",
        "240.0.0.0/4"
    )

    //Settings Pref
    const val PREF_PROXY_SHARING = "pref_proxy_sharing_enabled"
    const val PREF_RESOLVE_ADDRESS = "pref_resolve_address"
    const val PREF_SOCKS_PORT = "pref_socks_port"

    const val PREF_ALLOW_INSECURE = "pref_allow_insecure"
    const val PREF_LOG_LEVEL = "pref_core_log_level"
    const val PREF_NODE_TEST_URL = "pref_node_test_url"
    const val PREF_VPN_INTERFACE_ADDRESS = "pref_vpn_interface_address"
    const val PREF_VPN_INTERFACE_DNS = "pref_vpn_interface_dns"
    const val PREF_VPN_INTERFACE_MTU = "pref_vpn_interface_mtu"
    const val PREF_VPN_INTERFACE_BYPASS_LAN = "pref_vpn_interface_bypass_lan"
    const val PREF_VPN_INTERFACE_IPV6 = "pref_vpn_interface_ipv6"
    const val PREF_VPN_INTERFACE_APPEND_HTTP_PROXY = "pref_vpn_interface_append_http_proxy"
    const val PREF_VPN_INTERFACE_LOG_LEVEL = "pref_vpn_interface_log_level"
    const val PREF_VPN_INTERFACE_TCP_RW_TIMEOUT = "pref_vpn_interface_tcp_rw_timeout"
    const val PREF_VPN_INTERFACE_UDP_RW_TIMEOUT = "pref_vpn_interface_udp_rw_timeout"
    const val PREF_AUTO_UPDATE_SUBSCRIPTION = "pref_auto_update_subscription"
    const val PREF_AUTO_UPDATE_INTERVAL = "pref_auto_update_interval"

    //Per-apps Pref
    const val PREF_PER_APP_PROXY = "pref_per_app_proxy_enable"
    const val PREF_PER_APP_PROXY_APPS_SET = "pref_per_app_proxy_apps"
    const val PREF_BYPASS_APPS = "pref_bypass_apps"

    //Update
    const val PREF_IS_PRE_RELEASE_ENABLE = "pref_is_pre_release_enable"
}