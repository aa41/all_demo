package com.mxc.jniproject.ui.callback;

public interface ILifeCycleWatcher<T> {
    void watch(ILifeCycle<T> lifeCycle);
}
