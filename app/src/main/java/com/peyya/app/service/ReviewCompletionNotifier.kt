package com.peyya.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.peyya.app.MainActivity
import com.peyya.app.R

/**
 * 루트 완료 후 별점·후기 작성을 유도하는 로컬 알림.
 */
object ReviewCompletionNotifier {
    const val CHANNEL_ID = "peyya_trip_review"
    private const val NOTIFICATION_ID = 4201
    const val EXTRA_OPEN_REVIEW = "open_review"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "여행 리뷰",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "오늘 루트가 끝나면 별점·후기 작성 알림을 받아요"
            },
        )
    }

    fun notifyTripCompleted(
        context: Context,
        tripTitle: String,
        stopCount: Int,
    ) {
        ensureChannel(context)
        val launch = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_REVIEW, true)
        }
        val pending = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val body = if (stopCount > 0) {
            "${stopCount}곳을 다녀왔어요. 별점과 한 줄 후기를 남겨 주세요."
        } else {
            "별점과 한 줄 후기를 남겨 주세요."
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("오늘 루트가 끝났어요")
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$tripTitle\n$body"),
            )
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }
}
