package com.mxc.jniproject.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DramaEditTextWrapper extends DramaContainerWrapper {
    public DramaEditTextWrapper(@NonNull Context context) {
        this(context,null);
    }

    public DramaEditTextWrapper(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DramaEditTextWrapper(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnInterceptKeyDownListener(DEFAULT_DOWN_LISTENER);
    }






}
