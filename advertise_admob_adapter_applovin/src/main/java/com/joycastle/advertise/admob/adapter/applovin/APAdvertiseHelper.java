package com.joycastle.advertise.admob.adapter.applovin;import android.app.Activity;import android.app.Application;import android.content.Intent;import android.os.Bundle;import com.joycastle.gamepluginbase.AdvertiseDelegate;import com.joycastle.gamepluginbase.InvokeJavaMethodDelegate;/** * Created by joye on 2018/1/17. */public class APAdvertiseHelper implements AdvertiseDelegate {    private static final String TAG = "APAdvertiseHelper";    private static APAdvertiseHelper instance = new APAdvertiseHelper();    public static APAdvertiseHelper getInstance() {        return instance;    }    private APAdvertiseHelper() {}    @Override    public int showBannerAd(boolean protrait, boolean bottom, BannerAdListener listener) {        return 0;    }    @Override    public void hideBannerAd() {    }    @Override    public boolean isInterstitialAdReady() {        return false;    }    @Override    public boolean showInterstitialAd(InterstitialAdListener listener) {        return false;    }    @Override    public boolean isVideoAdReady() {        return false;    }    @Override    public boolean showVideoAd(InvokeJavaMethodDelegate listener) {        return false;    }    @Override    public String getName() {        return null;    }    @Override    public void init(Application application) {    }    @Override    public void onCreate(Activity activity, Bundle savedInstanceState) {    }    @Override    public void onStart(Activity activity) {    }    @Override    public void onResume(Activity activity) {    }    @Override    public void onPause(Activity activity) {    }    @Override    public void onStop(Activity activity) {    }    @Override    public void onDestroy(Activity activity) {    }    @Override    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {    }}