package com.mxc.jniproject.ui;

import android.app.Application;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.mxc.jniproject.R;
import com.mxc.jniproject.ui.callback.DramaObserver;
import com.mxc.jniproject.ui.callback.DramaPredicate;

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
        CurtainLifecycleManager.init((Application) context);
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
            } else {
                ViewParent parent = view.getParent();
                if (parent instanceof ViewGroup) {
                    return findDramaByView((View) parent);
                }


            }
        }
        return null;
    }


    public static void findChildrenDrama(Object target, DramaPredicate predicate) {
        List<Drama> children = Curtain.of(target).getChildren();
        if (children == null || children.isEmpty()) return;
        for (int i = 0; i < children.size(); i++) {
            Drama drama = children.get(i);
            findChildrenDrama(drama, predicate);
        }
    }

    private static void findChildrenDrama(Drama drama, DramaPredicate predicate) {
        if (drama != null) {
            predicate.predicate(drama);
            List<Drama> childDramas = drama.getChildDramas();
            if (childDramas == null || childDramas.isEmpty())return;
            for (int i = 0; i < childDramas.size(); i++) {
                Drama child = childDramas.get(i);
                findChildrenDrama(child, predicate);
            }
        }
    }

}
