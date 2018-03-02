package com.llx278.exeventbus;

import android.os.Parcelable;

/**
 * 对ExEventBus的具体实现
 * Created by llx on 2018/3/2.
 */

public class ExEventBusImpl implements ExEventBus {



    @Override
    public void register(Object subscriber) {

    }

    @Override
    public void unRegister(Object subscriber) {

    }

    @Override
    public void publish(Object eventObj, String tag) {

    }

    @Override
    public Object publish(Object eventObj, String tag, String returnClassName) {
        return null;
    }


    @Override
    public Parcelable publishToRemote(Parcelable eventObj, String tag, String returnClassName) {

        return null;
    }
}
