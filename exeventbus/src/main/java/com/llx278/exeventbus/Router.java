package com.llx278.exeventbus;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.llx278.exeventbus.exception.TimeoutException;
import com.llx278.exeventbus.execute.Executor;
import com.llx278.exeventbus.remote.IMockPhysicalLayer;
import com.llx278.exeventbus.remote.ITransportLayer;
import com.llx278.exeventbus.remote.MockPhysicalLayer;
import com.llx278.exeventbus.remote.Receiver;
import com.llx278.exeventbus.remote.TransportLayer;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Router封装了如何将订阅事件发布到其他的进程
 * Created by llx on 2018/3/2.
 */

public class Router implements Receiver {


    private static final String TAG = "Router";
    /**
     * 此key代表了每一个消息的类型
     */
    private static final String KEY_TYPE = "type";

    /**
     * 此value代表向其他进程查询已经注册的订阅事件
     */
    private static final String TYPE_VALUE_OF_QUERY = "query_event";
    /**
     * 此value代表查询已经注册的订阅事件的结果
     */
    private static final String TYPE_VALUE_OF_QUERY_RESULT = "query_event_result";
    /**
     * 此value代表添加一个订阅事件列表
     */
    private static final String TYPE_VALUE_OF_ADD = "add_event";
    /**
     * 此value代表移除一个订阅事件列表
     */
    private static final String TYPE_VALUE_OF_REMOVE = "remove_event";
    /**
     * 向其他进程的总线上发布一个订阅事件
     */
    private static final String TYPE_VALUE_OF_PUBLISH = "publish_event";
    /**
     * 向其他进程的总线上发布一个订阅事件的返回值
     */
    private static final String TYPE_VALUE_OF_PUBLISH_RETURN_VALUE = "publish_event_value";
    /**
     * 此key封装了查询订阅事件的列表
     */
    private static final String KEY_QUERY_LIST = "query_list";
    /**
     * 此key封装了添加订阅事件的列表
     */
    private static final String KEY_ADD_LIST = "add_list";
    /**
     * 此key封装了解除订阅事件的列表
     */
    private static final String KEY_REMOVE_LIST = "remove_list";
    /**
     * 此key封装了发布事件的参数
     */
    private static final String KEY_EVENT_OBJ = "publish_event_obj";
    /**
     * 此key封装了发布事件的tag
     */
    private static final String KEY_TAG = "publish_tag";
    /**
     * 此key封装了发布事件的返回值类型
     */
    private static final String KEY_RETURN_CLASS_NAME = "publish_return_class_name";
    /**
     * 此key封装了发布事件执行完毕后的返回值
     */
    private static final String KEY_RETURN_VALUE = "return_value";
    /**
     * 此key封装了每个消息唯一标志
     */
    private static final String KEY_ID = "id";

    private static long sDefaultTimeout = 1000 * 5;

    private ITransportLayer mTransportLayer;

    private ExecutorService mExecutor;

    private EventBus mEventBus;

    /**
     * 保存了对从其他进程发送的消息进行处理的类的列表
     */
    private ArrayList<MessageObserver> mMessageObserverList =
            new ArrayList<>();

    /**
     * 保存了等待其他进程执行结束返回的结果的列表
     */
    private ConcurrentHashMap<String, PublishHandler> mWaitingExecuteReturnValueMap =
            new ConcurrentHashMap<>();

    /**
     * 保存了每个进程已经订阅事件的列表
     */
    private ConcurrentHashMap<String, CopyOnWriteArrayList<Event>> mSubscribeEventList =
            new ConcurrentHashMap<>();

