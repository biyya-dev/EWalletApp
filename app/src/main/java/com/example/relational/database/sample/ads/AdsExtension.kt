package com.example.relational.database.sample.ads

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.example.relational.database.sample.R

import com.facebook.shimmer.ShimmerFrameLayout

private var counter = 0
private var counterList = 0
var ids= R.string.InterstisialId


fun Activity.showInterstitial(id: String) {
        InterstitialAdUpdated.getInstance().showInterstitialAdNew(this)
    InterListener(id)
}

fun Activity.isInterstitialLoaded(): Boolean {
    return InterstitialAdUpdated.getInstance().getInter() != null
}

fun Activity.InterListener(id:String) {
    InterstitialAdUpdated.getInstance().setListener(this, id)
}

//For Fragment
fun Fragment.showInterstitial() {
    InterstitialAdUpdated.getInstance().showInterstitialAdNew(requireActivity())
}

fun Fragment.isInterstitialLoaded(): Boolean {
    return InterstitialAdUpdated.getInstance().getInter() != null
}


fun Context.loadNativeAd(shimmerViewContainer: ShimmerFrameLayout? = null,
                         frameLayout: FrameLayout,
                         layoutId: Int,
                         sdIs: String?) {
        NativeAdsHelper(this).setNativeAd(shimmerViewContainer, frameLayout, layoutId, sdIs)
}
fun Context.loadNativeAd(adView: View,
                         shimmerViewContainer: ShimmerFrameLayout? = null,
                         frameLayout: FrameLayout,
                         layoutId: Int,
                         sdIs: String?) {
    NativeAdsHelper(this).setNativeAd(shimmerViewContainer, frameLayout, layoutId, sdIs)

}