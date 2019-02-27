package com.xiyou.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewConfiguration;

import com.test.Test299;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("PreviewProject", ViewConfiguration.get(this).getScaledTouchSlop()+"");
        Log.d("PreviewProject","call onCreate");
        new Test299().test0();
        try {
            Thread.sleep(200000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("PreviewProject","call onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("PreviewProject","call onRestoreInstanceState");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("PreviewProject","call onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("PreviewProject","call onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("PreviewProject","call onStop");
    }
}