package com.mxc.jniproject.ui;

import android.view.View;
import android.view.ViewGroup;

import com.mxc.jniproject.ui.callback.ILifeCycleWatcher;
import com.mxc.jniproject.ui.callback.ResultCallback;

import java.util.List;

public interface Drama<T> {


    T getTarget();

    List<Drama> getChildDramas();

    boolean isAttached();

    boolean isOverlay();

    void setOverlay(boolean isOverlay);


    View getView();

    void onPush();

    void onPop(Object obj);

    void onPop();

    void onHideWidthAnim(int anim);

    void onHide();

    boolean isHidden();

    void onShowWithAnim(int anim);

    void onShow();

    String url();

    void setParams(Object obj);

    Object getParams();

    void attachDramaManager(DramaManager manager);


    DramaManager getDramaManager();

    ResultCallback result();


    ILifeCycleWatcher<T> getLifeCycle();


    void backPress();


    void setRootView(ViewGroup rootView);

    ViewGroup getRootView();


    void setX(int x);

    int getX();


    void setY(int y);

    int getY();

    void setWidth(int width);

    int getWidth();

    void setHeight(int height);

    int getHeight();

    void setGravity(int gravity);

    int getGravity();

}
