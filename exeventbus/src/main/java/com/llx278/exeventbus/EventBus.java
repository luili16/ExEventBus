package com.llx278.exeventbus;

import android.os.Bundle;
import android.util.Log;

import com.llx278.exeventbus.execute.Executor;
import com.llx278.exeventbus.execute.ExecutorFactory;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * Created by llx on 2018/2/4.
 */
public class EventBus {
    private static EventBus sEventBus;
    private final SubscribeHolder mSubScribeHolder;
    private EventBus(){
        mSubScribeHolder = new SubscribeHolder();
    }

    public static EventBus getDefault() {
        if(sEventBus == null) {
            synchronized (EventBus.class) {
                if(sEventBus == null) {
                    sEventBus = new EventBus();
                }
            }
        }
        return sEventBus;
    }

    /**
     * 向EventBus上面注册一个subscriber
     * @param subscriber 待注册的subscriber
     */
    public void register(Object subscriber) {
        if (subscriber == null) {
            return;
        }

        mSubScribeHolder.put(subscriber);
    }

    /**
     * 从EventBus上面取消一个subscriber
     * @param subscriber 待取消的subscriber
     */
    public void unRegister(Object subscriber) {
        if (subscriber == null) {
            return;
        }
        mSubScribeHolder.remove(subscriber);
    }

    /**
     * 向EventBus上面发布一个事件
     */
    public void post(Object event) {
        post(event,EventType.DEFAULT_TAG);
    }

    public void post(Object event,String tag) {
        if (event == null) {
            Logger.e("EventBus.post(Object,String) param Object is null!!",null);
            return;
        }

        EventType eventType = new EventType(tag,event.getClass());
        CopyOnWriteArrayList<Subscription> subscriptionList = mSubScribeHolder.mSubscribeMap.get(eventType);
        if (subscriptionList != null) {
            for (Subscription subs : subscriptionList) {
                Executor executor = ExecutorFactory.createExecutor(subs.mThreadModel);
                Object subscribe = subs.mSubscribeRef.get();
                if (subscribe != null) {
                    executor.execute(subs.mMethod, event, subs.mSubscribeRef.get());
                }
            }
        }
    }

    public <T> T postAndReturn(Object event,Class<T> returnClass){
        return postAndReturn(event,EventType.DEFAULT_TAG,returnClass);
    }

    public <T> T postAndReturn(Object event,String tag,Class<T> returnClass) {

        return null;
    }
}
