package com.mxc.jniproject.ui.callback;

public interface ILifeCycle<T> {

    void initDrama(T t);


    void  onDramStateChanged(StateChanged stateChanged);

    void dispose(T t);


    public enum StateChanged{
        HIDE,SHOW,
    }
}
