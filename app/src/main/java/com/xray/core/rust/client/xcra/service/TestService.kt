package com.xray.core.rust.client.xcra.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.BuildConfig
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.tester.TestNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class TestService : Service() {
    companion object {
        const val PACKAGE: String = BuildConfig.APPLICATION_ID
        const val BROADCAST_ACTION: String = "${PACKAGE}_BROADCAST_ACTION"
        const val BROADCAST_TYPE: String = "${PACKAGE}_BROADCAST_TYPE"
        const val BROADCAST_MESSAGE: String = "${PACKAGE}_TESTER_BROADCAST_MESSAGE"
        const val BROADCAST_TYPE_ACTIVE: String = "${PACKAGE}_BROADCAST_TYPE_ACTIVE"
        const val BROADCAST_TYPE_CONFIG: String = "${PACKAGE}_BROADCAST_TYPE_CONFIG"

        const val ACTION_MEASURE_ACTIVE = "${PACKAGE}_ACTION_MEASURE_ACTIVE"
        const val ACTION_MEASURE_CONFIG = "${PACKAGE}_ACTION_MEASURE_CONFIG"
        const val ACTION_MEASURE_CONFIG_CANCEL = "${PACKAGE}_ACTION_MEASURE_CONFIG_CANCEL"
        const val EXTRA_UUID = "EXTRA_UUID"
        const val EXTRA_ID = "EXTRA_ID"


        fun cancelCurrentTests(ctx: Context) {
            try {
                val intent = Intent(ctx, TestService::class.java)
                intent.action = ACTION_MEASURE_CONFIG_CANCEL
                ctx.startService(intent)
            } catch (e: Exception) {
                App.Companion.log("Failed to send message to test service $e")
            }
        }

        fun testUUID(ctx: Context, uuid: String) {
            try {
                val intent = Intent(ctx, TestService::class.java)
                intent.action = ACTION_MEASURE_CONFIG
                intent.putExtra(EXTRA_UUID, uuid)
                ctx.startService(intent)
            } catch (e: Exception) {
                App.Companion.log("Failed to send message to test service $e")
            }
        }

        fun testActive(ctx: Context, id: String) {
            try {
                val intent = Intent(ctx, TestService::class.java)
                intent.action = ACTION_MEASURE_ACTIVE
                intent.putExtra(EXTRA_ID, id)
                ctx.startService(intent)
            } catch (e: Exception) {
                App.Companion.log("Failed to send message to test service $e")
            }
        }
    }

    private val activeTestScope by lazy {
        CoroutineScope(
            Executors.newFixedThreadPool(
                1
            ).asCoroutineDispatcher()
        )
    }

    private val realTestScope by lazy {
        CoroutineScope(
            Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
            ).asCoroutineDispatcher()
        )
    }


    /**
     * Handles the start command for the service.
     * @param intent The intent.
     * @param flags The flags.
     * @param startId The start ID.
     * @return The start mode.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_MEASURE_ACTIVE -> {
                val id = intent.getStringExtra(EXTRA_ID) ?: ""
                activeTestScope.launch {
                    val result = TestNode.testActive()
                    DatabaseHandler.encodeNodeTestDelayMillis(
                        id,
                        result
                    )
                    sendBroadcastActive(result)
                }
            }

            ACTION_MEASURE_CONFIG -> {
                val uuid = intent.getStringExtra(EXTRA_UUID) ?: ""
                if (uuid.isNotEmpty()) {
                    realTestScope.launch {
                        val result = TestNode.testServer(uuid)
                        val testResultPair = Pair(uuid, result)
                        DatabaseHandler.encodeNodeTestDelayMillis(
                            testResultPair.first,
                            testResultPair.second
                        )
                        sendBroadcast(testResultPair)
                    }
                }
            }

            ACTION_MEASURE_CONFIG_CANCEL -> {
                realTestScope.coroutineContext[Job.Key]?.cancelChildren()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun sendBroadcastActive(message: Long) {
        val intent = Intent(BROADCAST_ACTION)
        intent.putExtra(BROADCAST_TYPE, BROADCAST_TYPE_ACTIVE)
        intent.putExtra(BROADCAST_MESSAGE, message)
        this@TestService.sendBroadcast(intent)
    }

    private fun sendBroadcast(message: Pair<String, Long>) {
        val intent = Intent(BROADCAST_ACTION)
        intent.putExtra(BROADCAST_TYPE, BROADCAST_TYPE_CONFIG)
        intent.putExtra(BROADCAST_MESSAGE, message)
        this@TestService.sendBroadcast(intent)
    }
}