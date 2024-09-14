
package be.telecom4all.tmjmusic

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class OptimizationHelper(private val context: Context) {

    fun openBatteryOptimizationSettings() {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Impossible d'ouvrir les paramètres d'optimisation de la batterie", Toast.LENGTH_SHORT).show()
        }
    }

    fun showOptimizationDialog() {
        val preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isDialogShown = preferences.getBoolean("isOptimizationDialogShown", false)

        Log.d("OptimizationHelper", "showOptimizationDialog called, isDialogShown: $isDialogShown")

        if (!isDialogShown) {
            val message = """
                Si vous remarquez que le son de l'application se coupe tout seul, essayez de suivre les étapes ci-dessous :

                1. Désactiver l'optimisation de la batterie pour TMJ Music :
                   Allez dans Paramètres > Batterie et performance > Choisir des applications, sélectionnez TMJ Music, et réglez-la sur "Aucune restriction". Cela empêchera de fermer l'application lorsqu'elle est en arrière-plan.

                2. Activer le démarrage automatique :
                   Allez dans Paramètres > Applications > Permissions > Démarrage automatique, et activez cette option pour l'application. Cela garantit que l'application continue de fonctionner en arrière-plan sans être arrêtée par le système.

                3. Vérifier les paramètres de MIUI :
                   Allez dans Paramètres > Batterie et performance > Utilisation de l'énergie et vérifiez que l'application n'est pas configurée pour être arrêtée lorsque l'écran est éteint ou après une certaine période d'inactivité.
            """.trimIndent()

            AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
                .setTitle("Astuces pour éviter les coupures de son")
                .setMessage(message)
                .setPositiveButton("Paramètres") { _, _ ->
                    openBatteryOptimizationSettings()
                    preferences.edit().putBoolean("isOptimizationDialogShown", true).apply()
                }
                .setNegativeButton("OK") { dialog, _ ->
                    preferences.edit().putBoolean("isOptimizationDialogShown", true).apply()
                    dialog.dismiss()
                }
                .show()
        }
    }
}
