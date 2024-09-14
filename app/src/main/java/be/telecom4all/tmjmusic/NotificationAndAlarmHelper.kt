package be.telecom4all.tmjmusic

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import be.telecom4all.tmjmusic.receivers.LiveReminderReceiver
import be.telecom4all.tmjmusic.receivers.NotificationReceiver
import java.util.Calendar

class NotificationAndAlarmHelper(private val context: Context) {

    fun createNotificationChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Thread {
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }.start()
        }
    }


    fun scheduleNotification(notificationTime: Calendar, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notification_message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.w("scheduleNotification", "Scheduling notification at ${notificationTime.time}")

        try {
            Thread {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationTime.timeInMillis, pendingIntent)
                Log.w("scheduleNotification", "Notification programmée pour le ${notificationTime.time}")
            }.start()
        } catch (e: SecurityException) {
            Log.e("NotificationAndAlarmHelper", "SCHEDULE_EXACT_ALARM permission is required to schedule exact alarms.")
            Toast.makeText(context, "L'application nécessite la permission pour programmer des alarmes exactes.", Toast.LENGTH_LONG).show()
        }
    }

    fun scheduleLiveReminder(daysOfWeek: List<Int>, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        daysOfWeek.forEachIndexed { index, day ->
            val alarmIntent = Intent(context, LiveReminderReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(
                    context,
                    index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.DAY_OF_WEEK, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (before(Calendar.getInstance())) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            Thread {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    alarmIntent
                )
                Log.w("NotificationAndAlarmHelper", "Alarme programmée pour le jour: $day à ${hour}h${minute}")
            }.start()
        }
    }

    fun cancelLiveReminders(daysOfWeek: List<Int>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        daysOfWeek.forEachIndexed { index, _ ->
            val alarmIntent = Intent(context, LiveReminderReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(
                    context,
                    index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            Thread {
                alarmManager.cancel(alarmIntent)
                Log.d("NotificationAndAlarmHelper", "Alarme annulée pour le rappel live: index $index")
            }.start()
        }
    }

    fun getNextWeekday(weekday: Int): Calendar {
        val calendar = Calendar.getInstance()
        while (calendar.get(Calendar.DAY_OF_WEEK) != weekday) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar
    }

    fun cancelAllAlarms() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val daysOfWeek = listOf(Calendar.TUESDAY, Calendar.THURSDAY, Calendar.SATURDAY)

        for (index in daysOfWeek.indices) {
            val alarmIntent = Intent(context, LiveReminderReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(
                    context,
                    index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            Thread {
                alarmManager.cancel(alarmIntent)
                Log.d("NotificationAndAlarmHelper", "Alarme annulée pour le jour index : $index")
            }.start()
        }
    }

}
