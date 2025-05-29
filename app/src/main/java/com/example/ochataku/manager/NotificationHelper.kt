package com.example.ochataku.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.ochataku.R
import com.example.ochataku.ui.MainActivity

fun sendSystemNotification(
    context: Context,
    title: String,
    content: String,
    convId: Long,
    peerId: Long,
    peerName: String,
    isGroup: Boolean,
    peerAvatarUrl: String
) {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("open_conv_id", convId)
        putExtra("open_peer_id", peerId)
        putExtra("open_peer_name", peerName)
        putExtra("open_is_group", isGroup)
        putExtra("open_peer_avatar", peerAvatarUrl)
    }

    val pendingIntent = PendingIntent.getActivity(
        context, convId.toInt(), intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val channelId = "chat_channel"
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Chat", NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title) // 通常设置为联系人名
        .setContentText(content) // 消息内容
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setStyle(NotificationCompat.BigTextStyle().bigText(content))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    manager.notify(convId.toInt(), notification)
}
