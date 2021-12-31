package com.mxc.jniproject.ui.callback;

import com.mxc.jniproject.ui.Drama;

public interface DramaObserver {
    void didPush(Drama current, Drama previous);

    void didPop(Drama current, Drama previous);

    void didHide(Drama current);

    void didShow(Drama current);

}
