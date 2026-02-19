package com.xray.core.rust.client.xcra.enums


enum class TransportType(val type: String) {
    TCP("tcp"),
    WS("ws"),
    HTTP_UPGRADE("httpupgrade"),
    XHTTP("xhttp"),
    H2("h2"),
    GRPC("grpc");

    companion object {
        fun fromString(type: String?) = entries.find { it.type == type } ?: TCP
    }
}