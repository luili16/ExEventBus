package com.llx278.exeventbus;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.llx278.exeventbus.exception.IllegalRemoteArgumentException;
import com.llx278.exeventbus.exception.TimeoutException;
import com.llx278.exeventbus.remote.Address;
import com.llx278.exeventbus.remote.IMockPhysicalLayer;
import com.llx278.exeventbus.remote.ITransportLayer;
import com.llx278.exeventbus.remote.MockPhysicalLayer;
import com.llx278.exeventbus.remote.Receiver;
import com.llx278.exeventbus.remote.TransportLayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
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
     * 向其他进程的总线上发布一个订阅事件
     */
    private static final String TYPE_VALUE_OF_PUBLISH = "publish_event";
    /**
     * 向其他进程的总线上发布此订阅事件执行后返回值
     */
    private static final String TYPE_VALUE_OF_PUBLISH_RETURN_VALUE = "publish_event_value";

    /**
     * 此key封装了查询订阅事件的列表
     */
    private static final String KEY_QUERY_LIST = "query_list";
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
    private static final String KEY_ID = "router_id";

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
     * 保存了每个消息所对应那一时刻的事件列表。
     * 我没有考虑缓存所有进程的事件列表的原因是每个进程随时都有可能被杀死，这样的缓存列表很难维护，到不如
     * 每次都做一次查询，这样虽然效率会低点，但能避免很多bug。
     */
    private ConcurrentHashMap<String,EventListHolder> mSubscribeEventListSnapshot =
            new ConcurrentHashMap<>();

    Router(Context context, EventBus eventBus) {
        IMockPhysicalLayer physicalLayer = new MockPhysicalLayer(context);
        mTransportLayer = new TransportLayer(physicalLayer);
        mTransportLayer.setOnReceiveListener(this);
        mExecutor = Executors.newCachedThreadPool(new RouterThread());
        mEventBus = eventBus;

        mMessageObserverList.add(new ResultHandler());
        mMessageObserverList.add(new ExecuteHandler());
        mMessageObserverList.add(new QueryHandler());
        mMessageObserverList.add(new QueryResultHandler());
    }

    ArrayList<Integer> getAvailableProcessId() {
        ArrayList<String> availableAddress = mTransportLayer.getAvailableAddress();
        ArrayList<Integer> processIdList = new ArrayList<>();
        for (String address : availableAddress) {
            Address address1 = Address.toAddress(address);
            if (address1 != null) {
                processIdList.add(address1.getPid());
            }
        }
        return processIdList;
    }

    /**
     * 将此事件发送到其他的进程中执行
     * 注意，当一个事件被发布到多个进程中执行的时候，如果returnClassName是void.class.getName(),那么
     * 则只会只将此事件发送到其他的进程执行，直接返回。否则会等待，直到返回当前的执行后的结果。
     *
     * @return 返回执行的结果，如果方法的返回值是void，则默认返回null，如果不是，则返回执行后的结果
     * @throws TimeoutException 超时
     */
    Object route(Object eventObject, String tag, String returnClassName, long timeout)
            throws TimeoutException {
        ArrayList<String> availableAddress = mTransportLayer.getAvailableAddress();

        if (availableAddress == null || availableAddress.isEmpty()) {
            ELogger.e("no available address",null);
            return null;
        }
        long currentTime = SystemClock.uptimeMillis();
        String id = UUID.randomUUID().toString();
        CountDownLatch signal = new CountDownLatch(availableAddress.size());
        mSubscribeEventListSnapshot.put(id,new EventListHolder(signal));

        // 发送消息
        Bundle message = new Bundle();
        message.putString(KEY_ID,id);
        message.putString(KEY_TYPE,TYPE_VALUE_OF_QUERY);
        mTransportLayer.sendBroadcast(message);

        try {
            if (!signal.await(timeout,TimeUnit.MILLISECONDS)) {
                mSubscribeEventListSnapshot.remove(id);
                throw new TimeoutException("wait for other process respond timeout");
            }
        } catch (InterruptedException ignore) {
        }
        Event event = new Event(tag, eventObject.getClass().getName(), returnClassName, true);
        ConcurrentHashMap<String, ArrayList<Event>> eventList = mSubscribeEventListSnapshot.get(id).mEventList;
        ArrayList<String> eventAddressList = getAddressOf(event,eventList);
        if (eventAddressList.isEmpty()) {
            return null;
        }
        mSubscribeEventListSnapshot.remove(id);
        long elapsedTime = SystemClock.uptimeMillis() - currentTime;
        long leftTime = timeout - elapsedTime;
        if (leftTime <= 10) {
            return null;
        }
        if (returnClassName.equals(void.class.getName())) {
            for (String address : eventAddressList) {
                route(address, eventObject, tag, returnClassName, leftTime);
            }
            return null;
        } else {
            String address = eventAddressList.get(0);
            return route(address, eventObject, tag, returnClassName, leftTime);
        }
    }

    /**
     * 将事件发送到指定进程执行
     *
     * @return 返回执行的结果，如果方法的返回值是void，则默认返回null，如果不是，则返回执行后的结果
     */
    private Object route(String address, Object eventObject, String tag, String returnClassName, long timeout)
            throws TimeoutException {
        PublishHandler publishHandler = new PublishHandler(address);
        return publishHandler.publishToRemote(eventObject, tag, returnClassName, timeout);
    }

    private ArrayList<Event> filterNoRemoteEventList(ArrayList<Event> newEventArrayList) {
        Iterator<Event> iterator = newEventArrayList.iterator();
        while (iterator.hasNext()) {
            Event nextEvent = iterator.next();
            if (!nextEvent.isRemote()) {
                iterator.remove();
            }
        }
        return newEventArrayList;
    }

    private ArrayList<String> getAddressOf(Event event, ConcurrentHashMap<String, ArrayList<Event>> evenList) {
        ArrayList<String> addressList = new ArrayList<>();
        if (evenList == null) {
            return addressList;
        }
        Set<Map.Entry<String, ArrayList<Event>>> entries = evenList.entrySet();
        for (Map.Entry<String, ArrayList<Event>> entry : entries) {
            ArrayList<Event> eventValueList = entry.getValue();
            for (Event testedEvent : eventValueList) {
                if (testedEvent.equals(event)) {
                    addressList.add(entry.getKey());
                }
            }
        }
        return addressList;
    }

    @Override
    public void onMessageReceive(final String where, final Bundle message) {
        if (mExecutor != null && !mExecutor.isShutdown()) {
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
    }

    public void destroy() {
        mTransportLayer.destroy();
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
        private final String mId;

        /**
         * 返回的结果
         */
        private Object mResult;

        private final CountDownLatch mDoneSignal;

        private final String mAddress;

        PublishHandler(String address) {
            mId = UUID.randomUUID().toString();
            mDoneSignal = new CountDownLatch(1);
            mAddress = address;
        }

        Object publishToRemote(Object eventObj, String tag, String returnClassName, long timeout)
                throws TimeoutException {

            // 封装消息
            Bundle message = new Bundle();
            message.putString(KEY_TYPE, TYPE_VALUE_OF_PUBLISH);
            message.putString(KEY_ID, mId);
            if (eventObj instanceof Serializable) {
                message.putSerializable(KEY_EVENT_OBJ, (Serializable) eventObj);
            } else if (eventObj instanceof Parcelable) {
                message.putParcelable(KEY_EVENT_OBJ, (Parcelable) eventObj);
            } else {
                throw new IllegalRemoteArgumentException("eventObj(" + eventObj.getClass().getName()
                        + ") is not implement Serializable or Parcelable");
            }
            message.putString(KEY_TAG, tag);
            message.putString(KEY_RETURN_CLASS_NAME, returnClassName);

            if (returnClassName.equals(void.class.getName())) {
                // 空类型直接发送，不需要等待返回值
                mTransportLayer.send(mAddress, message, timeout);
            } else {
                // 其他类型需要缓存执行的事件，等待执行结果的返回
                mWaitingExecuteReturnValueMap.put(mId, this);
                long currentTime = SystemClock.uptimeMillis();
                mTransportLayer.send(mAddress, message, timeout);
                long endTime = SystemClock.uptimeMillis();
                long elapsedTime = endTime - currentTime;
                long leftTimeout = timeout - elapsedTime;
                try {
                    if (!mDoneSignal.await(leftTimeout, TimeUnit.MILLISECONDS)) {
                        mWaitingExecuteReturnValueMap.remove(mId);
                        // 等待超时
                        throw new TimeoutException("wait publish result timeout!");
                    }
                } catch (InterruptedException ignore) {
                    Log.e("main", "", ignore);
                }
                // 删除已经执行结束的事件
                mWaitingExecuteReturnValueMap.remove(mId);
                return mResult;
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
                String id = message.getString(KEY_ID);
                Bundle valueMessage = new Bundle();
                valueMessage.putString(KEY_ID, id);
                valueMessage.putString(KEY_TYPE, TYPE_VALUE_OF_QUERY_RESULT);
                valueMessage.setClassLoader(getClass().getClassLoader());
                valueMessage.putParcelableArrayList(KEY_QUERY_LIST, filterNoRemoteEventList(allEvents));
                try {
                    mTransportLayer.send(where, valueMessage, sDefaultTimeout);
                } catch (TimeoutException e) {
                    ELogger.e(TAG, "send message[typeValue = " + typeValue + " allEvents = " +
                            allEvents + "]", e);
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
                ArrayList<Event> queryEvents = message.getParcelableArrayList(KEY_QUERY_LIST);
                String id = message.getString(KEY_ID);
                EventListHolder eventListHolder = mSubscribeEventListSnapshot.get(id);
                if (queryEvents != null) {
                    eventListHolder.mEventList.put(where,queryEvents);
                }
                eventListHolder.mSignal.countDown();
                return true;
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
                Object eventObj = message.get(KEY_EVENT_OBJ);
                String tag = message.getString(KEY_TAG);
                String returnClassName = message.getString(KEY_RETURN_CLASS_NAME);
                Object returnValue = mEventBus.publish(eventObj, tag, returnClassName, true);
                if (!TextUtils.isEmpty(returnClassName) && !returnClassName.equals(void.class.getName())) {
                    // 只有返回值类型是非空的才会发送回去
                    Bundle valueMessage = new Bundle();
                    valueMessage.putString(KEY_TYPE, TYPE_VALUE_OF_PUBLISH_RETURN_VALUE);
                    if (returnValue != null) {
                        if (returnValue instanceof Serializable) {
                            valueMessage.putSerializable(KEY_RETURN_VALUE, (Serializable) returnValue);
                        } else if (returnValue instanceof Parcelable) {
                            valueMessage.putParcelable(KEY_RETURN_VALUE, (Parcelable) returnValue);
                        } else {
                            throw new IllegalRemoteArgumentException("eventObj(" + returnValue.getClass().getName()
                                    + ") is not implement Serializable or Parcelable");
                        }
                    }
                    valueMessage.putString(KEY_ID, id);
                    try {
                        mTransportLayer.send(where, valueMessage, sDefaultTimeout);
                    } catch (TimeoutException e) {
                        ELogger.e(TAG, "send message[typeValue = " + typeValue + " rturnvalue = " +
                                returnValue + "]", e);
                    }
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
                Object returnValue = message.get(KEY_RETURN_VALUE);
                String id = message.getString(KEY_ID);
                if (!TextUtils.isEmpty(id)) {
                    PublishHandler mWaitingPublishHandler = mWaitingExecuteReturnValueMap.get(id);
                    if (mWaitingPublishHandler != null) {
                        mWaitingPublishHandler.mResult = returnValue;
                        mWaitingPublishHandler.mDoneSignal.countDown();
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private class RouterThread implements ThreadFactory {

        private static final String NAME = "router_thread-";
        private int mNum = 0;

        @Override
        public Thread newThread(@NonNull Runnable r) {
            mNum++;
            if (mNum == Integer.MAX_VALUE) {
                mNum = 0;
            }
            return new Thread(r,NAME + mNum);
        }
    }

    private class EventListHolder {
        private final CountDownLatch mSignal;
        private final ConcurrentHashMap<String,ArrayList<Event>> mEventList;
        EventListHolder(CountDownLatch signal) {
            mSignal = signal;
            mEventList = new ConcurrentHashMap<>();
        }
    }
}
