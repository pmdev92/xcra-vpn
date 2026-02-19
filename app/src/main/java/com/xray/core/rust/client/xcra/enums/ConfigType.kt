package com.xray.core.rust.client.xcra.enums

enum class ConfigType(val value: Int) {

    SOCKS5(1),
    VLESS(2),
    VMESS(3),
    TROJAN(4),
    SHADOWSOCKS(5),
    TUIC(6),
    HYSTERIA2(7);


    companion object {
        fun fromInt(value: Int) =
            ConfigType.entries.firstOrNull { it.value == value }
    }

    fun getTitle(): String {
        val name = when (this) {
            SOCKS5 -> "Socks5"
            VLESS -> "Vless"
            VMESS -> "Vmess"
            TROJAN -> "Trojan"
            SHADOWSOCKS -> "ShadowSocks"
            TUIC -> "Tuic"
            HYSTERIA2 -> "Hysteria2"
        }
        return name
    }

    fun hasTransport(): Boolean {
        return when (this) {
            TUIC -> false
            HYSTERIA2 -> false
            else -> {
                true
            }
        }
    }

    fun getProtocol(): String {
        return when (this) {
            SOCKS5 -> "socks5"
            VLESS -> "vless"
            VMESS -> "vmess"
            TROJAN -> "trojan"
            SHADOWSOCKS -> "ss"
            TUIC -> "tuic"
            HYSTERIA2 -> "hysteria2"
        }
    }

    fun getProtocolScheme(): String {
        val name = getProtocol()
        return "$name://"
    }

    fun isProtocolScheme(str: String): Boolean {
        when (this) {
            SOCKS5 -> {
                return str.startsWith(SOCKS5.getProtocolScheme(), true)
            }

            VLESS -> {
                return str.startsWith(VLESS.getProtocolScheme(), true)
            }

            VMESS -> {
                return str.startsWith(VMESS.getProtocolScheme(), true)
            }

            TROJAN -> {
                return str.startsWith(TROJAN.getProtocolScheme(), true)
            }

            SHADOWSOCKS -> {
                if (str.startsWith(SHADOWSOCKS.getProtocolScheme(), true)) {
                    return true
                }
                return str.startsWith("shadowsocks", true)
            }

            TUIC -> {
                return str.startsWith(TUIC.getProtocolScheme(), true)
            }

            HYSTERIA2 -> {
                if (str.startsWith(HYSTERIA2.getProtocolScheme(), true)) {
                    return true
                }
                return str.startsWith("hy2", true)
            }
        }
    }

}