package com.mxc.jniproject.ui.callback;

import android.content.Intent;
import android.content.res.Configuration;


public interface OnActivityLifecycleListener {

    void onActivityResult(int requestCode, int resultCode, Intent data);

    void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);


    void startActivityForResult(Intent intent, int requestCode);

    void requestPermissions(String[] permissions, int requestCode);
}
