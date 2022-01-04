package com.mxc.jniproject.ui;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

public class DramaContainerWrapper extends FrameLayout {
    private DramaManager manager;

    public DramaContainerWrapper(@NonNull Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

    }

    void attachDramaManager(DramaManager manager) {
        this.manager = manager;
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && manager.canPop()) {
            manager.pop();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        Log.e("1111111","onKeyUp:"+event);
//        return true;
//    }
//
//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//
//        Log.e("1111111","dispatchKeyEvent:"+event);
//        return true;
//
//        //return super.dispatchKeyEvent(event);
//    }

}
