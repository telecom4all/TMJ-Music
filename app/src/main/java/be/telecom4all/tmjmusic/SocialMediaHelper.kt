package be.telecom4all.tmjmusic

import android.content.Context
import android.content.Intent
import android.net.Uri

class SocialMediaHelper(private val context: Context) {

    fun launchFacebook() {

        val facebookAppUrl = Uri.parse(context.getString(R.string.facebookAppUrl))
        val facebookWebUrl = Uri.parse(context.getString(R.string.facebookWebUrl))
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, facebookAppUrl))
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, facebookWebUrl))
        }
    }

    fun launchWebsite() {
        val websiteUrl = Uri.parse(context.getString(R.string.websiteUrl))
        context.startActivity(Intent(Intent.ACTION_VIEW, websiteUrl))
    }
}
