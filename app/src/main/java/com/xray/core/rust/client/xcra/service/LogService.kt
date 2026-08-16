package com.xray.core.rust.client.xcra.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.tencent.mmkv.MMKV
import com.xray.core.rust.client.xcra.dto.LogEntry
import com.xray.core.rust.client.xcra.util.JsonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch


class LogService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val logChannel = Channel<Pair<String, String>>(Channel.UNLIMITED)
    private val storageLogs by lazy { MMKV.mmkvWithID(ID_LOGS, MMKV.MULTI_PROCESS_MODE) }
    private val storageMeta by lazy { MMKV.mmkvWithID(ID_META, MMKV.MULTI_PROCESS_MODE) }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            for (log in logChannel) {
                saveLog(log)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val tag = it.getStringExtra("tag") ?: "APP"
            val msg = it.getStringExtra("message") ?: ""

            serviceScope.launch { logChannel.send(tag to msg) }
        }
        return START_STICKY
    }

    private fun saveLog(log: Pair<String, String>) {

        val logCounter = storageMeta.decodeLong(KEY_COUNTER, 0L) + 1
        val log = LogEntry(logCounter, System.currentTimeMillis(), log.first, log.second)
        storageMeta.encode(KEY_COUNTER, logCounter)
        val json = JsonUtil.toJson(log)
        val key = logCounter.toString()
        storageLogs.encode(key, json)
        val allKeys = storageLogs.allKeys()?.map { it.toLong() }?.sorted() ?: emptyList()
        if (allKeys.size > LOG_LIMIT) {
            val excess = allKeys.size - LOG_LIMIT
            allKeys.take(excess).forEach { storageLogs.remove(it.toString()) }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val LOG_LIMIT = 500
        const val ID_LOGS = "log_id_logs"
        const val ID_META = "log_id_meta"
        const val KEY_COUNTER = "log_key_counter"

        fun log(context: Context, tag: String, message: String) {
            val intent = Intent(context, LogService::class.java).apply {
                putExtra("tag", tag)
                putExtra("message", message)
            }
            context.startService(intent)
        }

        fun getLogs(
            startAfter: LogEntry?,
            batchSize: Int = 100,
            search: String
        ): Pair<Boolean, List<LogEntry>> {

            val mmkv = MMKV.mmkvWithID(ID_LOGS, MMKV.MULTI_PROCESS_MODE)

            val keys = mmkv.allKeys()?.mapNotNull { it.toLongOrNull() }
                ?.sortedDescending() ?: emptyList()

            val startIndex = startAfter?.let { entry ->
                keys.indexOf(entry.id).takeIf { it >= 0 }?.plus(1) ?: 0
            } ?: 0

            val result = mutableListOf<LogEntry>()
            var currentIndex = startIndex

            while (currentIndex < keys.size && result.size < batchSize) {
                val keyStr = keys[currentIndex].toString()
                val log = mmkv.decodeString(keyStr)?.let {
                    try {
                        JsonUtil.fromJson(it, LogEntry::class.java)
                    } catch (_: Exception) {
                        mmkv.remove(keyStr)
                        null
                    }
                }

                if (log != null) {
                    val displayText = log.display()

                    if (search.isEmpty() || displayText.contains(search, ignoreCase = true)) {
                        result.add(log)
                    }
                }
                currentIndex++
            }
            val isEnd = currentIndex >= keys.size
            return Pair(isEnd, result)
        }

        fun deleteAll() {
            MMKV.mmkvWithID(ID_LOGS, MMKV.MULTI_PROCESS_MODE).clear()
            MMKV.mmkvWithID(ID_META, MMKV.MULTI_PROCESS_MODE).clear()
        }
    }
}