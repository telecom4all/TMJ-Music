package be.telecom4all.tmjmusic

import android.content.Context
import android.util.Log
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class LiveMessageHelper(private val context: Context) {

    // Cette méthode met à jour le message du prochain live
    fun updateNextLiveMessage(
        nextLiveMessageTextView: TextView,
        vacanceActif: String?,
        vacanceDate: String?,
        notificationAndAlarmHelper: NotificationAndAlarmHelper
    ) {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        val dateFormat = SimpleDateFormat("EEEE d MMMM", Locale("fr", "FR"))

        if (vacanceActif?.toBoolean() == true && !vacanceDate.isNullOrEmpty()) {
            nextLiveMessageTextView.text = "TMJ-Music est en vacances jusqu'au $vacanceDate"
            notificationAndAlarmHelper.cancelAllAlarms()
            return
        }

        when (dayOfWeek) {
            Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY, Calendar.MONDAY -> {
                val nextTuesday = getNextWeekday(Calendar.TUESDAY)
                val message = "Prochain live le ${dateFormat.format(nextTuesday.time)} à 17h00"
                nextLiveMessageTextView.text = message
                notificationAndAlarmHelper.scheduleLiveReminder(listOf(Calendar.TUESDAY), 16, 45)
            }
            Calendar.TUESDAY -> {
                if (currentHour < 17) {
                    nextLiveMessageTextView.text = "Prochain live aujourd'hui à 17h00"
                    notificationAndAlarmHelper.scheduleLiveReminder(listOf(Calendar.TUESDAY), 16, 45)
                } else {
                    val nextThursday = getNextWeekday(Calendar.THURSDAY)
                    val message = "Prochain live le ${dateFormat.format(nextThursday.time)} à 17h00"
                    nextLiveMessageTextView.text = message
                    notificationAndAlarmHelper.scheduleLiveReminder(listOf(Calendar.THURSDAY), 16, 45)
                }
            }
            Calendar.WEDNESDAY -> {
                val nextThursday = getNextWeekday(Calendar.THURSDAY)
                val message = "Prochain live le ${dateFormat.format(nextThursday.time)} à 17h00"
                nextLiveMessageTextView.text = message
                notificationAndAlarmHelper.scheduleLiveReminder(listOf(Calendar.THURSDAY), 16, 45)
            }
            Calendar.THURSDAY -> {
                if (currentHour < 17) {
                    nextLiveMessageTextView.text = "Prochain live aujourd'hui à 17h00"
                    notificationAndAlarmHelper.scheduleLiveReminder(listOf(Calendar.THURSDAY), 16, 45)
                } else {
                    val nextTuesday = getNextWeekday(Calendar.TUESDAY)
                    val message = "Prochain live le ${dateFormat.format(nextTuesday.time)} à 17h00"
                    nextLiveMessageTextView.text = message
                    notificationAndAlarmHelper.scheduleLiveReminder(listOf(Calendar.TUESDAY), 16, 45)
                }
            }
            else -> {
                nextLiveMessageTextView.text = "Le prochain live sera bientôt programmé."
            }
        }
    }

/*
    // Cette méthode génère le message du prochain live
    private fun getNextLiveMessage(
        vacanceActif: String?,
        vacanceDate: String?,
        messageActif: String?,
        notificationMessage: String?,
        notificationDate: String?
    ): String {
        var messageProchainLive = "" // Valeur par défaut
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))


        // Log.d("getNextLiveMessage", "getNextLiveMessage called with:")
        // Log.d("getNextLiveMessage", "vacanceActif: $vacanceActif")
        // Log.d("getNextLiveMessage", "vacanceDate: $vacanceDate")
        //  Log.d("getNextLiveMessage", "messageActif: $messageActif")
        // Log.d("getNextLiveMessage", "notificationMessage: $notificationMessage")
        // Log.d("getNextLiveMessage", "notificationDate: $notificationDate")


        // Vérifier si les vacances sont activées
        if (vacanceActif?.toBoolean() == true && !vacanceDate.isNullOrEmpty()) {
            Log.w("vacanceActif?.toBoolean()", "***********************************")
            Log.w("vacanceActif?.toBoolean()", "vacanceActif: $vacanceActif")
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val vacanceEndDate: Date? = vacanceDate?.let { dateString ->
                formatter.parse(dateString)
            }

            vacanceEndDate?.let {
                val vacanceEndCalendar = Calendar.getInstance().apply {
                    time = it
                }

                // Vérifiez si la date actuelle est avant ou le même jour que la fin des vacances
                if (calendar.before(vacanceEndCalendar) || calendar.time == vacanceEndCalendar.time) {
                    messageProchainLive = "TMJ-Music est en vacances jusqu'au ${SimpleDateFormat("EEEE d MMMM y", Locale("fr", "FR")).format(vacanceEndCalendar.time)}"
                }

                Log.w("vacanceActif?.toBoolean()", "messageProchainLive: $messageProchainLive")
                Log.w("vacanceActif?.toBoolean()", "***********************************")
            }
        } else {
            Log.w("vacanceActif?.toBoolean()", "-------------------------------")
            Log.w("vacanceActif?.toBoolean()", "vacanceActif: $vacanceActif")

            val jourDeLaSemaine = calendar.get(Calendar.DAY_OF_WEEK)
            val heure = calendar.get(Calendar.HOUR_OF_DAY)

            // Logique pour déterminer si un live est en cours
            when (jourDeLaSemaine) {

                Calendar.TUESDAY, Calendar.THURSDAY -> {
                    Log.w("vacanceActif?.toBoolean()", "jourDeLaSemaine: $jourDeLaSemaine")
                    //  if (heure in 18..22) {
                    //       messageProchainLive = "Live en cours!"
                    //  } else {
                    val prochainLive = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, if (jourDeLaSemaine == Calendar.TUESDAY) Calendar.THURSDAY else Calendar.TUESDAY)
                        set(Calendar.HOUR_OF_DAY, 18)
                    }
                    if (prochainLive.before(Calendar.getInstance())) {
                        prochainLive.add(Calendar.WEEK_OF_YEAR, 1)
                    }
                    val jourCompletFormat = SimpleDateFormat("EEEE d MMMM y", Locale("fr", "FR"))
                    messageProchainLive = "Prochain live le ${jourCompletFormat.format(prochainLive.time)} à 18h00"

                    // }
                }
                else -> {
                    messageProchainLive = "Pas de live prévu actuellement."
                }
            }
            Log.w("vacanceActif?.toBoolean()", "messageProchainLive: $messageProchainLive")
            Log.w("vacanceActif?.toBoolean()", "-------------------------------")

        }

        // Vérifiez que messageProchainLive n'est pas vide
        if (messageProchainLive.isEmpty()) {
            Log.d("getNextLiveMessage", "Message du prochain live est vide!")
            messageProchainLive = "Le prochain live sera bientôt programmé."
        }

        //Log.d("getNextLiveMessage", "Message du prochain live calculé: $messageProchainLive")
        return messageProchainLive
    }

*/

    // Cette méthode vérifie la disponibilité du flux et met à jour le message du live en conséquence
    fun checkStreamAndUpdateMessage(
        checkStreamAvailability: (callback: (Boolean) -> Unit) -> Unit,
        nextLiveMessageTextView: TextView,
        vacanceActif: String?,
        vacanceDate: String?,
        notificationAndAlarmHelper: NotificationAndAlarmHelper
    ) {
        checkStreamAvailability { available ->
            if (available) {
                nextLiveMessageTextView.post {
                    nextLiveMessageTextView.text = context.getString(R.string.live_en_cours)
                }
            } else {
                nextLiveMessageTextView.post {
                    updateNextLiveMessage(nextLiveMessageTextView, vacanceActif, vacanceDate, notificationAndAlarmHelper)
                }
            }
        }
    }

    private fun getNextWeekday(weekday: Int): Calendar {
        val calendar = Calendar.getInstance()
        while (calendar.get(Calendar.DAY_OF_WEEK) != weekday) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar
    }
}
