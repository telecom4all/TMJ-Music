package be.telecom4all.tmjmusic

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.gms.tasks.Task

class AppUpdateHelper(private val context: Context) {

    fun checkForUpdates() {
        Thread {
            val appUpdateManager = AppUpdateManagerFactory.create(context)

            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    showUpdateDialog(appUpdateInfo)
                } else {
                    notifyUserAboutUpdate()
                }
            }.addOnFailureListener { e ->
                Log.e("AppUpdateHelper", "L'API de mise à jour n'est pas disponible", e)
                notifyUserAboutUpdate()
            }
        }.start()
    }

    private fun showUpdateDialog(appUpdateInfo: AppUpdateInfo) {
        AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setTitle("Mise à jour disponible")
            .setMessage("Une nouvelle version de l'application est disponible. Veuillez mettre à jour pour bénéficier des dernières fonctionnalités.")
            .setPositiveButton("Mettre à jour") { _, _ ->
                val appUpdateManager = AppUpdateManagerFactory.create(context)
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    context as Activity,
                    UPDATE_REQUEST_CODE
                )
            }
            .setNegativeButton("Annuler") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun notifyUserAboutUpdate() {
        AlertDialog.Builder(context)
            .setTitle("Mise à jour disponible")
            .setMessage("Une nouvelle version de l'application est disponible. Veuillez mettre à jour pour bénéficier des dernières fonctionnalités.")
            .setPositiveButton("Mettre à jour") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=be.telecom4all.tmjmusic"))
                context.startActivity(intent)
            }
            .setNegativeButton("Annuler") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    companion object {
        private const val UPDATE_REQUEST_CODE = 1001
    }
}
