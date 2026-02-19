package com.xray.core.rust.client.xcra.dto.core

import com.xray.core.rust.client.xcra.dto.core.inbound.Inbound
import com.xray.core.rust.client.xcra.dto.core.log.Log
import com.xray.core.rust.client.xcra.dto.core.outbound.Outbound

data class Config(
    val log: Log = Log(),
    val inbounds: MutableList<Inbound>,
    val outbounds: MutableList<Outbound>,
) {
    fun addMoreOutbounds() {
        //todo add direct and block
    }

    companion object {
//        fun createForConnect(
//        fun createForConnect(
//            listen: String,
//            port: Int,
//            link: String
//        ): Config {
//            val inbounds = mutableListOf<Inbound>()
//            val outbounds = mutableListOf<Outbound>()
//
//            inbounds.add(
//                Inbound(
//                    protocol = "socks",
//                    settings = Inbound.SocksSetting(listen = listen, port = port)
//                )
//            )
//
//            val xrayParser = Parser(link)
//            val outbound: Outbound? = xrayParser.parse()
//            outbound?.let {
//                outbounds.add(it)
//            }
//            if (outbounds.isEmpty()) {
//                outbounds.add(
//                    Outbound(
//                        protocol = "freedom"
//                    )
//                )
//            }
//            val config = Config(inbounds = inbounds, outbounds = outbounds)
//            return config
//        }

        fun createTemplate(
        ): Config {
            val inbounds = mutableListOf<Inbound>()
            val outbounds = mutableListOf<Outbound>()

            val config = Config(inbounds = inbounds, outbounds = outbounds)
            return config
        }
    }
}