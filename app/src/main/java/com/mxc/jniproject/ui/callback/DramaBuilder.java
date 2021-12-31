package com.mxc.jniproject.ui.callback;

import android.content.Context;

import com.mxc.jniproject.ui.Drama;
import com.mxc.jniproject.ui.DramaContainer;

public interface DramaBuilder {

    DramaContainer.Builder build(String url);
}