    public Router(Context context, EventBus eventBus) {
        IMockPhysicalLayer physicalLayer = new MockPhysicalLayer(context);
        mTransportLayer = new TransportLayer(physicalLayer);
        mTransportLayer.setOnReceiveListener(this);
        mExecutor = Executors.newCachedThreadPool();
        mEventBus = eventBus;

        mMessageObserverList.add(new AddEventListHandler());
        mMessageObserverList.add(new RemoveEventListHandler());
        mMessageObserverList.add(new ResultHandler());
        mMessageObserverList.add(new ExecuteHandler());
        mMessageObserverList.add(new QueryHandler());
        mMessageObserverList.add(new QueryResultHandler());

        // 向其他进程发送一次查询消息，请求其他已经注册了EventBus的进程中所有Event列表
        Bundle message = new Bundle();
        message.putString(KEY_ID,UUID.randomUUID().toString());
        message.putString(KEY_TYPE,TYPE_VALUE_OF_QUERY);
        mTransportLayer.sendBroadcast(message);

    }

    /**
     * 将此事件发送到其他的进程中执行
     *
     * @return 返回执行的结果，如果是方法的返回值是void，则默认返回null
     * @throws TimeoutException 超时
     */
    Bundle route(Bundle eventObject, String tag, String returnClassName, long timeout)
            throws TimeoutException {

        // 先判断此事件是否已经被其他的进程注册了
        Event event = new Event(tag,eventObject.getClass().getName(),returnClassName);
        String address = getAddressOf(event);
        if (TextUtils.isEmpty(address)) {
            return null;
        }

        PublishHandler publishHandler = new PublishHandler();
        // 缓存执行的事件，等待执行结果的返回
        mWaitingExecuteReturnValueMap.put(address,publishHandler);
        return publishHandler.publishToRemote(eventObject, tag, returnClassName, timeout);
    }

    /**
     * 通知其他进程的EventBus，此进程的EventBus发生了一个新的注册事件
     * @param newEventArrayList 新注册的event事件列表
     */
    public void add(ArrayList<Event> newEventArrayList) {
        Bundle message = new Bundle();
        // 封装消息类型
        message.putString(KEY_TYPE, TYPE_VALUE_OF_ADD);
        // 封装订阅事件的列表
        message.putParcelableArrayList(KEY_ADD_LIST,newEventArrayList);
        message.putString(KEY_ID,UUID.randomUUID().toString());
        // 发送给所有已经注册了总线的进程
        mTransportLayer.sendBroadcast(message);
    }

    /**
     * 通知其他进程的EventBus，此进程的EventBus移除了一个注册事件
     * @param oldEventArrayList 已经移除的注册事件列表
     */
    public void remove(ArrayList<Event> oldEventArrayList) {
        Bundle message = new Bundle();
        message.putString(KEY_TYPE, TYPE_VALUE_OF_REMOVE);
        message.putParcelableArrayList(KEY_REMOVE_LIST,oldEventArrayList);
        message.putString(KEY_ID,UUID.randomUUID().toString());
        mTransportLayer.sendBroadcast(message);
    }

    public String getAddressOf(Event event) {
        String address = null;
        Set<Map.Entry<String, CopyOnWriteArrayList<Event>>> entries = mSubscribeEventList.entrySet();
        for (Map.Entry<String,CopyOnWriteArrayList<Event>> entry : entries) {
            CopyOnWriteArrayList<Event> eventValueList = entry.getValue();
            for (Event testedEvent : eventValueList) {
                if (testedEvent.equals(event)) {
                    address = entry.getKey();
                }
            }
        }
        return address;
    }

    ConcurrentHashMap<String, CopyOnWriteArrayList<Event>> getSubScribeEventList() {
        return mSubscribeEventList;
    }


