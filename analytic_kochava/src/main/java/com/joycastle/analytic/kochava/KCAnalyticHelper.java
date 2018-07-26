package com.joycastle.analytic.kochava;import android.app.Activity;import android.app.Application;import android.content.Intent;import android.os.Bundle;import android.util.Log;import com.joycastle.gamepluginbase.LifeCycleDelegate;import com.joycastle.gamepluginbase.SystemUtil;import com.kochava.base.Tracker;import java.util.HashMap;import java.util.Iterator;import java.util.Map;public class KCAnalyticHelper implements LifeCycleDelegate {    private static final String TAG = "KCAnalyticHelper";    private static KCAnalyticHelper instance = new KCAnalyticHelper();    public static KCAnalyticHelper getInstance() {        return instance;    }    private KCAnalyticHelper() {}    public void charge(String iapId, double cash, double coin, int channal) {    }    public void onEvent(int eventId, HashMap map) {        int event;        if(eventId == 1) {            event = Tracker.EVENT_TYPE_PURCHASE;        }        else if(eventId == 2)        {            event = Tracker.EVENT_TYPE_LEVEL_COMPLETE;        }        else        {            return;        }        Tracker.Event e = new Tracker.Event(event);        Iterator iterator = map.entrySet().iterator();        while (iterator.hasNext()) {            Map.Entry entry = (Map.Entry) iterator.next();            e.addCustom(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));        }        Tracker.sendEvent(e);    }    public void onEvent(String eventName, HashMap map) {        Tracker.Event e = new Tracker.Event(eventName)                .setName(eventName);        Iterator iterator = map.entrySet().iterator();        while (iterator.hasNext()) {            Map.Entry entry = (Map.Entry) iterator.next();            e.addCustom(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));        }        Tracker.sendEvent(e);    }    @Override    public void init(Application application) {        String appKey = SystemUtil.getInstance().getPlatCfgValue("kochava_key");        Tracker.configure(new Tracker.Configuration(application)                .setAppGuid(appKey)                .setLogLevel(Tracker.LOG_LEVEL_INFO)        );        Log.i(TAG, "Tracker installed, Version: "+Tracker.getVersion());    }    @Override    public void onCreate(Activity activity, Bundle savedInstanceState) {    }    @Override    public void onStart(Activity activity) {    }    @Override    public void onResume(Activity activity) {    }    @Override    public void onPause(Activity activity) {    }    @Override    public void onStop(Activity activity) {    }    @Override    public void onDestroy(Activity activity) {    }    @Override    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {        Log.d(TAG, "onActivityResult: ");    }}