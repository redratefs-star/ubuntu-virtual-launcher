package com.uvl.launcher

import android.content.Context
import android.content.Intent
import android.os.Build

object ServerEngine {

    const val PORT = 8765

    @Volatile
    var isRunning: Boolean = false
        internal set

    @Volatile
    var lastError: String = "Belum dijalankan"
        internal set

    fun start(context: Context) {
        val intent = Intent(
            context,
            UVLServerService::class.java
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            lastError = e.message ?: "Gagal memulai server"
        }
    }

    fun stop(context: Context) {
        val intent = Intent(
            context,
            UVLServerService::class.java
        )

        context.stopService(intent)
    }

    fun getAddress(): String {
        return "http://127.0.0.1:$PORT"
    }
}
