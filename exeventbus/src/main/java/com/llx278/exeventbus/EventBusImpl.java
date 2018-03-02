package com.llx278.exeventbus;

import android.text.TextUtils;

import com.llx278.exeventbus.execute.Executor;
import com.llx278.exeventbus.execute.ExecutorFactory;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 对IEventBus的具体实现
 * Created by llx on 2018/3/2.
 */

public class EventBusImpl implements EventBus {

    private static EventBusImpl sEventBusImpl;

    public static EventBusImpl getDefault() {

        if (sEventBusImpl == null) {
            synchronized (EventBusImpl.class) {
                if (sEventBusImpl == null) {
                    sEventBusImpl = new EventBusImpl();
                }
            }
        }
        return sEventBusImpl;
    }


    private final SubscribeHolder mSubScribeHolder;


    private EventBusImpl() {
        mSubScribeHolder = new SubscribeHolder();
    }

    @Override
    public void register(Object subscriber) {
        if (subscriber == null) {
            return;
        }

        mSubScribeHolder.put(subscriber);
    }

    @Override
    public void unRegister(Object subscriber) {
        if (subscriber == null) {
            return;
        }
        mSubScribeHolder.remove(subscriber);
    }

    @Override
    public void publish(Object eventObj, String tag) {
        if (eventObj == null || TextUtils.isEmpty(tag)) {
            Logger.e("LocalEventBus.publish(Object,String) param Object or tag is null!!", null);
            return;
        }
        publish(eventObj,tag,void.class.getName());
    }

    @Override
    public Object publish(Object eventObj, String tag, String returnClassName) {
        if (eventObj == null || TextUtils.isEmpty(tag)) {
            Logger.e("LocalEventBus.publish(Object,String,Class) param Object or tag or class is null!!", null);
            return null;
        }

        Event event = new Event(tag, eventObj.getClass().getName(), returnClassName);
        CopyOnWriteArrayList<Subscription> subscriptionList = mSubScribeHolder.get(event);
        if (subscriptionList != null) {
            for (Subscription subs : subscriptionList) {

                Executor executor = ExecutorFactory.createExecutor(subs.mThreadModel);
                Object subscribe = subs.mSubscribeRef.get();
                if (subscribe != null) {
                    if (subs.mType == Type.BLOCK_RETURN) {
                        // 因为返回值只能有一个,所以默认只是第一个注册的有效!!
                        return executor.submit(subs.mMethod,eventObj,subscribe);
                    } else if (subs.mType == Type.DEFAULT) {
                        executor.execute(subs.mMethod, eventObj, subscribe);
                    }
                }
            }
        }
        return null;
    }

    SubscribeHolder getSubscribeHolder() {
        return mSubScribeHolder;
    }
}
