package com.mxc.jniproject.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;



import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressLint("ValidFragment")
class CurtainLifecycleManager extends Fragment implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = CurtainLifecycleManager.class.getName();

    private static CurtainLifecycleManager newInstance() {
        return new CurtainLifecycleManager();
    }

    private Map<Integer, WeakReference<Drama>> resultDramaCaches = Collections.synchronizedMap(new HashMap<>());

    private static boolean isInit = false;


    private static Activity currentActivity;


    public static Activity currentActivity() {
        return currentActivity;
    }

    public static void init(Application app) {
        if (isInit) return;
        app.registerActivityLifecycleCallbacks(newInstance());
        isInit = true;
    }


    public static CurtainLifecycleManager get() {
        FragmentManager fragmentManager = currentActivity().getFragmentManager();
        CurtainLifecycleManager fragment = (CurtainLifecycleManager) fragmentManager.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = newInstance();
            try {
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.add(fragment, TAG);
                ft.commitAllowingStateLoss();
                fragmentManager.executePendingTransactions();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return fragment;
    }


    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void startActivityForResult(Drama drama,Intent intent,int requestCode) {
        resultDramaCaches.put(requestCode,new WeakReference<>(drama));
        startActivityForResult(intent, requestCode);
    }
    

    public void requestPermissions(Drama drama, String[] permissions, int requestCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resultDramaCaches.put(requestCode,new WeakReference<>(drama));
            requestPermissions(permissions,requestCode);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        WeakReference<Drama> reference = resultDramaCaches.remove(requestCode);
        if(reference != null && reference.get() != null){
            Drama drama = reference.get();
            drama.onActivityResult(requestCode,resultCode,data);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        WeakReference<Drama> reference = resultDramaCaches.remove(requestCode);
        if(reference != null && reference.get() != null){
            Drama drama = reference.get();
            drama.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }


    @Override
    public void onActivityCreated( Activity activity, Bundle savedInstanceState) {
        currentActivity = activity;
    }

    @Override
    public void onActivityStarted( Activity activity) {

    }

    @Override
    public void onActivityResumed( Activity activity) {
        if (currentActivity != activity) {
            currentActivity = activity;
        }
    }

    @Override
    public void onActivityPaused( Activity activity) {

    }

    @Override
    public void onActivityStopped( Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState( Activity activity,  Bundle outState) {

    }

    @Override
    public void onActivityDestroyed( Activity activity) {

    }
    
}
