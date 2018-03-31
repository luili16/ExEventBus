package com.llx278.eventbusdemo;

import android.app.Application;

import com.llx278.exeventbus.ExEventBus;

/**
 *
 * Created by liu on 18-3-31.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ExEventBus.create(this);
    }
}
