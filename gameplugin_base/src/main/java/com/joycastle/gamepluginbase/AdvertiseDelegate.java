package com.joycastle.gamepluginbase;

/**
 * Created by gaoyang on 9/29/16.
 */

public interface AdvertiseDelegate extends LifeCycleDelegate {
    /**
     * 显示Banner广告
     * @param protrait
     * @param bottom
     */
    int showBannerAd(boolean protrait, boolean bottom);

    /**
     * 隐藏Banner广告
     */
    void hideBannerAd();

    /**
     * 插屏广告是否准备就绪
     * @return
     */
    boolean isInterstitialAdReady();

    /**
     * 显示插屏广告
     * @param listener
     * @return
     */
    boolean showInterstitialAd(InvokeJavaMethodDelegate listener);

    /**
     * 视频广告准备就绪
     * @return
     */
    boolean isVideoAdReady();

    /**
     * 显示视频广告
     * @param listener
     * @return
     */
    boolean showVideoAd(InvokeJavaMethodDelegate listener);

    /**
     * 名称
     * @return
     */
    String getName();
}
