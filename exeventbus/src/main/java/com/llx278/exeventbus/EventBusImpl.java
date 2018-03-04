package com.llx278.exeventbus;

import android.os.Parcelable;
import android.text.TextUtils;

import com.llx278.exeventbus.exception.IllegalRemoteArgumentException;
import com.llx278.exeventbus.execute.Executor;
import com.llx278.exeventbus.execute.ExecutorFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

    /**
     * 已经订阅的事件
     */
    private final Map<Event,CopyOnWriteArrayList<Subscription>> mSubscribedMap = new ConcurrentHashMap<>();

    private EventBusImpl() {
    }

    @Override
    public ArrayList<Event> register(Object subscriber) {
        if (subscriber == null) {
            return null;
        }
        ArrayList<Event> newAddedList = new ArrayList<>();
        Class<?> aClass = subscriber.getClass();
        while (aClass != null && !isSystemClass(aClass.getName())) {
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for (Method method:declaredMethods) {
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
                        Event newEvent = new Event(annotation.tag(),paramType.getName(),
                                returnType.getName(),isRemote);
                        CopyOnWriteArrayList<Subscription> subscriptionList =
                                mSubscribedMap.get(newEvent);
                        if (subscriptionList == null) {
                            subscriptionList = new CopyOnWriteArrayList<>();
                            newAddedList.add(newEvent);
                        }
                        Type type = annotation.type();
                        Subscription subscription = new Subscription(subscriber,method,
                                annotation.model(),type);
                        subscriptionList.add(subscription);
                        mSubscribedMap.put(newEvent,subscriptionList);
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
        if (!isParcelable || !isSerializable) {
            throw new IllegalRemoteArgumentException("param subscriber must " +
                    "implements Parcelable or serializable!");
        }
        isParcelable = false;
        isSerializable = false;
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
        if (!isParcelable || isSerializable) {
            throw new IllegalRemoteArgumentException("return value must " +
                    "implements Parcelable or Serializable!");
        }
    }


    @Override
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
        return publish(eventObj,tag,returnClassName,false);
    }

    @Override
    public Object remotePublish(Object eventObj, String tag, String returnClassName) {
        return publish(eventObj,tag,returnClassName,true);
    }

    private Object publish(Object eventObj, String tag, String returnClassName,boolean isRemote) {
        if (eventObj == null || TextUtils.isEmpty(tag)) {
            Logger.e("LocalEventBus.publish(Object,String,Class) param Object or tag or class is null!!", null);
            return null;
        }

        Event event = new Event(tag, eventObj.getClass().getName(), returnClassName,isRemote);
        CopyOnWriteArrayList<Subscription> subscriptionList = mSubscribedMap.get(event);
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


    @Override
    public ArrayList<Event> query() {
        Set<Event> eventsSet = mSubscribedMap.keySet();
        ArrayList<Event> eventList = new ArrayList<>();
        if (!eventsSet.isEmpty()) {
            eventList.addAll(eventsSet);
        }
        return eventList;
    }

    Map<Event,CopyOnWriteArrayList<Subscription>> getDefaultMap() {
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
