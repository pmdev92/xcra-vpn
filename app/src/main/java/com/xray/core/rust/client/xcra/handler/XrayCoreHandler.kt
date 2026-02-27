package com.xray.core.rust.client.xcra.handler

import android.net.VpnService
import com.xray.core.rust.AndroidLogger
import com.xray.core.rust.ProtectFd
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.shutdownXrayCore
import com.xray.core.rust.startXrayCore
import com.xray.core.rust.startXrayLogger

class XrayCoreHandler(val vpnService: VpnService? = null) : ProtectFd, AndroidLogger {
    companion object {
        var id: Int = 1
    }

    val id: Int = XrayCoreHandler.id++

    init {
        startXrayLogger(false, this)
    }

    fun start(json: String?) {
        App.log("json $json")
        startXrayCore(id.toUInt(), json!!, this)
    }

    fun stop() {
        shutdownXrayCore(id.toUInt())
    }

    override fun protect(id: ULong): Boolean {
        return vpnService?.protect(id.toInt()) ?: true
    }

    override fun log(message: String) {
        App.log("xray core rust: $message")
    }
}