package com.llx278.exeventbus;

import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * 对ExEventBus的具体实现
 * Created by llx on 2018/3/2.
 */

public class ExEventBusImpl implements ExEventBus {



    @Override
    public ArrayList<Event> register(Object subscriber) {
        return null;
    }

    @Override
    public ArrayList<Event> unRegister(Object subscriber) {
        return null;
    }

    @Override
    public void publish(Object eventObj, String tag) {

    }

    @Override
    public Object publish(Object eventObj, String tag, String returnClassName) {
        return null;
    }

    @Override
    public ArrayList<Event> query() {
        return null;
    }


    @Override
    public Parcelable publishToRemote(Parcelable eventObj, String tag, String returnClassName) {

        return null;
    }
}
