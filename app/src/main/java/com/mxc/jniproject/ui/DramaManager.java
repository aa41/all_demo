package com.mxc.jniproject.ui;


import android.view.View;

import com.mxc.jniproject.R;
import com.mxc.jniproject.ui.callback.DramaBuilder;
import com.mxc.jniproject.ui.callback.DramaObserver;
import com.mxc.jniproject.ui.callback.DramaPredicate;
import com.mxc.jniproject.ui.callback.ResultCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DramaManager {

    private List<Drama> history = new ArrayList<>();


    private final Object target;

    private Map<String, DramaBuilder> routers = new HashMap<>();


    public DramaManager(Object target) {
        this.target = target;
    }


    public Object getTarget() {
        return target;
    }


    public synchronized List<Drama> getChildren() {
        return history;
    }

    public void registerRouters(Map<String, DramaBuilder> routers) {
        this.routers = routers;
    }


    public synchronized <T> ResultCallback push(Drama<T> drama) {
        if (drama.isAttached()) {
            throw new IllegalArgumentException("drama is already pushed,pls check");
        }
        drama.attachDramaManager(this);
        drama.onPush();
        List<DramaObserver> observers = Curtain.getObservers();
        for (DramaObserver observer : observers) {
            if (observer != null) {
                observer.didPush(drama, history.size() - 1 < 0 ? null : history.get(history.size() - 1));
            }
        }
        history.add(drama);
        return drama.result();
    }

    public synchronized ResultCallback push(String url) {
        int index = url.indexOf("?");
        String tmpUrl = url;
        if (index > 0) {
            tmpUrl = url.substring(0, index);
        }
        DramaBuilder builder = routers.get(tmpUrl);
        if (builder != null) {
            DramaContainer.Builder build = builder.build(url);
            Drama drama = build.build();
            return push(drama);
        }

        return null;
    }


    public synchronized <T> ResultCallback pushAndRemoveUntil(Drama<T> drama, DramaPredicate predicate) {
        if (drama != null && predicate != null) {
            int index = history.size() - 1;
            ResultCallback callback = push(drama);
            while (index >= 0 && !predicate.predicate(history.get(index))) {
                pop(history.get(index));
                index--;
            }
            return callback;
        }
        return null;
    }


    public synchronized ResultCallback pushUrlAndRemoveUntil(String url, DramaPredicate predicate) {
        if (url != null && predicate != null) {
            int index = history.size() - 1;
            ResultCallback callback = push(url);
            while (index >= 0 && !predicate.predicate(history.get(index))) {
                pop(history.get(index));
                index--;
            }
            return callback;
        }

        return null;
    }

    public synchronized ResultCallback popAndPushUrl(String url, Object popResult) {
        pop(popResult);
        return push(url);
    }

    public synchronized <T> ResultCallback popAndPush(Drama<T> drama, Object popResult) {
        pop(popResult);
        return push(drama);
    }


    public synchronized <T> ResultCallback showAsDropDown(Drama<T> drama,View anchor){
        new OverlayHelper().showAsDropDown(drama,anchor);
        return push(drama);
    }

    public synchronized void popUtil(DramaPredicate predicate) {
        int index = history.size() - 1;
        while (index >= 0) {
            Drama drama = history.get(index);
            if (predicate.predicate(drama)) break;
            pop(drama);
            index--;
        }
    }


    public synchronized <T> void pop(Drama<T> drama) {
        pop(drama, null);
    }

    public synchronized <T> void pop(Drama<T> drama, Object obj) {
        boolean pop = history.remove(drama);
        if (pop) {
            OverlayHelper.dispose(drama);
            drama.onPop(obj);
            List<DramaObserver> observers = Curtain.getObservers();
            for (DramaObserver observer : observers) {
                if (observer != null) {
                    observer.didPop(history.size() - 1 < 0 ? null : history.get(history.size() - 1), drama);
                }
            }
        }

    }

    public synchronized boolean backPress() {
        boolean canPop = canPop();
        if (canPop) {
            Drama drama = history.get(history.size() - 1);
            if (drama != null) {
                drama.backPress();
            }
        }

        return canPop;
    }

    public synchronized boolean canPop() {
        return !history.isEmpty() && !history.get(history.size() - 1).isHidden();
    }

    public synchronized void pop(Object obj) {
        if (canPop()) {
            pop(history.get(history.size() - 1), obj);
        }
    }

    public synchronized void pop() {
        pop(null);
    }

    public synchronized void showWidthAnim(Drama drama, int anim) {
        if (!drama.isHidden()) return;
        for (int i = 0; i < history.size(); i++) {
            Drama tmp = history.get(i);
            if (tmp == drama) {
                history.remove(i);
                break;
            }
        }
        List<DramaObserver> observers = Curtain.getObservers();
        for (DramaObserver observer : observers) {
            if (observer != null) {
                observer.didShow(drama);
            }
        }
        history.add(drama);
        drama.onShowWithAnim(anim);
    }

    public synchronized void show(Drama drama) {
        showWidthAnim(drama, 0);
    }

    public synchronized void hide(Drama drama) {
        hideWithAnim(drama, 0);
    }

    public synchronized void hideWithAnim(Drama drama, int anim) {
        if(drama.isHidden())return;
        List<DramaObserver> observers = Curtain.getObservers();
        for (DramaObserver observer : observers) {
            if (observer != null) {
                observer.didHide(drama);
            }
        }
        history.remove(drama);
        history.add(0, drama);
        drama.onHideWidthAnim(anim);
    }


    public synchronized Drama top(){
        if(!history.isEmpty()){
            return history.get(history.size() - 1);
        }
        return null;
    }





}
