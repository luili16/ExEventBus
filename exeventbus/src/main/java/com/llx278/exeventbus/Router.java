package com.llx278.exeventbus;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.TextUtils;

import com.llx278.exeventbus.exception.TimeoutException;
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
 * Router里面保存了进程间订阅信息
 * <p>
 * <p>
 * 进程间信息的共享主要靠这样的几条消息来维持
 * <p>
 * 进程间消息通信的载体用的是Bundle，因此所有消息都是以key-value的形式来封装。
 * <p>
 * 1.事件列表发生变化
 * 如果EventBus上产生了一个注册事件，或者产生了一个解除注册的事件。则将执行的结果发送给其他的进程，
 * 使得进程间始终保存最新的事件列表
 * 2.发布一个事件
 * 向其他进程发布一个事件，此事件被其他进程的EventBus所匹配执行。
 * 3.发送一个事件执行结束的返回结果
 * <p>
 * 消息封装
 * <p>
 * key                      value(type)
 * <p>
 * type(String)           register_event // 此条消息是一个订阅事件
 *                        unregister_event // 此条消息是一个解除订阅的事件
 *                        publish_event // 此条消息是一个待执行的发布事件
 *                        publish_event_value // 此条消息是一个已经执行完毕的发布事件的返回值
 * <p>
 * <p>
 * // 备选的key，依据不同的type类型
 * register_list(String)               ParcelableArrayList // 订阅事件的列表
 * unregister_list(String)             ParcelableArrayList // 解除订阅事件的列表
 * <p>
 * publish_event_obj(String)           Parcelable    // 发布事件参数
 * publish_tag(String)                 String        // 发布事件的Tag
 * publish_return_class_name(String)   String        // 订阅事件的返回值类型
 * id                                  String        // 唯一标志一条消息
 * return_value                        Parcelable    // 订阅事件执行结束的返回值
 * <p>
 * Created by llx on 2018/3/2.
 */

public class Router implements Receiver {

    private static final String TAG = "Router";

    /**
     * 每一个消息执行实体都应该有一个事件的类型
     */
    private static final String KEY_TYPE = "type";
    /**
     * 每个消息唯一标志
     */
    private static final String KEY_ID = "id";

    private ITransportLayer mTransportLayer;

    private ExecutorService mExecutor;

    private EventBus mEventBus;

    /**
     * 保存了对从其他进程发送的消息进行处理的类的列表
     */
    private CopyOnWriteArrayList<MessageObserver> mMessageObserverList = new CopyOnWriteArrayList<>();

    /**
     * 保存了等待其他进程执行结束返回的结果的列表
     */
    private ConcurrentHashMap<String, PublishHandler> mWaitingExecuteReturnValueMap = new ConcurrentHashMap<>();

    /**
     * 保存了每个进程已经订阅事件的列表
     */
    private ConcurrentHashMap<String, CopyOnWriteArrayList<Event>> mSubscribeEventList = new ConcurrentHashMap<>();

    private RegisterHandler mRegisterHandler;
    private UnregisterHandler mUnRegisterHandler;


    public Router(Context context, EventBus eventBus) {
        IMockPhysicalLayer physicalLayer = new MockPhysicalLayer(context);
        mTransportLayer = new TransportLayer(physicalLayer);
        mTransportLayer.setOnReceiveListener(this);
        mExecutor = Executors.newCachedThreadPool();
        mEventBus = eventBus;

        mRegisterHandler = new RegisterHandler();
        mUnRegisterHandler = new UnregisterHandler();
        mMessageObserverList.add(mRegisterHandler);
        mMessageObserverList.add(mUnRegisterHandler);
        mMessageObserverList.add(new ResultHandler());
        mMessageObserverList.add(new ExecuteHandler());
    }

    /**
     * 将此事件发送到其他的进程中执行
     *
     * @return 返回执行的结果，如果是方法的返回值是void，则默认返回null
     * @throws TimeoutException 此事件发送失败
     */
    Parcelable route(Parcelable eventObject, String tag, String returnClassName, long timeout)
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

    public void registerToRemote(ArrayList<Event> eventArrayList) {
        mRegisterHandler.registerToRemote(eventArrayList);
    }

