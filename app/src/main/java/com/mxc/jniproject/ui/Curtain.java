package com.mxc.jniproject.ui;

import android.app.Application;
import android.content.Context;
import android.view.View;

import com.mxc.jniproject.R;
import com.mxc.jniproject.ui.callback.DramaObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class Curtain {

    private static class INNER {
        public static final Curtain INSTANCE = new Curtain();
    }

    private Curtain() {
    }

    private static volatile boolean isInit = false;
    private static Context context;
    private WeakHashMap<Object, DramaManager> dramaManagers = new WeakHashMap<>();
    private List<DramaObserver> observers = new ArrayList<>();

    static Curtain getInstance() {
        return INNER.INSTANCE;
    }

    public static void init(Context ctx) {
        if (isInit) return;
        if (ctx == null) {
            throw new IllegalArgumentException("context can not be null");
        }
        if (ctx instanceof Application) {
            context = ctx;
        } else {
            context = ctx.getApplicationContext();
        }
    }


    public static DramaManager of(Object obj) {
        return getInstance().getDramManager(obj);
    }

    synchronized DramaManager getDramManager(Object obj) {
        DramaManager manager = dramaManagers.get(obj);
        if (manager == null) {
            manager = new DramaManager(obj);
        }
        dramaManagers.put(obj, manager);
        return manager;
    }


    public static void addDramaObserver(DramaObserver observer) {
        List<DramaObserver> observers = getInstance().observers;
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public static void removeDramaObserver(DramaObserver observer) {
        List<DramaObserver> observers = getInstance().observers;
        observers.remove(observer);
    }


     static List<DramaObserver> getObservers() {
        return getInstance().observers;
    }

    public static Context getContext() {
        return context;
    }


    public static Drama findDramaByView(View view) {
        if (view != null) {
            Object viewTag = view.getTag(R.id.curtain_target_id);
            if (viewTag instanceof Drama) {
                return (Drama) viewTag;
            }
        }
        return null;
    }

}
