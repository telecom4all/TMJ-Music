package be.telecom4all.tmjmusic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.PlaybackException


class AudioService : Service() {

    private var exoPlayer: ExoPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d("AudioService", "onStartCommand called with action: $action")

        when (action) {
            ACTION_START -> startPlaying()
            ACTION_PAUSE -> pausePlaying()
            ACTION_STOP -> stopPlaying()
        }

        return START_STICKY
    }

    // Méthode pour envoyer un broadcast avec l'état actuel
    private fun sendPlaybackStateBroadcast() {
        val intent = Intent(ACTION_PLAYBACK_STATE_CHANGED)
        intent.putExtra(EXTRA_IS_PLAYING, isPlaying)
        sendBroadcast(intent)
    }

    private fun startPlaying() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(this).build()
        }

        val mediaItem = MediaItem.fromUri(getString(R.string.audio_flu_url))

        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true

        // Vérifiez et ajustez le volume ici
        exoPlayer?.volume = 1.0f // 1.0f est le volume maximum, 0.0f est le volume minimum

        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    Log.d("AudioService", "Playback completed")
                    stopSelf()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("AudioService", "ExoPlayer error: ${error.message}")
                stopSelf()
            }
        })

        isPlaying = true
        sendPlaybackStateBroadcast() // Envoyer le broadcast
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun pausePlaying() {
        exoPlayer?.pause()
        isPlaying = false
        sendPlaybackStateBroadcast() // Envoyer le broadcast
        stopForeground(STOP_FOREGROUND_DETACH)
    }

    private fun stopPlaying() {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
        isPlaying = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notificationChannelId = "AUDIO_SERVICE_CHANNEL"
        val channel = NotificationChannel(
            notificationChannelId,
            "Audio Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("TMJ Music")
            .setContentText("Lecture en cours...")
            .setSmallIcon(R.drawable.ic_play)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        exoPlayer?.release()
        exoPlayer = null
        super.onDestroy()
    }

    companion object {
        var isPlaying: Boolean = false
            private set

        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_PLAYBACK_STATE_CHANGED = "ACTION_PLAYBACK_STATE_CHANGED"
        const val EXTRA_IS_PLAYING = "EXTRA_IS_PLAYING"
        const val NOTIFICATION_ID = 1
    }
}