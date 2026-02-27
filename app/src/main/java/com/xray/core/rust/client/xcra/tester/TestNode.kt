package com.xray.core.rust.client.xcra.tester

import android.webkit.URLUtil
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.handler.CoreConfigHandler
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.handler.DatabaseHandler.decodeSettingsString
import com.xray.core.rust.client.xcra.handler.XrayCoreHandler
import com.xray.core.rust.client.xcra.util.OkHttpClientUtil
import kotlinx.coroutines.delay
import okhttp3.Request
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy


object TestNode {

    private fun test(testUrl: String, socksAddress: String = "127.0.0.1", socksPort: Int): Long {
        val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(socksAddress, socksPort))
        val client = OkHttpClientUtil.getUnsafeOkHttpClientWithProxy(5, proxy)
        val request: Request = Request.Builder()
            .url(testUrl)
            .build()
        try {
            val tsStart = System.currentTimeMillis()
            val response = client.newCall(request).execute()
            val tsEnd = System.currentTimeMillis()
            if (response.code < 500) {
                return tsEnd - tsStart
            } else {
                return -1
            }
        } catch (e: IOException) {
            App.log("$e")
            e.printStackTrace()
            return -1
        }
    }


    suspend fun testServer(uuid: String): Long {
        var testUrl =
            decodeSettingsString(AppConfig.PREF_NODE_TEST_URL) ?: AppConfig.DEFAULT_NODE_TEST_URL
        if (!URLUtil.isValidUrl(testUrl)) {
            testUrl = AppConfig.DEFAULT_NODE_TEST_URL
        }
        var result = -1L
        try {
            val config = CoreConfigHandler.getConfig4Speedtest(uuid)
            if (config.status) {
                val xrayRunner = XrayCoreHandler()
                xrayRunner.start(config.json)
                delay(500)
                result = test(testUrl = testUrl, socksPort = config.socksPort)
                xrayRunner.stop()
            }
        } catch (e: Exception) {
            App.log("$e")
        }
        return result
    }


    fun testActive(): Long {
        var testUrl =
            decodeSettingsString(AppConfig.PREF_NODE_TEST_URL) ?: AppConfig.DEFAULT_NODE_TEST_URL
        if (!URLUtil.isValidUrl(testUrl)) {
            testUrl = AppConfig.DEFAULT_NODE_TEST_URL
        }
        val sockPort = DatabaseHandler.getSocksPort()
        var result = -1L
        try {
            result = test(testUrl = testUrl, socksPort = sockPort)
        } catch (e: Exception) {
            App.log("$e")
        }
        return result
    }
}

