package com.joycastle.iab.googleplay;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.joycastle.gamepluginbase.InvokeJavaMethodDelegate;
import com.joycastle.gamepluginbase.LifeCycleDelegate;
import com.joycastle.gamepluginbase.SystemUtil;
import com.joycastle.iab.googleplay.util.IabBroadcastReceiver;
import com.joycastle.iab.googleplay.util.IabHelper;
import com.joycastle.iab.googleplay.util.IabResult;
import com.joycastle.iab.googleplay.util.Inventory;
import com.joycastle.iab.googleplay.util.Purchase;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gaoyang on 10/9/16.
 */

public class GoogleIabHelper implements LifeCycleDelegate, IabBroadcastReceiver.IabBroadcastListener, IabHelper.QueryInventoryFinishedListener {
    private static String TAG = "GoogleIabHelper";
    private static GoogleIabHelper instance = new GoogleIabHelper();

    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private IabHelper mHelper;
    private IabBroadcastReceiver mBroadcastReceiver;
    private String mVerifyUrl;
    private String mVerifySign;
    private SharedPreferences mSharedPreferences;
    private boolean mCanDoIap = false;
    private Activity mActivity;

    public static GoogleIabHelper getInstance() { return instance; }

    private GoogleIabHelper() {

    }

    @Override
    public void init(Application application) {
        mSharedPreferences = application.getSharedPreferences("test", application.MODE_PRIVATE);
    }

    @Override
    public void onCreate(final Activity activity, Bundle savedInstanceState) {
        String base64PublicKey = SystemUtil.getInstance().getPlatCfgValue("google_iab_publickey");
        mActivity = activity;
        mHelper = new IabHelper(activity, base64PublicKey);
        mHelper.enableDebugLogging(true);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.e(TAG, "Problem setting up In-app Billing: " + result);
                    return;
                }
                if (mHelper == null) {
                    return;
                }
                mCanDoIap = true;

