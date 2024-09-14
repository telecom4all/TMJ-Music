package be.telecom4all.tmjmusic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import okhttp3.OkHttpClient
import okhttp3.Request

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale





class MainActivity : AppCompatActivity() {

    private lateinit var playPauseButton: ImageView
    private var isPlaying = false
    //private var isStreamAvailable = false

    private val streamUrl: String by lazy { getString(R.string.audio_flu_url) }
    private lateinit var wakeLock: PowerManager.WakeLock
    private var isFacebookButtonEnabled = true
    private var isWebsiteButtonEnabled = true

     // Déclaration des variables globales
    private var messageActif: String? = null
    private var notificationMessage: String? = null
    private var notificationDate: String? = null
    private var vacanceActif: String? = null
    private var vacanceDate: String? = null

    private lateinit var permissionManager: PermissionManager
    private lateinit var notificationAndAlarmHelper: NotificationAndAlarmHelper
    private lateinit var appUpdateHelper: AppUpdateHelper
    private lateinit var notificationDataFetcher: NotificationDataFetcher
    private lateinit var optimizationHelper: OptimizationHelper
    private lateinit var liveMessageHelper: LiveMessageHelper
    private lateinit var nextLiveMessageTextView: TextView
    private lateinit var socialMediaHelper: SocialMediaHelper
    private lateinit var buttonHelper: ButtonHelper

    //var preferences_update: SharedPreferences? = null


    private val playbackStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isPlaying = intent?.getBooleanExtra(AudioService.EXTRA_IS_PLAYING, false) ?: false
            updatePlayPauseIcon()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionManager = PermissionManager(this)
        notificationAndAlarmHelper = NotificationAndAlarmHelper(this)
        appUpdateHelper = AppUpdateHelper(this)
        notificationDataFetcher = NotificationDataFetcher(this)
        optimizationHelper = OptimizationHelper(this)
        liveMessageHelper = LiveMessageHelper(this)
        nextLiveMessageTextView = findViewById(R.id.nextLiveMessage)
        socialMediaHelper = SocialMediaHelper(this)
        buttonHelper = ButtonHelper()

        // Vérifiez si l'activité a été lancée par une notification
        var notificationMessage = intent.getStringExtra("notification_message")
        if (!notificationMessage.isNullOrEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Message de la Notification")
                .setMessage(notificationMessage)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            appUpdateHelper.checkForUpdates()
        }

        val settingsButton = findViewById<ImageView>(R.id.settingsButton)

        // Utiliser PermissionManager pour gérer les permissions
        permissionManager.checkAndRequestPermissions()

        // Planifier les notifications
        //notificationAndAlarmHelper.scheduleLiveReminder(listOf(Calendar.TUESDAY, Calendar.THURSDAY), 16, 45)


        val preferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isFirstLaunch = preferences.getBoolean("isFirstLaunch", true)

        if (isFirstLaunch) {
            optimizationHelper.showOptimizationDialog()
            preferences.edit().putBoolean("isFirstLaunch", false).apply()
        }

        settingsButton.setOnClickListener {
            Log.d("MainActivity", "Settings button clicked")
            preferences.edit().putBoolean("isOptimizationDialogShown", false).apply()
            if (settingsButton.isEnabled) {
                buttonHelper.disableButtonTemporarily(settingsButton)
                runOnUiThread {
                    optimizationHelper.showOptimizationDialog()
                }
                val delay = resources.getInteger(R.integer.button_reenable_delay).toLong()
                buttonHelper.reEnableButton(settingsButton, delay)
            }
        }



        playPauseButton = findViewById(R.id.playPauseButton)

        //val nextLiveMessageTextView = findViewById<TextView>(R.id.nextLiveMessage)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TMJMusic::AudioWakelock")

