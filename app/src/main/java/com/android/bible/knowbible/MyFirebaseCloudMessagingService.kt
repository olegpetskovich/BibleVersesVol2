package com.android.bible.knowbible

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_VIBRATE
import com.android.bible.knowbible.mvvm.view.activity.MainActivity
import com.android.bible.knowbible.utility.Utility
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseCloudMessagingService : FirebaseMessagingService() {
    private val notificationChannelId = "channel_id"

    //Метод для обработки приходящих уведомлений
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        //Перейти по ссылке с объяснением, если уведомления не приходят: https://goo.gl/39bRNJ
        Utility.log("From: " + remoteMessage.from)

        //Проверка пришедших данных на содержимость уведомления на наличие данных
        if (remoteMessage.data.isNotEmpty()) {
            Utility.log("Message data payload: " + remoteMessage.data)

            //Этот фрагмент кода был в документации.
            //Он нужен в случае, если приходят данные, на обработку которых требуется много времени,
            //и чтобы в случае чего вынести их в побочный поток. Закомментирую его, вдруг понадобиться в будущем
//            if (/* Check if data needs to be processed by long running job */ true) {
//                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
//                scheduleJob();
//            } else {
//                // Handle message within 10 seconds
//                handleNow();
//            }

        }

        //Проверка полученного уведомления из объекта data на наличие данных
        if (remoteMessage.data.isEmpty()) {
            if (remoteMessage.notification != null) {
                Utility.log("Message Notification Body: " + remoteMessage.notification!!.body)

                if (remoteMessage.notification!!.title != null && remoteMessage.notification!!.body != null) {
                    makeNotification(getNotification(remoteMessage.notification!!.title!!, remoteMessage.notification!!.body!!))
//            sendNotification(remoteMessage.notification!!)
                }
            }
        } else makeNotification(getNotification(remoteMessage.data))

    }

    //Метод взят по этой ссылке, если здесь что-то непонятно, то там всё описано: https://gist.github.com/jirawatee/85d4b46a89b9ae821b63c31f5d5189de
//    private fun sendNotification(notification: RemoteMessage.Notification, data: Map<String, String>) { //Это на случай уведомления с картинкой
    private fun sendNotification(notification: RemoteMessage.Notification) {
        val icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_app_icon)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, notificationChannelId)
                .setContentTitle(notification.title)
                .setContentText(notification.body)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentInfo(notification.title)
                .setLargeIcon(icon)
                .setColor(Color.WHITE)
                .setLights(Color.WHITE, 1000, 300)
                .setDefaults(DEFAULT_VIBRATE)
                .setSmallIcon(R.mipmap.ic_app_icon)
        //Фрагмент кода для добавления картинок в уведомление
//        try {
//            val picture_url = data["picture_url"]
//            if (picture_url != null && "" != picture_url) {
//                val url = URL(picture_url)
//                val bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream())
//                notificationBuilder.setStyle(
//                        NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(notification.body)
//                )
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Notification Channel is required for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(notificationChannelId, "channel_name", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "channel description"
            channel.setShowBadge(true)
            channel.canShowBadge()
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun getNotification(notificationTitle: String, notificationBody: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            NotificationCompat.Builder(this, notificationChannelId)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationBody)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
                    .setSmallIcon(R.mipmap.ic_app_icon)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(longArrayOf(200, 200, 200, 200))
                    .setDefaults(NotificationCompat.DEFAULT_SOUND)
                    .build()
        } else NotificationCompat.Builder(this, notificationChannelId)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
                .setSmallIcon(R.mipmap.ic_app_icon)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .build()
    }

    //Метод для отображения уведомления в фоне, но пока непонятно, так ли это
    private fun getNotification(data: Map<String, String>): Notification {
        val notificationTitle = data["title"].toString()
        val notificationBody = data["body"].toString()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            NotificationCompat.Builder(this, notificationChannelId)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationBody)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
                    .setSmallIcon(R.mipmap.ic_app_icon)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(longArrayOf(200, 200, 200, 200))
                    .setDefaults(NotificationCompat.DEFAULT_SOUND)
                    .build()
        } else NotificationCompat.Builder(this, notificationChannelId)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
                .setSmallIcon(R.mipmap.ic_app_icon)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .build()
    }

    private fun makeNotification(notification: Notification) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(notificationChannelId, "Channel Name", NotificationManager.IMPORTANCE_HIGH)
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            manager.createNotificationChannel(channel)
        }
        manager.notify(123, notification)
    }

    //Метод необходим в случае, если нужно отправлять уведомления для конкретных девайсов.
    //Метод генерирует уникальный токен для каждого девайся (Насколько я понимаю)
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }
}