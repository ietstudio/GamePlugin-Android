package com.joycastle.advertise.admob;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.joycastle.gamepluginbase.AdvertiseDelegate;
import com.joycastle.gamepluginbase.SystemUtil;

/**
 * Created by gaoyang on 9/29/16.
 */

public class AMAdvertiseHelper implements AdvertiseDelegate {
    private static final String TAG = "FLAnalyticHelper";

    private static AMAdvertiseHelper instance = new AMAdvertiseHelper();

    private String testDeviceId = null;
    private AdView bannerAd = null;
    private InterstitialAd interstitialAd;
    private BannerAdListener bannerAdListener = null;
    private boolean interstitialAdClicked = false;
    private InterstitialAdListener interstitialAdListener = null;

    public static AMAdvertiseHelper getInstance() {
        return instance;
    }

    private AMAdvertiseHelper() {}

    @Override
    public int showBannerAd(boolean protrait, boolean bottom, BannerAdListener listener) {
        bannerAdListener = listener;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) bannerAd.getLayoutParams();
        layoutParams.gravity = bottom ? Gravity.BOTTOM : Gravity.TOP;
        bannerAd.setLayoutParams(layoutParams);
        bannerAd.setVisibility(View.VISIBLE);
        return bannerAd.getHeight();
    }

    @Override
    public void hideBannerAd() {
        bannerAd.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean isInterstitialAdReady() {
        return interstitialAd.isLoaded();
    }

    @Override
    public boolean showInterstitialAd(InterstitialAdListener listener) {
        if (this.isInterstitialAdReady()) {
            interstitialAd.show();
            interstitialAdListener = listener;
            return true;
        }
        return false;
    }

    @Override
    public boolean isVideoAdReady() {
        Log.i(TAG, "didn't support");
        return false;
    }

    @Override
    public boolean showVideoAd(VideoAdListener listener) {
        Log.i(TAG, "didn't support");
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void init(Application application) {
        Log.i(TAG, "Admob installed");
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(testDeviceId)
                .build();
        interstitialAd.loadAd(adRequest);
    }

    private void requestNewBanner() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(testDeviceId)
                .build();
        bannerAd.loadAd(adRequest);
    }

    @Override
    public void onCreate(Activity activity, Bundle savedInstanceState) {
        String bannerId = SystemUtil.getMetaData(activity, "banner_ad_unit_id");
        String interstitialId = SystemUtil.getMetaData(activity, "interstitial_ad_unit_id");
        testDeviceId = SystemUtil.getMetaData(activity, "admob_test_device_id");

        // init banner ad
        bannerAd = new AdView(activity);
        bannerAd.setAdSize(AdSize.SMART_BANNER);
        bannerAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                requestNewBanner();
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                bannerAdListener.onClick();
            }
        });
        bannerAd.setAdUnitId(bannerId);

        ViewGroup viewGroup = (ViewGroup) activity.findViewById(android.R.id.content);
        AdSize adSize = bannerAd.getAdSize();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(adSize.getWidth(), adSize.getHeight());
        viewGroup.addView(bannerAd, layoutParams);
        bannerAd.setVisibility(View.INVISIBLE);
        requestNewBanner();

        // init interstitial Ad
        interstitialAd = new InterstitialAd(activity);
        interstitialAd.setAdUnitId(interstitialId);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                requestNewInterstitial();
                interstitialAdListener.onResult(interstitialAdClicked);
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                requestNewInterstitial();
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                interstitialAdClicked = true;
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                interstitialAdClicked = false;
            }
        });
        requestNewInterstitial();
    }

    @Override
    public void onStart(Activity activity) {

    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public void onStop(Activity activity) {

    }

    @Override
    public void onDestroy(Activity activity) {

    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

    }
}
