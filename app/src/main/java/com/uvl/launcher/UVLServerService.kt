package com.uvl.launcher

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import fi.iki.elonen.NanoHTTPD

class UVLServerService : Service() {

    private var server: UVLHttpServer? = null

    companion object {
        private const val CHANNEL_ID = "uvl_server_channel"
        private const val NOTIFICATION_ID = 8765
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )

        if (server != null) {
            return START_STICKY
        }

        try {
            ServerEngine.lastError = "Menyalakan server..."

            val newServer = UVLHttpServer(
                ServerEngine.PORT
            )

            newServer.start(
                NanoHTTPD.SOCKET_READ_TIMEOUT,
                false
            )

            server = newServer

            ServerEngine.isRunning = true
            ServerEngine.lastError = "Server aktif"

        } catch (e: Exception) {

            ServerEngine.isRunning = false
            ServerEngine.lastError =
                e.message ?: "Server gagal dijalankan"

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        try {
            server?.stop()
        } catch (_: Exception) {
        }

        server = null

        ServerEngine.isRunning = false
        ServerEngine.lastError = "Server berhenti"

        stopForeground(STOP_FOREGROUND_REMOVE)

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Virtus Server",
                NotificationManager.IMPORTANCE_LOW
            )

            channel.description =
                "Status UVL Server Engine"

            val manager =
                getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Virtus OS Server")
                .setContentText("UVL Server Engine sedang berjalan")
                .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                .setOngoing(true)
                .build()

        } else {

            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Virtus OS Server")
                .setContentText("UVL Server Engine sedang berjalan")
                .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                .setOngoing(true)
                .build()
        }
    }
}

class UVLHttpServer(port: Int) :
    NanoHTTPD("127.0.0.1", port) {

    override fun serve(
        session: IHTTPSession
    ): Response {

        return when (session.uri) {

            "/" -> {
                newFixedLengthResponse(
                    Response.Status.OK,
                    MIME_HTML,
                    """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport"
                              content="width=device-width, initial-scale=1">
                        <title>Virtus OS</title>
                    </head>
                    <body style="
                        background:#080c17;
                        color:white;
                        font-family:Arial;
                        text-align:center;
                        padding:40px;">
                        <h1>VIRTUS OS</h1>
                        <p>UVL Server Engine</p>
                        <p>Server berhasil berjalan!</p>
                    </body>
                    </html>
                    """.trimIndent()
                )
            }

            "/health" -> {
                newFixedLengthResponse(
                    Response.Status.OK,
                    MIME_PLAINTEXT,
                    "VIRTUS_SERVER_ONLINE"
                )
            }

            "/api/status" -> {
                newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    """
                    {
                        "name": "Virtus OS",
                        "engine": "UVL Server Engine",
                        "status": "running",
                        "mode": "local",
                        "linux": "not_installed",
                        "game_center": "android"
                    }
                    """.trimIndent()
                )
            }

            "/api/apps" -> {
                newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    """
                    {
                        "apps": [
                            {
                                "name": "Android Game Center",
                                "status": "available"
                            },
                            {
                                "name": "Ubuntu Desktop",
                                "status": "not_installed"
                            },
                            {
                                "name": "Cloud PC",
                                "status": "not_connected"
                            }
                        ]
                    }
                    """.trimIndent()
                )
            }

            else -> {
                newFixedLengthResponse(
                    Response.Status.NOT_FOUND,
                    MIME_PLAINTEXT,
                    "404 - Virtus endpoint not found"
                )
            }
        }
    }
    }
