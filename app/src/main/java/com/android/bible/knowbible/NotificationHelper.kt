package com.android.bible.knowbible

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

import java.util.Calendar

object NotificationHelper {

    fun createNotification(context: Context) {
        val calendar = Calendar.getInstance()

        calendar.timeInMillis = System.currentTimeMillis()

        calendar.set(Calendar.HOUR_OF_DAY, 9)
        val firstTime = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 21)
        val secondTime = calendar.timeInMillis

        val currentTime = System.currentTimeMillis()

        when {
            currentTime < firstTime -> calendar.set(Calendar.HOUR_OF_DAY, 9)
            currentTime in (firstTime + 1) until secondTime -> calendar.set(Calendar.HOUR_OF_DAY, 21)
            else -> {
                val nextDay = calendar.get(Calendar.DAY_OF_MONTH) + 1
                calendar.set(Calendar.DAY_OF_MONTH, nextDay)
                calendar.set(Calendar.HOUR_OF_DAY, 9)
            }
        }

        val receiverIntent = Intent(context, NotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(context, 1, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_HALF_DAY, pendingIntent)
    }
}
