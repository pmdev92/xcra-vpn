package com.xray.core.rust.client.xcra

import android.app.Application
import android.util.Log
import com.tencent.mmkv.MMKV
import com.xray.core.rust.initialize

class App : Application() {
    companion object {
        var logEnable: Boolean = true

        var TAG: String = "APP_XCRA_VPN"
        var TAG_SERVICE: String = "SERVICE_XCRA_VPN"
        fun log(message: String) {
            if (!logEnable) {
                return
            }
            Log.i(TAG, message)
        }

        fun logService(message: String) {
            if (!logEnable) {
                return
            }
            Log.i(TAG_SERVICE, message)
        }
    }

    override fun onCreate() {
        super.onCreate()
        initialize()
        MMKV.initialize(this)
    }
}