    @Override
    public void onMessageReceive(final String where, final Bundle message) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (MessageObserver observer : mMessageObserverList) {
                    if (observer.handleMessage(where, message)) {
                        return;
                    }
                }
            }
        });
    }

    /**
     * 消息处理接口
     */
    interface MessageObserver {

        /**
         * 处理消息
         *
         * @param where   地址
         * @param message 消息
         * @return true 此条消息已经被处理，false 没有被处理，
         */
        boolean handleMessage(String where, Bundle message);
    }

    /**
     * 发布订阅事件
     */
    private class PublishHandler {

        /**
         * 这个事件的唯一标识
         */
        private String mId;

        /**
         * 返回的结果
         */
        private Bundle mResult;
        /**
         * true 正确的获得了结果 false 没有获得
         */
        private boolean mHasResult;

        private CountDownLatch mDoneSignal;

        PublishHandler() {
            mId = UUID.randomUUID().toString();
            mDoneSignal = new CountDownLatch(1);
        }

        Bundle publishToRemote(Bundle eventObj,String tag,String returnClassName,long timeout)
                throws TimeoutException {

            Event event = new Event(tag,eventObj.getClass().getName(),returnClassName);
            String address = getAddressOf(event);
            if (!TextUtils.isEmpty(address)) {
                // 封装消息
                Bundle message = new Bundle();
                message.putString(KEY_TYPE, TYPE_VALUE_OF_PUBLISH);
                message.putString(KEY_ID,mId);
                message.putBundle(KEY_EVENT_OBJ,eventObj);
                message.putString(KEY_TAG,tag);
                message.putString(KEY_RETURN_CLASS_NAME,returnClassName);
                long currentTime = SystemClock.uptimeMillis();
                mTransportLayer.send(address,message,timeout);
                long endTime = SystemClock.uptimeMillis();
                long leftTimeout = endTime - currentTime;
                mHasResult = false;
                try {
                    if (!mDoneSignal.await(leftTimeout,TimeUnit.MILLISECONDS)) {
                        // 等待超时
                        throw new TimeoutException("wait publish result timeout!");
                    }
                } catch (InterruptedException ignore) {
                }
                if (mHasResult) {
                    return mResult;
                }
            }
            return null;
        }
    }

    //   --------------- 内部类（MessageObserver的不同实现） -------------------

    /**
     * 用来处理查询订阅注册列表的类
     */
    private class QueryHandler implements MessageObserver {
        @Override
        public boolean handleMessage(String where, Bundle message) {
            String typeValue = message.getString(KEY_TYPE);
            if (TYPE_VALUE_OF_QUERY.equals(typeValue)) {
                ArrayList<Event> allEvents = mEventBus.query();
                Bundle valueMessage = new Bundle();
                valueMessage.putString(KEY_ID,UUID.randomUUID().toString());
                valueMessage.putString(KEY_TYPE,TYPE_VALUE_OF_QUERY_RESULT);
                valueMessage.putParcelableArrayList(KEY_QUERY_LIST,allEvents);
                try {
                    mTransportLayer.send(where,valueMessage,sDefaultTimeout);
                } catch (TimeoutException e) {
                    Logger.e(TAG,"send message[typeValue = " + typeValue + " allEvents = " +
                            allEvents + "]",e);
                }
                return true;
            }
            return false;
        }
    }

    /**
     * 用来处理查询订阅注册列表结果的类
     */
    private class QueryResultHandler implements MessageObserver {

        @Override
        public boolean handleMessage(String where, Bundle message) {

            String typeValue = message.getString(KEY_TYPE);
            if (TYPE_VALUE_OF_QUERY_RESULT.equals(typeValue)) {
                Log.d("main","接收了query！！");
                ArrayList<Event> queryEvents = message.getParcelableArrayList(KEY_QUERY_LIST);
                if (queryEvents != null) {
                    mSubscribeEventList.put(where,new CopyOnWriteArrayList<>(queryEvents));
                }
                return true;
            }
            return false;
        }
    }


    /**
     * 用来添加注册事件的类
     */
    private class AddEventListHandler implements MessageObserver {

        @Override
        public boolean handleMessage(String where, Bundle message) {

            String typeValue = message.getString(KEY_TYPE);
            if (TYPE_VALUE_OF_ADD.equals(typeValue)) {
                // 接收到了一个订阅事件
                ArrayList<Event> newEvents = message.getParcelableArrayList(KEY_ADD_LIST);
                if (newEvents != null && !newEvents.isEmpty() ) {
                    CopyOnWriteArrayList<Event> events = mSubscribeEventList.get(where);
                    if (events == null) {
                        events = new CopyOnWriteArrayList<>();
                    }
                    events.addAll(newEvents);
                    mSubscribeEventList.put(where,events);
                    Log.d("main","mSubscribeEventList : " + mSubscribeEventList.toString());
                }
                return true;
            }
            return false;
        }
    }

    /**
     * 用来处理删除注册事件的类
     */
    private class RemoveEventListHandler implements MessageObserver {

        @Override
        public boolean handleMessage(String where, Bundle message) {
            String typeValue = message.getString(KEY_TYPE);
            if (TYPE_VALUE_OF_REMOVE.equals(typeValue)) {
                // 接收到了一个解除订阅的时间
                ArrayList<Event> unregisterEvents = message.getParcelableArrayList(KEY_REMOVE_LIST);
                if (unregisterEvents != null && !unregisterEvents.isEmpty()) {
                    CopyOnWriteArrayList<Event> events = mSubscribeEventList.get(where);
                    if (events == null || events.isEmpty()) {
                        Logger.i(TAG,"got an empty event list when attempt to delete events " +
                                "from subscribeEventList!");
                        return true;
                    }
                    events.removeAll(unregisterEvents);
                }
            }
            return false;
        }
    }

    /**
     * 执行发布的订阅事件
     */
    private class ExecuteHandler implements MessageObserver {

        @Override
        public boolean handleMessage(String where, Bundle message) {

            String typeValue = message.getString(KEY_TYPE);
            if (TYPE_VALUE_OF_PUBLISH.equals(typeValue)) {
                // 执行一个发布的订阅事件
                String id = message.getString(KEY_ID);
                Parcelable eventObj = message.getParcelable(KEY_EVENT_OBJ);
                String tag = message.getString(KEY_TAG);
                String returnClassName = message.getString(KEY_RETURN_CLASS_NAME);
                Object returnValue = mEventBus.publish(eventObj, tag, returnClassName);
                // 执行结束，将此消息发送回去
                Bundle valueMessage = new Bundle();
                valueMessage.putString(KEY_TYPE, TYPE_VALUE_OF_PUBLISH_RETURN_VALUE);
                valueMessage.putParcelable(KEY_RETURN_VALUE, (Parcelable) returnValue);
                valueMessage.putString(KEY_ID,id);
                try {
                    mTransportLayer.send(where,valueMessage, sDefaultTimeout);
                } catch (TimeoutException e) {
                    Logger.e(TAG,"send message[typeValue = " + typeValue + " rturnvalue = " +
                            returnValue + "]",e);
                }
                return true;
            }
            return false;
        }
    }

    /**
     * 等待返回的结果
     */
    private class ResultHandler implements MessageObserver {

        @Override
        public boolean handleMessage(String where, Bundle message) {

            String typeValue = message.getString(KEY_TYPE);
            if (TYPE_VALUE_OF_PUBLISH_RETURN_VALUE.equals(typeValue)) {
                // 接收到了一个订阅事件的执行结果
                Bundle returnValue = message.getBundle(KEY_RETURN_VALUE);
                String id = message.getString(KEY_ID);
                if (!TextUtils.isEmpty(id)) {
                    PublishHandler mWaitingPublishHandler = mWaitingExecuteReturnValueMap.get(id);
                    mWaitingPublishHandler.mResult = returnValue;
                    mWaitingPublishHandler.mHasResult = true;
                    mWaitingPublishHandler.mDoneSignal.countDown();
                    // 删除已经执行结束的事件
                    mWaitingExecuteReturnValueMap.remove(id);
                    return true;
                }
            }
            return false;
        }
    }

}
