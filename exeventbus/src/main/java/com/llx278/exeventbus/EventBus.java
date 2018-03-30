package com.llx278.exeventbus;

import android.os.Parcelable;
import android.text.TextUtils;

import com.llx278.exeventbus.exception.IllegalRemoteArgumentException;
import com.llx278.exeventbus.execute.Executor;
import com.llx278.exeventbus.execute.ExecutorFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 实现进程内的subscribe/publish
 * Created by llx on 2018/3/2.
 */

class EventBus {

    /**
     * 已经订阅的事件
     */
    private final Map<Event, CopyOnWriteArrayList<Subscription>> mSubscribedMap = new ConcurrentHashMap<>();


    EventBus() {
    }

    /**
     * 将此subscriber注册到EventBus上
     *
     * @param subscriber 待注册的subscriber
     * @return 返回此次注册的subscriber所生成的订阅事件列表
     * @throws IllegalStateException 当订阅的方法的返回值不为空的时候，每一个订阅的消息只允许有一个可以执行的方法，
     *                               并且Type固定为BLOCK_RETURN。不符合此限制则会抛出IllegalStateException
     */
    public ArrayList<Event> register(Object subscriber) throws IllegalStateException {
        if (subscriber == null) {
            return null;
        }
        ArrayList<Event> newAddedList = new ArrayList<>();
        Class<?> aClass = subscriber.getClass();
        while (aClass != null && !isSystemClass(aClass.getName())) {
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for (Method method : declaredMethods) {
                Subscriber annotation = method.getAnnotation(Subscriber.class);
                if (annotation != null) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes != null && parameterTypes.length == 1) {
                        Class<?> paramType = convertType(parameterTypes[0]);
                        Class<?> returnType = method.getReturnType();
                        boolean isRemote = annotation.remote();
                        // 此订阅事件可以被远程执行
                        if (isRemote) {
                            checkRemoteParam(paramType, returnType);
                        }
                        Event newEvent = new Event(annotation.tag(), paramType.getName(),
                                returnType.getName(), isRemote);
                        CopyOnWriteArrayList<Subscription> subscriptionList =
                                mSubscribedMap.get(newEvent);
                        if (subscriptionList == null) {
                            subscriptionList = new CopyOnWriteArrayList<>();
                            newAddedList.add(newEvent);
                        }
                        Type type = annotation.type();
                        Subscription subscription = new Subscription(subscriber, method,
                                annotation.model(), type);
                        subscriptionList.add(subscription);
                        // 判断一下返回值的类型
                        if (!returnType.equals(void.class)) {
                            if (subscriptionList.size() > 1 || !annotation.type().equals(Type.BLOCK_RETURN)) {
                                throw new IllegalStateException("more than one subscription or wrong Type");
                            }
                        }
                        mSubscribedMap.put(newEvent, subscriptionList);
                    }

                }
            }
            aClass = aClass.getSuperclass();
        }
        return newAddedList;
    }

    private void checkRemoteParam(Class<?> paramType, Class<?> returnType) {
        boolean isSerializable = false;
        boolean isParcelable = false;
        Class<?>[] paramInterfaces = paramType.getInterfaces();
        if (paramInterfaces != null && paramInterfaces.length > 0) {
            for (Class<?> interfaceClass : paramInterfaces) {
                if (interfaceClass.equals(Serializable.class)) {
                    isSerializable = true;
                } else if (interfaceClass.equals(Parcelable.class)) {
                    isParcelable = true;
                }
            }
        }
        // 既没有实现Serializable也没有实现Parcelable
        if (!isParcelable && !isSerializable) {
            throw new IllegalRemoteArgumentException("subscriber param(" + paramType.getName() + ") must " +
                    "implements Parcelable or serializable!");
        }
        isParcelable = false;
        isSerializable = false;

        if (returnType.equals(void.class)) {
            // 忽略返回值是void类型，因为void类型并不会向其他进程返回数据
            return;
        }

        Class<?>[] returnParamInterfaces = returnType.getInterfaces();
        if (returnParamInterfaces != null && returnParamInterfaces.length > 0) {
            for (Class<?> returnParamInterface : returnParamInterfaces) {
                if (returnParamInterface.equals(Serializable.class)) {
                    isSerializable = true;
                } else if (returnParamInterface.equals(Parcelable.class)) {
                    isParcelable = true;
                }
            }
        }
        // 既没有实现Serializable也没有实现Parcelable
        if (!isParcelable && !isSerializable) {
            throw new IllegalRemoteArgumentException("return value(" + returnType.getName() + ") must" +
                    "implements Parcelable or Serializable!");
        }
    }

    /**
     * 从EventBus上取消一个subscriber
     *
     * @param subscriber 待取消的subscriber
     * @return 返回此次注册的subscriber所移除的订阅事件列表
     */
    public ArrayList<Event> unRegister(Object subscriber) {
        if (subscriber == null) {
            return null;
        }
        ArrayList<Event> removedEventList = new ArrayList<>();
        Set<Map.Entry<Event, CopyOnWriteArrayList<Subscription>>> entries = mSubscribedMap.entrySet();
        Iterator<Map.Entry<Event, CopyOnWriteArrayList<Subscription>>> entriesIterator = entries.iterator();
        while (entriesIterator.hasNext()) {
            Map.Entry<Event, CopyOnWriteArrayList<Subscription>> entry = entriesIterator.next();
            CopyOnWriteArrayList<Subscription> subscriptionList = entry.getValue();
            if (subscriptionList != null) {
                for (Subscription subscription : subscriptionList) {
                    Object cachedSubscribe = subscription.mSubscribeRef.get();
                    if (cachedSubscribe == null) {
                        subscriptionList.remove(subscription);
                    } else if (cachedSubscribe.equals(subscriber)) {
                        subscriptionList.remove(subscription);
                    }
                }
                if (subscriptionList.isEmpty()) {
                    entriesIterator.remove();
                    removedEventList.add(entry.getKey());
                }
            }
        }
        return removedEventList;
    }

    /**
     * 向EventBus上面发布一个事件,eventObj和tag以及默认的void.getClass.getName()和remote组成了一个唯一标志,
     * 此方法默认匹配当前进程中注册的所有事件
     *
     * @param eventObj 待执行的发布对象
     * @param tag      这个事件的标志
     */
    public void publish(Object eventObj, String tag) {
        if (eventObj == null || TextUtils.isEmpty(tag)) {
            ELogger.e("LocalEventBus.publish(Object,String) param Object or tag is null!!", null);
            return;
        }
        publish(eventObj, tag, void.class.getName());
    }

    /**
     * 向EventBus上面发布一个事件,eventObj和tag、returnCLassName以及remote组成了一个唯一标志,
     * 此方法默认匹配当前进程中注册的所有事件
     *
     * @param eventObj        待执行的发布对象
     * @param tag             这个事件的标志
     * @param returnClassName 执行这个事件方法返回值的类的名字
     * @return 返回执行这个事件方法的返回值, 如果有多个事件匹配那么会返回最先注册的那个方法的返回值
     * 请尽量确保有返回值的事件只匹配到一个方法。
     */
    public Object publish(Object eventObj, String tag, String returnClassName) {
        return publish(eventObj, tag, returnClassName, false);
    }


    Object publish(Object eventObj, String tag, String returnClassName, boolean isRemote) {
        if (eventObj == null || TextUtils.isEmpty(tag) || TextUtils.isEmpty(returnClassName)) {
            ELogger.e("LocalEventBus.publish(Object,String,Class) param Object or tag or " +
                    "class is null!!", null);
            return null;
        }

        Event event = new Event(tag, eventObj.getClass().getName(), returnClassName, isRemote);
        return publish(event, eventObj);
    }

    private Object publish(Event event, Object eventObj) {
        CopyOnWriteArrayList<Subscription> subscriptionList = mSubscribedMap.get(event);
        if (subscriptionList != null) {
            for (Subscription subs : subscriptionList) {
                Executor executor = ExecutorFactory.createExecutor(subs.mThreadModel);
                Object subscribe = subs.mSubscribeRef.get();
                if (subscribe != null) {
                    if (subs.mType == Type.BLOCK_RETURN) {
                        // 因为返回值只能有一个,所以默认只是第一个注册的有效
                        return executor.submit(subs.mMethod, eventObj, subscribe);
                    } else if (subs.mType == Type.DEFAULT) {
                        executor.execute(subs.mMethod, eventObj, subscribe);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 查询当前已经订阅事件的列表
     *
     * @return 返回当前已经订阅的事件的列表
     */
    public ArrayList<Event> query() {
        Set<Event> eventsSet = mSubscribedMap.keySet();
        ArrayList<Event> eventList = new ArrayList<>();
        if (!eventsSet.isEmpty()) {
            eventList.addAll(eventsSet);
        }
        return eventList;
    }

    Map<Event, CopyOnWriteArrayList<Subscription>> getDefaultMap() {
        return mSubscribedMap;
    }

    private Class<?> convertType(Class<?> eventType) {
        Class<?> returnClass = eventType;
        if (eventType.equals(boolean.class)) {
            returnClass = Boolean.class;
        } else if (eventType.equals(int.class)) {
            returnClass = Integer.class;
        } else if (eventType.equals(float.class)) {
            returnClass = Float.class;
        } else if (eventType.equals(double.class)) {
            returnClass = Double.class;
        }
        return returnClass;
    }

    private boolean isSystemClass(String name) {
        return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.");
    }
}
