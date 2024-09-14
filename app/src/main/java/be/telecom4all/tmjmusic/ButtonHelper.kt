package be.telecom4all.tmjmusic

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.widget.ImageView

class ButtonHelper {

    fun disableButtonTemporarily(button: ImageView) {
        button.isEnabled = false
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)  // Désaturation totale
        button.colorFilter = ColorMatrixColorFilter(matrix)
    }

    fun reEnableButton(button: ImageView, delay: Long) {
        button.postDelayed({
            button.isEnabled = true
            button.clearColorFilter()  // Réinitialiser l'apparence d'origine
        }, delay)
    }

    fun updatePlayPauseIcon(button: ImageView, isPlaying: Boolean) {
        if (isPlaying) {
            button.setImageResource(R.drawable.ic_pause)
        } else {
            button.setImageResource(R.drawable.ic_play)
        }
    }
}
