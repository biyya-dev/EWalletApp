package com.example.relational.database.sample.ads

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.example.relational.database.sample.R


class NativeAdsHelper(private val activity: Context) {
    private var nativeAd: NativeAd? = null

    fun setNativeAd(
        shimmerViewContainer: ShimmerFrameLayout? = null,
        frameLayout: FrameLayout,
        layoutId: Int,
        sdIs: String?,
        onFail: ((String?) -> Unit)? = null,
        onLoad: ((NativeAd?) -> Unit)? = null
    ) {

        showShimmer(shimmerViewContainer)

        val builder = AdLoader.Builder(activity, sdIs!!)
        builder.forNativeAd { unifiedNativeAd: NativeAd ->
            if (nativeAd != null) {
                nativeAd!!.destroy()
            }
            nativeAd = unifiedNativeAd
            val adView = (activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                layoutId, null) as NativeAdView
            populateUnifiedNativeAdView(unifiedNativeAd, adView)
            frameLayout.removeAllViews()
            frameLayout.addView(adView)
            onLoad?.invoke(nativeAd!!)
        }
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
        builder.withNativeAdOptions(adOptions)
        val adLoader = builder
            .withAdListener(object : AdListener() {

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.d("Add Load", "onAdLoaded: " )
                    dismissShimmer(shimmerViewContainer)
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                    dismissShimmer(shimmerViewContainer)
//                    Log.d("Add Failed", loadAdError.message + "-ECode " + loadAdError.code)
//                    super.onAdFailedToLoad(loadAdError)
//                    nativeAd?.let { onFail?.invoke(loadAdError.message) }
                }

            })
            .withNativeAdOptions(adOptions)
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateUnifiedNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        val mediaView: MediaView = adView.findViewById(R.id.ad_media)
        adView.mediaView = mediaView
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
        adView.bodyView = adView.findViewById(R.id.ad_body)


        adView.mediaView?.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
        if (adView.mediaView != null) {
        } else {
            adView.mediaView?.visibility = View.GONE
        }

        if (nativeAd.headline != null) (adView.headlineView as TextView).text = nativeAd.headline
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.GONE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as TextView).text = nativeAd.callToAction
        }
        if (nativeAd.icon != null) {
            adView.iconView?.visibility = View.VISIBLE
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
        } else adView.iconView?.visibility = View.GONE
        if (nativeAd.starRating == null) {
            adView.starRatingView?.visibility = View.GONE
        }
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            (adView.bodyView as TextView).text = nativeAd.body
            adView.bodyView?.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)
    }


    fun showShimmer(shimmerViewContainer: ShimmerFrameLayout?) {
        try {
            shimmerViewContainer?.startShimmer()
            shimmerViewContainer?.visibility = View.VISIBLE
        } catch (e: Exception) {
        }
    }

    fun dismissShimmer(shimmerViewContainer: ShimmerFrameLayout?) {
        try {
            shimmerViewContainer?.visibility = View.GONE
            shimmerViewContainer?.stopShimmer()
        } catch (e: Exception) {
        }
    }
    fun loadNativeAds(
        addUnitId: String,
        onLoad: ((NativeAd?) -> Unit)? = null,
        onFail: ((String?) -> Unit)? = null,
    ) {
        val builder =
            AdLoader.Builder(
                activity,
                addUnitId
            )
        // OnUnifiedNativeAdLoadedListener implementation.
        builder.forNativeAd { unifiedNativeAd: NativeAd? ->
            if (nativeAd != null) {
                nativeAd!!.destroy()
            }
            nativeAd = unifiedNativeAd
            nativeAd?.let { onLoad?.invoke(it) }
        }
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions =
            NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                .setVideoOptions(videoOptions).build()
        builder.withNativeAdOptions(adOptions)
        val adLoader = builder
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("Add Failed", loadAdError.message + "-ECode " + loadAdError.code)
                    super.onAdFailedToLoad(loadAdError)
                    onFail?.invoke(loadAdError.message)
                }
            })
            .withNativeAdOptions(adOptions)
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun setLoadedNativeAd(
        frameLayout: FrameLayout,
        layoutId: Int,
        nativeAd: NativeAd,
        shimmerFrameLayout: ShimmerFrameLayout? = null,
    ) {
        val adView =
            (activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                layoutId,
                null
            ) as NativeAdView
        populateUnifiedNativeAdView(nativeAd, adView)
        frameLayout.removeAllViews()
        frameLayout.addView(adView)
        shimmerFrameLayout?.stopShimmer()
        shimmerFrameLayout?.visibility = View.GONE
    }
}