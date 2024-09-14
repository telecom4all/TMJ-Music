package be.telecom4all.tmjmusic

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity

class PermissionManager(private val context: Context) {

    private val notificationPermissionCode = 1001
    private val scheduleExactAlarmRequestCode = 1002

    fun checkAndRequestPermissions() {
        Thread {
            // Check and request POST_NOTIFICATIONS permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("PermissionManager", "Notification permission not granted")
                    ActivityCompat.requestPermissions(
                        context as AppCompatActivity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        notificationPermissionCode
                    )
                } else {
                    Log.d("PermissionManager", "Notification permission granted")
                }
            }

            // Check if the app can schedule exact alarms
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.e("PermissionManager", "Permission to schedule exact alarms not granted.")
                    // Open the exact alarm permission settings directly for your app
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    (context as AppCompatActivity).startActivityForResult(intent, scheduleExactAlarmRequestCode)
                } else {
                    Log.d("PermissionManager", "Permission to schedule exact alarms granted.")
                }
            }
        }.start()
    }





    // Gérer la réponse de l'utilisateur à la demande de permission
    // Gérer la réponse de l'utilisateur à la demande de permission
    fun handlePermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == notificationPermissionCode) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.d("PermissionManager", "Notification permission granted by the user.")
            } else {
                Toast.makeText(context, "La permission de notification a été refusée", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
