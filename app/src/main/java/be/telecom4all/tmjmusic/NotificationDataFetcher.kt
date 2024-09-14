package be.telecom4all.tmjmusic

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class NotificationDataFetcher(private val context: Context) {

    fun fetchNotificationData(callback: (notificationData: Map<String, String?>) -> Unit) {
        Thread {
            val url = context.getString(R.string.notification_data_url)
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e("NotificationDataFetcher", "Erreur lors de la récupération des données JSON : ${e.message}")
                    callback(emptyMap())
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val notificationData = mutableMapOf<String, String?>()

                    response.body?.string()?.let { jsonString ->
                        try {
                            val jsonObject = JSONObject(jsonString)

                            // Extraire les informations de "vacances" et "notification"
                            notificationData["vacanceActif"] = jsonObject.getJSONArray("vacances").getString(0)
                            notificationData["vacanceDate"] = jsonObject.getJSONArray("vacances").getString(1)
                            notificationData["messageActif"] = jsonObject.getJSONArray("notification").getString(0)
                            notificationData["notificationMessage"] = jsonObject.getJSONArray("notification").getString(1)
                            notificationData["notificationDate"] = jsonObject.getJSONArray("notification").getString(2)

                        } catch (e: JSONException) {
                            Log.e("NotificationDataFetcher", "Erreur lors du parsing du JSON : ${e.message}")
                        }
                    }
                    // Appeler le callback sur le thread principal
                    (context as MainActivity).runOnUiThread {
                        callback(notificationData)
                    }
                }
            })
        }.start()
    }
}
