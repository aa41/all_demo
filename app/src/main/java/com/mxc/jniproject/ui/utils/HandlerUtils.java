package com.mxc.jniproject.ui.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;

public class HandlerUtils {

    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    private static Map<String, Handler> cacheHandlers = new HashMap<>();
    private static Map<String, HandlerThread> cacheHandlerThreads = new HashMap<>();


    public static Handler getMainHandler() {
        return mainHandler;
    }


    public static Handler getHandlerByName(String name) {
        Handler handler = cacheHandlers.get(name);
        if (handler == null) {
            HandlerThread thread = new HandlerThread(name);
            thread.start();
            handler = new Handler(thread.getLooper());
            cacheHandlers.put(name, handler);
            cacheHandlerThreads.put(name, thread);
        }
        return handler;
    }

    public static void quitByName(String name) {
        HandlerThread thread = cacheHandlerThreads.remove(name);
        cacheHandlers.remove(name);
        if (thread != null) {
            try {
                thread.quit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


}
