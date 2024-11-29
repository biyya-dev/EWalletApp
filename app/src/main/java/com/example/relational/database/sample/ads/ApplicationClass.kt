package com.example.relational.database.sample.ads

import android.app.Application
import com.google.android.gms.ads.MobileAds

class ApplicationClass : Application() {
    var appOpenAd: OpenApp? = null
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
        appOpenAd = OpenApp(this)
       // InterstitialAdUpdated.getInstance().loadInterstitialAd(this, getString(R.string.InterstisialId))
    }

    override fun onTerminate() {
        appOpenAd = null
        super.onTerminate()
    }
}