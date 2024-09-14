package be.telecom4all.tmjmusic.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.app.PendingIntent
import android.util.Log
import be.telecom4all.tmjmusic.MainActivity
import be.telecom4all.tmjmusic.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            Log.w("NotificationReceiver", "onReceive triggered")
            val message = intent?.getStringExtra("notification_message") ?: "Nouveau message!"

            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val notificationId = System.currentTimeMillis().toInt()

            val notificationIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("notification_message", message)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                102,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            Log.w("NotificationReceiver", "message: $message")
            val notification = NotificationCompat.Builder(context!!, "default")
                .setContentTitle("TMJ Music")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification) // Remplacez par votre propre icône
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager?.notify(notificationId, notification)
        } else {
            Log.e("NotificationReceiver", "Context is null")
        }
    }
}