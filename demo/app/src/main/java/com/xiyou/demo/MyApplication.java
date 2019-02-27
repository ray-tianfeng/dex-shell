package com.xiyou.demo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * @author zl
 * @time 2019/2/26 0026.
 */
public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.d("PreviewProject","MyApplication call attachBaseContext");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("PreviewProject","MyApplication call onCreate");

    }
}
