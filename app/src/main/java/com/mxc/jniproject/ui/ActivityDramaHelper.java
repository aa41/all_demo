package com.mxc.jniproject.ui;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.mxc.jniproject.ui.callback.ResultCallback;

public class ActivityDramaHelper {

    public static ResultCallback addDramaToContentView(Drama<Activity> drama, Activity target) {
        if (drama != null) {
            if (drama.isAttached()) return null;

            if (target != null) {
                View contentView = target.findViewById(android.R.id.content);
                if (drama.getRootView() != contentView) {
                    drama.setRootView((ViewGroup) contentView);
                }
                return Curtain.of(target).push(drama);
            }
        }

        return null;
    }


    public static ResultCallback addDefaultDramaToContentView(View view, Activity target) {
        DramaContainer.Builder builder = new DramaContainer.Builder();
        builder.view(view);
        Drama<Activity> drama = builder.<Activity>build();
        return addDramaToContentView(drama, target);

    }


}