                mBroadcastReceiver = new IabBroadcastReceiver(GoogleIabHelper.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                activity.registerReceiver(mBroadcastReceiver, broadcastFilter);

                quertInventory();
            }
        });
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
        if (mBroadcastReceiver != null) {
            activity.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        mHelper.handleActivityResult(requestCode, resultCode, data);
    }

    private void quertInventory() {
        if (mHelper == null) {
            return;
        }
        try {
            mHelper.queryInventoryAsync(this);
        } catch (IabHelper.IabAsyncInProgressException e) {
            Log.w(TAG, "Error querying inventory. Another async operation in progress.");
        }
    }

    @Override
    public void receivedBroadcast() {
        quertInventory();
    }

    @Override
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
        if (mHelper == null) {
            return;
        }
        if (result.isFailure()) {
            Log.w(TAG, "Failed to query inventory: "+result);
            return;
        }

        Log.d(TAG, "Query inventory was successful.");

        List<Purchase> purchases = inventory.getAllPurchases();
        if (purchases.size() <= 0)
            return;

        HashMap suspensiveIap = getSuspensiveIap();
        if (!suspensiveIap.isEmpty())
            return;

        // 只处理一个，下次在处理剩余的
        final Purchase purchase = purchases.get(0);
        verifyIap(purchase, new InvokeJavaMethodDelegate() {
            @Override
            public void onFinish(final ArrayList<Object> resArrayList) {
                try {
                    mHelper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
                        @Override
                        public void onConsumeFinished(Purchase purchase, IabResult result) {
                            if (result.isFailure()) {
                                return;
                            }
                            boolean payResult = (boolean) resArrayList.get(0);
                            if (!payResult) {
                                return;
                            }
                            String environment = (String) resArrayList.get(1);
                            HashMap hashMap = new HashMap();
                            hashMap.put("productId", purchase.getSku());
                            hashMap.put("environment", environment);
                            setSuspensiveIap(hashMap);
                        }
                    });
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.w(TAG, "Error comsume purchases. Another async operation in progress.");
                }
            }
        });
    }

    public void setIapVerifyUrlAndSign(String url, String sign) {
        this.mVerifyUrl = url;
        this.mVerifySign = sign;
    }

    public boolean canDoIap() {
        if(!mCanDoIap){
            SystemUtil.getInstance().showAlertDialog("Failure", "Can not make payment.", "OK", "", new InvokeJavaMethodDelegate() {
                @Override
                public void onFinish(ArrayList<Object> resArrayList) {
                    
                }
            });
        }
        return mCanDoIap;
    }

    public HashMap getSuspensiveIap() {
        HashMap hashMap = new HashMap<>();
        try {
            String jsonStr = mSharedPreferences.getString("suspensiveIap","{}");
            JSONObject iapinfo = new JSONObject(jsonStr);
            Iterator<String> keys = iapinfo.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                hashMap.put(key, iapinfo.getString(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hashMap;
    }

    public void setSuspensiveIap(HashMap iapInfo) {
        try {
            JSONObject jsonObject = new JSONObject();
            Iterator it = iapInfo.keySet().iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
                Object val = iapInfo.get(key);
                jsonObject.put(key, val);
            }
            //得到SharedPreferences.Editor对象，并保存数据到该对象中
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("suspensiveIap", jsonObject.toString());
            //保存key-value对到文件中
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doIap(String iapId, String userId, final InvokeJavaMethodDelegate delegate) {
        if (!canDoIap()) {
            ArrayList respon = new ArrayList();
            respon.add(false);
            respon.add("can not make payment.");
            delegate.onFinish(respon);
            return;
        }

        try {
            SystemUtil.getInstance().showLoading("Loading...");
            mHelper.launchPurchaseFlow(mActivity, iapId, 10001, new IabHelper.OnIabPurchaseFinishedListener() {
                @Override
                public void onIabPurchaseFinished(IabResult result, final Purchase info) {
                    if (result.isFailure()) {
                        SystemUtil.getInstance().hideLoading();
                        ArrayList respon = new ArrayList();
                        respon.add(false);
                        respon.add("purchase failed.");
                        delegate.onFinish(respon);
                        return;
                    }
                    verifyIap(info, new InvokeJavaMethodDelegate() {
                        @Override
                        public void onFinish(final ArrayList<Object> resArrayList) {
                            try {
                                mHelper.consumeAsync(info, new IabHelper.OnConsumeFinishedListener() {
                                    @Override
                                    public void onConsumeFinished(Purchase purchase, IabResult result) {
                                        SystemUtil.getInstance().hideLoading();
                                        if (result.isFailure()) {
                                            ArrayList respon = new ArrayList();
                                            respon.add(false);
                                            respon.add("consume failed.");
                                            delegate.onFinish(respon);
                                            return;
                                        }
                                        delegate.onFinish(resArrayList);
                                    }
                                });
                            } catch (IabHelper.IabAsyncInProgressException e) {
                                Log.w(TAG, "Error comsume purchases. Another async operation in progress.");
                            }
                        }
                    });
                }
            }, userId);
        } catch (IabHelper.IabAsyncInProgressException e) {
            Log.w(TAG, "Error launch purchase. Another async operation in progress.");
        }
    }

    private void verifyIap(final Purchase purchase, final InvokeJavaMethodDelegate listener) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mPackageName", purchase.getPackageName());
            jsonObject.put("mSku", purchase.getSku());
            jsonObject.put("mToken", purchase.getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String receipt = jsonObject.toString();
        String token = purchase.getDeveloperPayload();
        String sign = calcSign(mVerifySign+receipt+token);
        HashMap hashMap = new HashMap();
        hashMap.put("receipt", receipt);
        hashMap.put("token", purchase.getDeveloperPayload());
        hashMap.put("sign", sign);
        SystemUtil.getInstance().requestUrl("post", this.mVerifyUrl, hashMap, new InvokeJavaMethodDelegate() {
            @Override
            public void onFinish(final ArrayList<Object> resArrayList) {
                boolean result = (boolean) resArrayList.get(0);
                if (!result) {
                    mMainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            verifyIap(purchase, listener);
                        }
                    }, 5000);
                } else {
                    String resJson = (String) resArrayList.get(1);
                    boolean ret = false;
                    String msg = "unknow error";
                    try {
                        JSONObject jsonObject = new JSONObject(resJson);
                        if (jsonObject.has("errCode") || jsonObject.has("errDesc")) {
                            ret = false;
                            msg = resJson;
                        } else {
                            String environment = jsonObject.getString("environment");
                            String sign = jsonObject.getString("sign");
                            String mySign = calcSign(mVerifySign+receipt+environment);
                            if (!mySign.equalsIgnoreCase(sign)) {
                                ret = false;
                                msg = "sign error";
                            } else {
                                ret = true;
                                msg = environment;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    final boolean finalRet = ret;
                    final String finalMsg = msg;
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList arrayList = new ArrayList();
                            arrayList.add(finalRet);
                            arrayList.add(finalMsg);
                            listener.onFinish(arrayList);
                        }
                    });
                }
            }
        });
    }

    private String calcSign(String input) {
        byte[] bArr = new byte[0];
        try {
            bArr = java.security.MessageDigest.getInstance("MD5").digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bArr) {
            String h = Integer.toHexString(0xFF & b);
            while (h.length() < 2)
                h = "0" + h;
            sb.append(h);
        }
        return sb.toString();
    }
}
