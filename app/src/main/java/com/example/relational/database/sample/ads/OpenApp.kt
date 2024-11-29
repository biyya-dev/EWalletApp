package com.example.relational.database.sample.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.relational.database.sample.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import org.jetbrains.annotations.NotNull

class OpenApp(private val globalClass: ApplicationClass) : Application.ActivityLifecycleCallbacks,
    LifecycleObserver {

    private val log = "AppOpenManager"

    private var adVisible = false
    private var appOpenAd: AppOpenAd? = null

    private var currentActivity: Activity? = null
    private var isShowingAd = false
    private var myApplication: ApplicationClass? = globalClass
    private var fullScreenContentCallback: FullScreenContentCallback? = null
    private var appOpenLoading: AppOpenLoading? = null

    companion object {
        var COUNTER = 1
        var isInterstitialShown = false
        var isShowingAd = false
    }


    init {
        this.myApplication?.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }


    fun fetchAd() {
        Log.d(log, "fetchAd " + isAdAvailable())

        if (isAdAvailable()) {
            return
        }

        /*
          Called when an app open ad has failed to load.

          @param loadAdError the error.
         */
        // Handle the error.
        val loadCallback: AppOpenAd.AppOpenAdLoadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            /**
             * Called when an app open ad has loaded.
             *
             * @param ad the loaded app open ad.
             */

            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                Log.d(log, "loaded")
            }

            /**
             * Called when an app open ad has failed to load.
             *
             * @param loadAdError the error.
             */
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error.
                Log.d(log, "error")
                println("load appopenerror"+loadAdError.message)
            }
        }
        val request: AdRequest = getAdRequest()
        myApplication?.let {
            AppOpenAd.load(
                it, globalClass.getString(R.string.admob_app_open), request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback
            )
        }
    }

    private fun showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        COUNTER++
        if (!isShowingAd && isAdAvailable() && !isInterstitialShown && !isShowingAd) {
            Log.d(log, "Will show ad.")
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    appOpenAd = null
                    isShowingAd = false
                    adVisible = false
                    fetchAd()
                    dismissLoading(1)
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {

                    dismissLoading(3)
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                }
            }
//            if (COUNTER % 2 == 0) {
            adVisible = true
            showLoading()
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            currentActivity?.let { appOpenAd?.show(it) }
//            } else {
//                dismissLoading(2)
//            }
        } else {
            Log.d(log, "Can not show ad.")
            dismissLoading(4)
            fetchAd()
        }
    }

    private fun showLoading() {
        if (!currentActivity?.isFinishing!!) {
            appOpenLoading = AppOpenLoading(currentActivity!!)
            appOpenLoading?.show()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onAppBackgrounded() {
        //App in background
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onAppForegrounded() {
        currentActivity?.let {
            Handler(Looper.myLooper()!!).postDelayed({
                showAdIfAvailable()
            }, 100)
        }
        // App in foreground
    }

    private fun dismissLoading(from: Int) {
        Log.d("dismiss55", "11$from")
        if (!adVisible)
            appOpenLoading?.dismiss()
    }

    /**
     * Creates and returns ad request.
     */
    @NotNull
    private fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null
    }


    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityStarted(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityResumed(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }
}