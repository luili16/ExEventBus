package com.llx278.exeventbus.remote;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.TextUtils;

import com.llx278.exeventbus.Event;
import com.llx278.exeventbus.exception.RemoteException;
import com.llx278.exeventbus.exception.TimeoutException;
import com.llx278.exeventbus.exception.UnSubscribedException;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Router里面保存了进程间订阅信息
 * Created by llx on 2018/3/2.
 */

public class Router implements ITransportLayer.ReceiverListener {

    /**
     * 用来同步订阅事件的key
     */
    private static final String KEY_SYNC = "key_sync";


    private ITransportLayer mTransportLayer;

    private ExecutorService mExecutor;

    /**
     * 保存了每个进程中已经订阅的事件
     */
    private ConcurrentHashMap<String,CopyOnWriteArrayList<Event>> mEventSnapshot = new ConcurrentHashMap<>();

    public Router(Context context) {
        IMockPhysicalLayer physicalLayer = new MockPhysicalLayer(context);
        mTransportLayer = new TransportLayer(physicalLayer);
        mTransportLayer.setOnReceiveListener(this);
        mExecutor = Executors.newCachedThreadPool();
    }

    /**
     * 将当前EventBus上的订阅信息同步给其他的进程,任何导致EventBus上保存的事件列表的改变都应该调用此
     * 方法来通知其他的进程应用此更改
     *
     */
    public void updateSubscription(ArrayList<Event> eventList) {
        Bundle message = new Bundle();
        message.putParcelableArrayList(KEY_SYNC,eventList);
        mTransportLayer.sendBroadcast(message);
    }

    /**
     * 将此事件发送到其他的进程中执行
     * @return 如果是执行的方法的返回值是void，则
     * @throws TimeoutException 此事件发送失败
     */
    Parcelable route(Parcelable eventObject, String tag, String returnClassName, long timeout) throws TimeoutException {


        return null;
    }

    @Override
    public void onMessageReceive(final String where, final Bundle message) {

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<Event> newEventList = message.getParcelableArrayList(KEY_SYNC);
                if (newEventList == null || newEventList.isEmpty()) {
                    mEventSnapshot.remove(where);
                } else {
                    mEventSnapshot.put(where,new CopyOnWriteArrayList<Event>(newEventList));
                }
            }
        });

    }


    private String getAddressOf(Event event) {
        String address = null;
        Set<Map.Entry<String, CopyOnWriteArrayList<Event>>> entries = mEventSnapshot.entrySet();
        for (Map.Entry<String,CopyOnWriteArrayList<Event>> entry : entries) {
            CopyOnWriteArrayList<Event> eventLists = entry.getValue();
            if (eventLists.contains(event)) {
                address = entry.getKey();
            }
        }
        return address;
    }

    /**
     * EventEntry代表了一个具体执行的事件，
     */
    private class ExecuteEntry {

        /**
         * 发布事件的参数对象
         */
        private static final String KEY_EVENT_OBJ = "key_event_obj";
        /**
         * 发布事件的tag
         */
        private static final String KEY_TAG = "key_tag";
        /**
         * 发布事件的返回值类名
         */
        private static final String KEY_RETURN_CLASS_NAME = "key_return_class_name";

        /**
         * 代表了唯一的执行事件
         */
        public static final String KEY_ID = "key_id";

        final CountDownLatch mFinishSignal;

        ExecuteEntry() {
            mFinishSignal = new CountDownLatch(1);
        }


        Parcelable route(Parcelable eventObject, String tag, String returnClassName, long timeout)
                throws RemoteException,UnSubscribedException,TimeoutException {
            Event event = new Event(tag,eventObject.getClass().getName(),returnClassName);
            String address = getAddressOf(event);
            if (TextUtils.isEmpty(address)) {
                throw new UnSubscribedException("event["+event.toString()+"] was not subscribed by other process!");
            }
            // 封装消息
            Bundle message = new Bundle();
            message.putParcelable(KEY_EVENT_OBJ,eventObject);
            message.putString(KEY_TAG,tag);
            message.putString(KEY_RETURN_CLASS_NAME,returnClassName);
            mTransportLayer.send(address,message,timeout);

            return null;
        }
    }
}
