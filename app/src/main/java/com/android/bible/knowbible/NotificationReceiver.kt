package com.android.bible.knowbible

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.android.bible.knowbible.mvvm.view.activity.MainActivity

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val repeatingIntent = Intent(context, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(context, 1, repeatingIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, "1")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("Стих дня")
                .setContentText("Не забудь найти свой стих дня!")
                .setVibrate(longArrayOf(200, 200, 200, 200))
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }
}
