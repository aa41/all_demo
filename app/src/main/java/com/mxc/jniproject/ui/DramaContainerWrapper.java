package com.mxc.jniproject.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DramaContainerWrapper extends FrameLayout {
    private OnInterceptKeyDownListener listener;

    public DramaContainerWrapper(@NonNull Context context) {
        this(context, null);
    }

    public DramaContainerWrapper(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DramaContainerWrapper(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    public void setOnInterceptKeyDownListener(OnInterceptKeyDownListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (listener != null) {
            return listener.onWrapperKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }


    protected final OnInterceptKeyDownListener DEFAULT_DOWN_LISTENER = new OnInterceptKeyDownListener() {
        @Override
        public boolean onWrapperKeyDown(int keyCode, KeyEvent event) {
            Drama drama = Curtain.findDramaByView(DramaContainerWrapper.this);
            if (drama != null) {
                DramaManager manager = Curtain.of(drama.getTarget());
                if (manager != null && manager.canPop()) {
                    manager.pop(drama);
                    return true;
                }
            }
            return false;
        }
    };


    public interface OnInterceptKeyDownListener {
        boolean onWrapperKeyDown(int keyCode, KeyEvent event);
    }


}
