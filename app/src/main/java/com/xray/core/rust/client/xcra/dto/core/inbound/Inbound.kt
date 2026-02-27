package com.xray.core.rust.client.xcra.dto.core.inbound

import com.google.gson.annotations.SerializedName

data class Inbound(
    @SerializedName("tag")
    var tag: String? = null,
    @SerializedName("protocol")
    var protocol: String,
    @SerializedName("settings")
    val settings: Setting
) {
    abstract class Setting
    data class SocksSetting(
        @SerializedName("listen")
        var listen: String,
        @SerializedName("port")
        val port: Int
    ) : Setting() {
    }

    data class HttpSetting(
        @SerializedName("listen")
        var listen: String,
        @SerializedName("port")
        val port: Int
    ) : Setting() {
    }


    companion object {
        fun createSocks5(listen: String = "127.0.0.1", port: Int): Inbound {
            return Inbound(
                protocol = "socks",
                settings = SocksSetting(listen = listen, port = port)
            )
        }

        fun createHttp(listen: String = "127.0.0.1", port: Int): Inbound {
            return Inbound(
                protocol = "http",
                settings = HttpSetting(listen = listen, port = port)
            )
        }
    }
}


