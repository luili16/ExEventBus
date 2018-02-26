package com.llx278.exeventbus;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 封装了与订阅事件有关的查找，保存，以及删除。
 * Created by llx on 2018/2/4.
 */

class SubscribeHolder {

    /**
     * 默认注册事件的映射
     */
    private final Map<Event,CopyOnWriteArrayList<Subscription>> mDefaultMap = new ConcurrentHashMap<>();

    /**
     * 将传入的subscribe中所有被{@link Subscriber}所修饰的方法保存
     * @param subscribe 订阅事件
     */
    void put(Object subscribe) {
        Class<?> aClass = subscribe.getClass();
        while (aClass != null && !isSystemClass(aClass.getName())) {
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for (Method method:declaredMethods) {
                Subscriber annotation = method.getAnnotation(Subscriber.class);
                if (annotation != null) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes != null && parameterTypes.length == 1) {
                        Class<?> paramType = convertType(parameterTypes[0]);
                        Class<?> returnType = method.getReturnType();
                        Event defaultEvent = new Event(annotation.tag(),paramType,returnType);
                        CopyOnWriteArrayList<Subscription> subscriptionList = mDefaultMap.get(defaultEvent);
                        if (subscriptionList == null) {
                            subscriptionList = new CopyOnWriteArrayList<>();
                        }
                        Type type = annotation.type();
                        Subscription subscription = new Subscription(subscribe,method,annotation.model(),type);
                        subscriptionList.add(subscription);
                        mDefaultMap.put(defaultEvent,subscriptionList);
                    }
                }
            } // end for
            aClass = aClass.getSuperclass();
        }
    }

    /**
     * 移除指定的订阅事件
     * @param subscribe 订阅事件
     */
    void remove(Object subscribe) {
        Set<Map.Entry<Event, CopyOnWriteArrayList<Subscription>>> entries = mDefaultMap.entrySet();
        Iterator<Map.Entry<Event, CopyOnWriteArrayList<Subscription>>> entriesIterator = entries.iterator();
        while (entriesIterator.hasNext()) {
            Map.Entry<Event, CopyOnWriteArrayList<Subscription>> entry = entriesIterator.next();
            CopyOnWriteArrayList<Subscription> subscriptionList = entry.getValue();
            if (subscriptionList != null) {
                for (Subscription subscription : subscriptionList) {
                    Object cachedSubscribe = subscription.mSubscribeRef.get();
                    if (cachedSubscribe == null) {
                        subscriptionList.remove(subscription);
                    } else if (cachedSubscribe.equals(subscribe)) {
                        subscriptionList.remove(subscription);
                    }
                }
                if (subscriptionList.isEmpty()) {
                    entriesIterator.remove();
                }
            }
        }
    }

    CopyOnWriteArrayList<Subscription> get(Event event) {
        return mDefaultMap.get(event);
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
