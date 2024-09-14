package be.telecom4all.tmjmusic.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import be.telecom4all.tmjmusic.MainActivity
import be.telecom4all.tmjmusic.R


class LiveReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("LiveReminderReceiver", "Alarme reçue")

        sendNotification(context)
    }

    private fun sendNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Handle the case where permission is not granted
            return
        }

        val channelId = "live_reminder_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Création du canal de notification
        val channel = NotificationChannel(channelId, "Live Reminder", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)


        // Intent pour ouvrir MainActivity et afficher un message d'alerte
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_message", "Le live commence bientôt à 17h00 !")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Création de la notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_play)
            .setContentTitle("TMJ Music")
            .setContentText("Le live commence bientôt à 17h00 !")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Associer l'intent à la notification
            .build()

        notificationManager.notify(1001, notification)
    }
/*
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("LiveReminderReceiver", "Alarme reçue")

        // Récupérer le message passé dans l'intent
        val message = intent?.getStringExtra("notification_message") ?: "Prochain live bientôt!"

        // Log pour vérification
        Log.w("LiveReminderReceiver", "Message reçu : $message")

        // Envoyer la notification avec le message personnalisé
        sendNotification(context, message)
    }

    // Méthode pour envoyer la notification
    private fun sendNotification(context: Context, message: String) {
        // Obtenir une instance de NotificationManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Définir un ID unique pour la notification
        val notificationId = System.currentTimeMillis().toInt()

        // Créer une intention pour ouvrir l'activité principale lors du clic sur la notification
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_message", message) // Passer le message à MainActivity
        }

        // Créer un PendingIntent pour déclencher l'intent lorsque l'utilisateur clique sur la notification
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,  // Utiliser l'ID de notification pour l'unicité
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Créer la notification avec le message personnalisé
        val notification = NotificationCompat.Builder(context, "default")
            .setSmallIcon(R.drawable.ic_notification) // Assurez-vous de remplacer cela par votre icône
            .setContentTitle("Rappel TMJ Music") // Titre de la notification
            .setContentText(message) // Message de la notification
            .setAutoCancel(true) // Fermer la notification après clic
            .setContentIntent(pendingIntent) // Définir l'intent qui sera lancé lors du clic
            .build()

        // Envoyer la notification
        notificationManager.notify(notificationId, notification)
    }*/
}