        playPauseButton.setOnClickListener {
            buttonHelper.disableButtonTemporarily(playPauseButton)
            if (isPlaying) {
                pauseAudio()
            } else {
                checkStreamAvailability { available ->
                    if (available) {
                        playAudio()
                    } else {
                        showStreamUnavailableDialog()
                    }
                }
            }
            val delay = resources.getInteger(R.integer.button_reenable_delay).toLong()
            buttonHelper.reEnableButton(playPauseButton, delay)
        }

        val filter = IntentFilter(AudioService.ACTION_PLAYBACK_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.registerReceiver(this, playbackStateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(playbackStateReceiver, filter)
        }


        notificationDataFetcher.fetchNotificationData { data ->
            vacanceActif = data["vacanceActif"]
            vacanceDate = data["vacanceDate"]
            messageActif = data["messageActif"]
            notificationMessage = data["notificationMessage"]
            notificationDate = data["notificationDate"]

            liveMessageHelper.updateNextLiveMessage(
                nextLiveMessageTextView,
                vacanceActif,
                vacanceDate,
                notificationAndAlarmHelper
            )

            val liveReminderHour = resources.getInteger(R.integer.live_reminder_hour)
            val liveReminderMinute = resources.getInteger(R.integer.live_reminder_minute)


            if (vacanceActif?.toBoolean() == true) {
                notificationAndAlarmHelper.cancelLiveReminders(listOf(Calendar.TUESDAY, Calendar.THURSDAY))
            } else {
                notificationAndAlarmHelper.scheduleLiveReminder(listOf(Calendar.TUESDAY, Calendar.THURSDAY), liveReminderHour, liveReminderMinute)
            }

            val channelId = "default"
            val channelName = "Notification Channel"
            NotificationAndAlarmHelper(this).createNotificationChannel(channelId, channelName)

            // 5. Vérifier et programmer la notification si un message est actif
            val currentNotificationMessage = notificationMessage // Variable locale immuable

            if (messageActif?.toBoolean() == true && !currentNotificationMessage.isNullOrEmpty() && !notificationDate.isNullOrEmpty()) {
                val lastNotificationMessage = getLastNotificationMessage()

                // Ne programmer la notification que si le message a changé
                if (currentNotificationMessage != lastNotificationMessage) {
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    notificationDate?.let { dateString ->
                        val parsedDate = formatter.parse(dateString)
                        if (parsedDate != null) {
                            val notificationHour = resources.getInteger(R.integer.notification_hour)
                            val notificationMinute = resources.getInteger(R.integer.notification_minute)
                            val notificationSecond = resources.getInteger(R.integer.notification_second)
                            val notificationMillisecond = resources.getInteger(R.integer.notification_millisecond)

                            val notificationCalendar = Calendar.getInstance().apply {
                                time = parsedDate
                                set(Calendar.HOUR_OF_DAY, notificationHour)
                                set(Calendar.MINUTE, notificationMinute)
                                set(Calendar.SECOND, notificationSecond)
                                set(Calendar.MILLISECOND, notificationMillisecond)
                            }

                            val now = Calendar.getInstance()
                            if (notificationCalendar.after(now) || now.get(Calendar.DAY_OF_YEAR) == notificationCalendar.get(Calendar.DAY_OF_YEAR)) {
                                notificationAndAlarmHelper.scheduleNotification(notificationCalendar, currentNotificationMessage!!)

                                // Sauvegarder le message dans les préférences une fois la notification programmée
                                saveLastNotificationMessage(currentNotificationMessage)
                            } else {
                                Log.e("Notification", "La date fournie est dans le passé")
                            }
                        } else {
                            Log.e("Notification", "La date fournie ne peut pas être analysée : $dateString")
                        }
                    }
                } else {
                    Log.d("Notification", "Le message de la notification n'a pas changé, aucune notification programmée.")
                }
            }

        }

        val facebookButton = findViewById<ImageView>(R.id.facebookButton)
        facebookButton.setOnClickListener {
            if (isFacebookButtonEnabled) {
                buttonHelper.disableButtonTemporarily(facebookButton)
                socialMediaHelper.launchFacebook()
                val delay = resources.getInteger(R.integer.button_reenable_delay).toLong()
                buttonHelper.reEnableButton(facebookButton, delay)
            }
        }

        val websiteButton = findViewById<ImageView>(R.id.websiteButton)
        websiteButton.setOnClickListener {
            if (isWebsiteButtonEnabled) {
                buttonHelper.disableButtonTemporarily(websiteButton)
                socialMediaHelper.launchWebsite()
                val delay = resources.getInteger(R.integer.button_reenable_delay).toLong()
                buttonHelper.reEnableButton(websiteButton, delay)
            }
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.handlePermissionsResult(requestCode, grantResults)
    }


    override fun onResume() {
        super.onResume()
        // 1. Récupérer l'état de la lecture
        val preferences = getSharedPreferences("audio_preferences", Context.MODE_PRIVATE)
        isPlaying = preferences.getBoolean("is_playing", false) // Récupérer l'état de lecture
        if (isPlaying) {
            checkStreamAvailability { available ->
                if (available) {
                    updatePlayPauseIcon()
                } else {
                    isPlaying = false
                    updatePlayPauseIcon()
                }
            }
        } else {
            updatePlayPauseIcon()
        }


        // 2. Récupérer et afficher le bon message en fonction du stream et des notifications
        notificationDataFetcher.fetchNotificationData { data ->
            vacanceActif = data["vacanceActif"]
            vacanceDate = data["vacanceDate"]
            messageActif = data["messageActif"]
            notificationMessage = data["notificationMessage"]
            notificationDate = data["notificationDate"]

            liveMessageHelper.updateNextLiveMessage(
                nextLiveMessageTextView,
                vacanceActif,
                vacanceDate,
                notificationAndAlarmHelper
            )

            val liveReminderHour = resources.getInteger(R.integer.live_reminder_hour)
            val liveReminderMinute = resources.getInteger(R.integer.live_reminder_minute)

            // 3. Annuler les alarmes existantes avant de les reprogrammer
            notificationAndAlarmHelper.cancelLiveReminders(listOf(Calendar.TUESDAY, Calendar.THURSDAY))

            // 4. Reprogrammer les alarmes en fonction de l'état des vacances
            if (vacanceActif?.toBoolean() == true) {
                notificationAndAlarmHelper.cancelLiveReminders(listOf(Calendar.TUESDAY, Calendar.THURSDAY, Calendar.SATURDAY))
            } else {
                notificationAndAlarmHelper.scheduleLiveReminder(listOf(Calendar.TUESDAY, Calendar.THURSDAY), liveReminderHour, liveReminderMinute)
            }

            // 5. Vérifier et programmer la notification si un message est actif
            val currentNotificationMessage = notificationMessage // Variable locale immuable

            if (messageActif?.toBoolean() == true && !currentNotificationMessage.isNullOrEmpty() && !notificationDate.isNullOrEmpty()) {
                val lastNotificationMessage = getLastNotificationMessage()

                // Ne programmer la notification que si le message a changé
                if (currentNotificationMessage != lastNotificationMessage) {
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    notificationDate?.let { dateString ->
                        val parsedDate = formatter.parse(dateString)
                        if (parsedDate != null) {
                            val notificationHour = resources.getInteger(R.integer.notification_hour)
                            val notificationMinute = resources.getInteger(R.integer.notification_minute)
                            val notificationSecond = resources.getInteger(R.integer.notification_second)
                            val notificationMillisecond = resources.getInteger(R.integer.notification_millisecond)

                            val notificationCalendar = Calendar.getInstance().apply {
                                time = parsedDate
                                set(Calendar.HOUR_OF_DAY, notificationHour)
                                set(Calendar.MINUTE, notificationMinute)
                                set(Calendar.SECOND, notificationSecond)
                                set(Calendar.MILLISECOND, notificationMillisecond)
                            }

                            val now = Calendar.getInstance()
                            if (notificationCalendar.after(now) || now.get(Calendar.DAY_OF_YEAR) == notificationCalendar.get(Calendar.DAY_OF_YEAR)) {
                                notificationAndAlarmHelper.scheduleNotification(notificationCalendar, currentNotificationMessage!!)

                                // Sauvegarder le message dans les préférences une fois la notification programmée
                                saveLastNotificationMessage(currentNotificationMessage)
                            } else {
                                Log.e("Notification", "La date fournie est dans le passé")
                            }
                        } else {
                            Log.e("Notification", "La date fournie ne peut pas être analysée : $dateString")
                        }
                    }
                } else {
                    Log.d("Notification", "Le message de la notification n'a pas changé, aucune notification programmée.")
                }
            }

        }

        // 6. Vérifier si un flux est en cours de lecture et mettre à jour l'affichage du message
        liveMessageHelper.checkStreamAndUpdateMessage(
            ::checkStreamAvailability,
            nextLiveMessageTextView,
            vacanceActif,
            vacanceDate,
            notificationAndAlarmHelper
        )

    }

    private fun updatePlayPauseIcon() {
        buttonHelper.updatePlayPauseIcon(playPauseButton, isPlaying)
    }


    private fun playAudio() {
        Log.d("MainActivity", "playAudio() called")

        val intent = Intent(this, AudioService::class.java)
        intent.action = AudioService.ACTION_START
        startService(intent)


        val delay = resources.getInteger(R.integer.wakeLock_delay).toLong()
        wakeLock.acquire(delay)  // Acquire the wakelock with a 10-minute timeout

        isPlaying = true
        savePlaybackState(isPlaying)  // Sauvegarder l'état de lecture
        updatePlayPauseIcon()
    }

    private fun pauseAudio() {
        Log.d("MainActivity", "pauseAudio() called")

        val intent = Intent(this, AudioService::class.java)
        intent.action = AudioService.ACTION_PAUSE
        startService(intent)

        if (wakeLock.isHeld) {
            wakeLock.release()  // Release the wakelock when pausing audio
        }

        isPlaying = false
        savePlaybackState(isPlaying)  // Sauvegarder l'état de lecture
        updatePlayPauseIcon()
    }

    private fun savePlaybackState(isPlaying: Boolean) {
        val preferences = getSharedPreferences("audio_preferences", Context.MODE_PRIVATE)
        preferences.edit().putBoolean("is_playing", isPlaying).apply()
    }

    private fun showStreamUnavailableDialog() {
        runOnUiThread {
            AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                .setMessage("Le flux n'est pas disponible. Veuillez attendre la prochaine diffusion.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .setCancelable(true)
                .create()
                .show()
        }
    }

    private fun checkStreamAvailability(callback: (Boolean) -> Unit) {
        Thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(streamUrl).head().build()
                val response = client.newCall(request).execute()
                //Log.d("StreamCheck", "Response code: ${response.code}")
                // Seul un code de réponse 200 est considéré comme succès
                callback(response.code == 200)
            } catch (e: IOException) {
                Log.e("StreamCheck", "Error checking stream availability", e)
                callback(false)  // Indiquer explicitement que le flux n'est pas disponible
            }
        }.start()
    }





    override fun onDestroy() {
        super.onDestroy()
        // Libérer le BroadcastReceiver pour éviter les fuites de mémoire
        unregisterReceiver(playbackStateReceiver)
        if (wakeLock.isHeld) {
            wakeLock.release()  // Ensure the wakelock is released when activity is destroyed
        }
    }

    private fun getLastNotificationMessage(): String? {
        val preferences = getSharedPreferences("notification_preferences", Context.MODE_PRIVATE)
        return preferences.getString("last_notification_message", null)
    }

    private fun saveLastNotificationMessage(message: String) {
        val preferences = getSharedPreferences("notification_preferences", Context.MODE_PRIVATE)
        preferences.edit().putString("last_notification_message", message).apply()
    }

}