    public void unRegisterToRemote(ArrayList<Event> eventArrayList) {
        mUnRegisterHandler.unregisterToRemote(eventArrayList);
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

    /**
     * 用来处理注册事件的类
     */
    private class RegisterHandler implements MessageObserver {

        /**
         * 此value代表此条消息是注册事件
         */
        private static final String TYPE_VALUE = "register_event";

        private static final String KEY_REGISTER_LIST = "register_list";

        void registerToRemote(ArrayList<Event> eventArrayList) {
            Bundle message = new Bundle();
            // 封装消息类型
            message.putString(KEY_TYPE,TYPE_VALUE);
            // 封装订阅事件的列表
            message.putParcelableArrayList(KEY_REGISTER_LIST,eventArrayList);
            message.putString(KEY_ID,UUID.randomUUID().toString());
            // 发送给所有已经注册了总线的进程
            mTransportLayer.sendBroadcast(message);
        }


        @Override
        public boolean handleMessage(String where, Bundle message) {

            String typeValue = message.getString(KEY_TYPE);
            if (TYPE_VALUE.equals(typeValue)) {
                // 接收到了一个订阅事件
                ArrayList<Event> newEvents = message.getParcelableArrayList(KEY_REGISTER_LIST);
                if (newEvents != null && !newEvents.isEmpty() ) {
                    CopyOnWriteArrayList<Event> events = mSubscribeEventList.get(where);
                    if (events == null) {
                        events = new CopyOnWriteArrayList<>();
                        mSubscribeEventList.put(where,events);
                    }
                    events.addAll(newEvents);
                }
                return true;
            }
            return false;
        }
    }

    /**
     * 用来处理解除注册事件的类
     */
    private class UnregisterHandler implements MessageObserver {

        private static final String TYPE_VALUE = "unregister_event";

        private static final String KEY_UNREGISTER_LIST = "unregister_list";

        void unregisterToRemote(ArrayList<Event> eventArrayList) {
            Bundle message = new Bundle();
            message.putString(KEY_TYPE,TYPE_VALUE);
            message.putParcelableArrayList(KEY_UNREGISTER_LIST,eventArrayList);
            message.putString(KEY_ID,UUID.randomUUID().toString());
            mTransportLayer.sendBroadcast(message);
        }

        @Override
        public boolean handleMessage(String where, Bundle message) {
            String typeValue = message.getString(KEY_TYPE);
            if (TYPE_VALUE.equals(typeValue)) {
                // 接收到了一个解除订阅的时间
                ArrayList<Event> unregisterEvents = message.getParcelableArrayList(KEY_UNREGISTER_LIST);
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
     * 发布订阅事件
     */
    private class PublishHandler {
        static final String TYPE_VALUE = "publish_event";

        private static final String KEY_EVENT = "executableEvent";

        /**
         * 发布事件的参数
         */
        private static final String KEY_EVENT_OBJ = "publish_event_obj";

        /**
         * 发布事件的tag
         */
        private static final String KEY_TAG = "publish_tag";

        /**
         * 发布事件的返回值类型
         */
        private static final String KEY_RETURN_CLASS_NAME = "publish_return_class_name";

        /**
         * 这个事件的唯一标识
         */
        private String mId;

        /**
         * 返回的结果
         */
        private Parcelable mResult;
        /**
         * true 正确的获得了结果 false 没有获得
         */
        private boolean mHasResult;

        private CountDownLatch mDoneSignal;

        public PublishHandler() {
            mId = UUID.randomUUID().toString();
            mDoneSignal = new CountDownLatch(1);
        }

        Parcelable publishToRemote(Parcelable eventObj,String tag,String returnClassName,long timeout)
                throws TimeoutException {

            Event event = new Event(tag,eventObj.getClass().getName(),returnClassName);
            String address = getAddressOf(event);
            if (!TextUtils.isEmpty(address)) {
                // 封装消息
                Bundle message = new Bundle();
                message.putString(KEY_TYPE,TYPE_VALUE);
                message.putString(KEY_ID,mId);
                message.putParcelable(KEY_EVENT_OBJ,eventObj);
                message.putString(KEY_TAG,tag);
                message.putString(KEY_RETURN_CLASS_NAME,returnClassName);
                long currentTime = SystemClock.uptimeMillis();
                mTransportLayer.send(address,message,timeout);
                long endTime = SystemClock.uptimeMillis();
                long leftTimeout = endTime - currentTime;
                mHasResult = false;
                while (!mHasResult) {
                    // 等待执行的结果返回
                    try {
                        mDoneSignal.await(leftTimeout, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ignore) {
                    }
                }
                mHasResult = false;
                return mResult;
            }
            return null;
        }
    }

    /**
     * 执行发布的订阅事件
     */
    private class ExecuteHandler implements MessageObserver {

        private long mDefaultTimeout = 1000 * 5;

        @Override
        public boolean handleMessage(String where, Bundle message) {

            String typeValue = message.getString(KEY_TYPE);
            if (PublishHandler.TYPE_VALUE.equals(typeValue)) {
                // 执行一个发布的订阅事件
                String id = message.getString(KEY_ID);
                Parcelable eventObj = message.getParcelable(PublishHandler.KEY_EVENT_OBJ);
                String tag = message.getString(PublishHandler.KEY_TAG);
                String returnClassName = message.getString(PublishHandler.KEY_RETURN_CLASS_NAME);
                Object returnValue = mEventBus.publish(eventObj, tag, returnClassName);
                // 执行结束，将此消息发送回去
                Bundle valueMessage = new Bundle();
                valueMessage.putString(KEY_TYPE,ResultHandler.TYPE_VALUE);
                valueMessage.putParcelable(ResultHandler.KEY_RETURN_VALUE, (Parcelable) returnValue);
                valueMessage.putString(KEY_ID,id);
                try {
                    mTransportLayer.send(where,valueMessage,mDefaultTimeout);
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

        private static final String TYPE_VALUE = "publish_event_value";

        /**
         * 订阅事件执行结束的返回值
         */
        private static final String KEY_RETURN_VALUE = "return_value";

        @Override
        public boolean handleMessage(String where, Bundle message) {

            String typeValue = message.getString(KEY_TYPE);
            if (TYPE_VALUE.equals(typeValue)) {
                // 接收到了一个订阅事件的执行结果
                Parcelable returnValue = message.getParcelable(KEY_RETURN_VALUE);
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

    @Override
    public void onMessageReceive(final String where, final Bundle message) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (MessageObserver observer : mMessageObserverList) {
                    if (observer.handleMessage(where, message)) {
                        break;
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
}
