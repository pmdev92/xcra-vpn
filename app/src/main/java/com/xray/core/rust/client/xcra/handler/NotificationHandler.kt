package com.xray.core.rust.client.xcra.handler

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.service.XcraVpnService
import com.xray.core.rust.client.xcra.ui.activity.MainActivity

object NotificationHandler {

    private const val NOTIFICATION_CHANNEL_ID_VPN_SERVICE: String = "Vpn Service"

    private const val SERVICE_NOTIFICATION_ID: Int = 1111

    fun createNotificationChannels(context: Context) {
        val mNotificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID_VPN_SERVICE,
                    NOTIFICATION_CHANNEL_ID_VPN_SERVICE,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }

    private fun getConnectedNotification(
        service: Service,
        message: String?,
    ): Notification {

        val builder = NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID_VPN_SERVICE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }
        builder.setSmallIcon(R.mipmap.ic_launcher)
            .setContentText(message)
            .setOngoing(true)
            .setShowWhen(false)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntentMain(service))
            .addAction(
                android.R.drawable.ic_menu_delete,
                service.getString(R.string.stop),
                pendingIntentStop(service)
            )


        return builder.build()
    }

    fun sendNotificationConnected(
        service: Service,
        message: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            service.startForeground(
                SERVICE_NOTIFICATION_ID,
                getConnectedNotification(
                    service,
                    message
                ),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            service.startForeground(
                SERVICE_NOTIFICATION_ID,
                getConnectedNotification(
                    service,
                    message
                )
            )
        }
    }


    fun pendingIntentMain(service: Service): PendingIntent {
        val flags =
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val startMainIntent = Intent(service, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            service,
            0,
            startMainIntent,
            flags
        )

        return contentPendingIntent;

    }

    fun pendingIntentStop(service: Service): PendingIntent {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val intent = Intent(service, XcraVpnService::class.java)
        intent.action = XcraVpnService.ACTION_STOP
        val stopPendingIntent = PendingIntent.getService(
            service,
            0,
            intent,
            flags
        )
        return stopPendingIntent
    }
}