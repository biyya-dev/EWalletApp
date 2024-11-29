package com.example.relational.database.sample.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.relational.database.sample.BuildConfig
import com.example.relational.database.sample.R


fun Context.shareApp(context: Context) {
    val myIntent= Intent()
    myIntent.action = Intent.ACTION_SEND;
    myIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
    myIntent.putExtra(
        Intent.EXTRA_TEXT,"https://play.google.com/store/apps/details?id="+
            BuildConfig.APPLICATION_ID)
    myIntent.type = "text/plain"
    startActivity(myIntent)
}

fun myEmailFeedback(context: Context, appVersionName: String = "") {

    val to = "appscoder088@gmail.com"
    val subject = "User Feedback for Card Wallet $appVersionName"
    val body = "My feedback: "

    val myUriBuilder = StringBuilder("mailto:" + Uri.encode(to))
    myUriBuilder.append("?subject=" + Uri.encode(subject))
    myUriBuilder.append("&body=" + Uri.encode(body))
    val uriString = myUriBuilder.toString()

    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(uriString))

    try {
        ContextCompat.startActivity(context, intent, null)
    } catch (e: Exception) {
        Log.e("LOG", e.localizedMessage)

        if (e is ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No application can handle this request. Please install an email app.",
                Toast.LENGTH_LONG).show()
        }
    }
}
