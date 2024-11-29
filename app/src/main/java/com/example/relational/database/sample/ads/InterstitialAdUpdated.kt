package com.example.relational.database.sample.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.example.relational.database.sample.R
import com.example.relational.database.sample.ads.OpenApp.Companion.isInterstitialShown


open class InterstitialAdUpdated {
    var mInterstitialAd: InterstitialAd? = null

    companion object {
        var intsrid= R.string.InterstisialId
        @Volatile private var instance: InterstitialAdUpdated? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: InterstitialAdUpdated().also { instance = it }
        }
        var count = 0
    }

    fun loadInterstitialAd(context: Context, id:String) {
        mInterstitialAd = null
        context.let {
            InterstitialAd.load(it,id,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(ad: LoadAdError) {
                        count++
                        if (count < 2) loadInterstitialAd(context,id)
                        isInterstitialShown = false
                    }

                    override fun onAdLoaded(ad: InterstitialAd) {
                        mInterstitialAd = ad
                    }
                })
        }
    }

    fun showInterstitialAdNew(activity: Activity) {
        activity.let {
            if (it.isInterstitialLoaded()){
                mInterstitialAd?.show(it)
            }

        }
    }

    fun getInter(): InterstitialAd? {
        return mInterstitialAd
    }

    fun setListener(context: Context, id: String) {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadInterstitialAd(context,id)
                isInterstitialShown = false
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                isInterstitialShown = false
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                isInterstitialShown = true
                mInterstitialAd = null
            }

            override fun onAdImpression() {
                super.onAdImpression()
                isInterstitialShown = true
            }
        }
    }
}