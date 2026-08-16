package com.xray.core.rust.client.xcra.util

import android.annotation.SuppressLint
import okhttp3.OkHttpClient
import java.net.Proxy
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object OkHttpClientUtil {
    fun getUnsafeOkHttpClientWithProxy(timeOut: Int, proxy: Proxy?): OkHttpClient {
        return try {
            @SuppressLint("CustomX509TrustManager") val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
            builder.connectTimeout(timeOut.toLong(), TimeUnit.SECONDS)
                .writeTimeout(timeOut.toLong(), TimeUnit.SECONDS)
                .readTimeout(timeOut.toLong(), TimeUnit.SECONDS)
                .proxy(proxy)
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}