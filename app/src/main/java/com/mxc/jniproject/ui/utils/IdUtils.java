package com.mxc.jniproject.ui.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;

import com.mxc.jniproject.ui.Drama;

import java.util.concurrent.atomic.AtomicInteger;

public class IdUtils {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateViewId() {
        if (Build.VERSION.SDK_INT < 17) {
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1;
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }


    public static <T extends View> T findViewById(Object obj, int id) {
        if (obj == null || id <= 0) return null;
        if (obj instanceof View) {
            return ((View) obj).findViewById(id);
        } else if (obj instanceof Activity) {
            return ((Activity) obj).findViewById(id);
        } else if (obj instanceof Drama) {
            Drama current = (Drama) obj;
            return current.getView().findViewById(id);
        }//todo

        return  null;

    }

}
