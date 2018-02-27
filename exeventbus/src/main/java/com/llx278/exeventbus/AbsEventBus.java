package com.llx278.exeventbus;

import android.text.TextUtils;

import com.llx278.exeventbus.execute.Executor;
import com.llx278.exeventbus.execute.ExecutorFactory;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by llx on 2018/2/4.
 */
public class AbsEventBus {
    private final SubscribeHolder mSubScribeHolder;

    public AbsEventBus() {
        mSubScribeHolder = new SubscribeHolder();
    }

    /**
     * 向EventBus上面注册一个subscriber
     *
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
     *
     * @param subscriber 待取消的subscriber
     */
    public void unRegister(Object subscriber) {
        if (subscriber == null) {
            return;
        }
        mSubScribeHolder.remove(subscriber);
    }

    public void post(Object eventObj, String tag) {
        if (eventObj == null || TextUtils.isEmpty(tag)) {
            Logger.e("AbsEventBus.post(Object,String) param Object or tag is null!!", null);
            return;
        }
        post(eventObj,tag,void.class.getName());
    }

    public Object post(Object eventObj, String tag,String returnClassName) {
        return post(eventObj,tag,returnClassName,false);
    }

    public Object post(Object eventObj, String tag,String returnClassName,boolean remote) {
        if (eventObj == null || TextUtils.isEmpty(tag)) {
            Logger.e("AbsEventBus.post(Object,String,Class) param Object or tag or class is null!!", null);
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

    public void cleanUp() {

    }

}
