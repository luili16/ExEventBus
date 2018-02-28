package com.llx278.exeventbus;

import android.content.Context;

import com.llx278.exeventbus.remote.RemoteEventBus;

/**
 *
 * Created by llx on 2018/2/26.
 */

public class EventBus extends AbsEventBus {

    private static EventBus sEventBus;

    public static EventBus getDefault() {

        if (sEventBus == null) {
            throw new IllegalStateException("you should call EventBus.init(Context) before use getDefault()");
        }

        return sEventBus;
    }

    public static void init(Context context) {
        if (sEventBus == null) {
            synchronized (EventBus.class) {
                if(sEventBus == null) {
                    sEventBus = new EventBus(new AbsEventBus(),new RemoteEventBus(context));
                }
            }
        }
    }

    public static void destroy() {
        if (sEventBus != null) {
            sEventBus = null;
        }
    }

    private final AbsEventBus mAbsEventBus;
    private final RemoteEventBus mRemoteEventBus;

    private EventBus(AbsEventBus absEventBus,RemoteEventBus remoteEventBus) {
        mAbsEventBus = absEventBus;
        mRemoteEventBus = remoteEventBus;
    }

    @Override
    public void register(Object subscriber) {
        mAbsEventBus.register(subscriber);
    }

    @Override
    public void unRegister(Object subscriber) {
        mAbsEventBus.unRegister(subscriber);
    }

    @Override
    public void publish(Object eventObj, String tag) {

        mAbsEventBus.publish(eventObj,tag);
    }

    @Override
    public Object publish(Object eventObj, String tag, String returnClass) {
        return mAbsEventBus.publish(eventObj,tag,returnClass);
    }

    @Override
    public Object publish(Object eventObj, String tag, String returnClassName, boolean remote) {

        if (remote) {


        }

        return mAbsEventBus.publish(eventObj,tag,returnClassName,remote);
    }

    @Override
    SubscribeHolder getSubscribeHolder() {
        return mAbsEventBus.getSubscribeHolder();
    }
}
