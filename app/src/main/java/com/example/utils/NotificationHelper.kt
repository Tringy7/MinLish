package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    private const val DAILY_REMINDER_CHANNEL_ID = "daily_reminder_channel"
    private const val REVIEW_REMINDER_CHANNEL_ID = "review_reminder_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val dailyChannel = NotificationChannel(
                DAILY_REMINDER_CHANNEL_ID,
                "Nhắc học hàng ngày",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Thông báo nhắc nhở bạn học từ vựng mỗi ngày"
            }

            val reviewChannel = NotificationChannel(
                REVIEW_REMINDER_CHANNEL_ID,
                "Nhắc ôn tập từ vựng",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo khi có từ vựng đến hạn cần ôn tập"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(dailyChannel)
            notificationManager.createNotificationChannel(reviewChannel)
        }
    }

    fun showDailyReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, DAILY_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Đã đến giờ học rồi!")
            .setContentText("Dành 5-10 phút để học từ vựng mới hôm nay nhé.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(1001, builder.build())
            } catch (e: SecurityException) {

            }
        }
    }

    fun showReviewReminder(context: Context, count: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, REVIEW_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Đã đến lúc ôn tập!")
            .setContentText("Bạn có $count từ vựng cần ôn tập ngay bây giờ.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(1002, builder.build())
            } catch (e: SecurityException) {
            }
        }
    }
}
