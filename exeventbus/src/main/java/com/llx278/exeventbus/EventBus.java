package com.llx278.exeventbus;

/**
 * Created by llx on 2018/3/2.
 */

public interface EventBus {

    /**
     * 将此subscriber注册到EventBus上
     * @param subscriber 待注册的subscriber
     */
    void register(Object subscriber);

    /**
     * 从EventBus上取消一个subscriber
     * @param subscriber 待取消的subscriber
     */
    void unRegister(Object subscriber);


    /**
     * 向EventBus上面发布一个事件,eventObj和tag以及默认的void.getClass.getName()组成了一个唯一标志,
     * 此方法默认匹配当前进程中注册的所有事件
     * @param eventObj 待执行的发布对象
     * @param tag 这个事件的标志
     */
    void publish(Object eventObj,String tag);


    /**
     * 向EventBus上面发布一个事件,eventObj和tag以及returnCLassName组成了一个唯一标志,
     * 此方法默认匹配当前进程中注册的所有事件
     * @param eventObj 待执行的发布对象
     * @param tag 这个事件的标志
     * @param returnClassName 执行这个事件方法返回值的类的名字
     * @return 返回执行这个事件方法的返回值,如果有多个事件匹配那么会返回最先注册的那个方法的返回值
     *         请尽量确保有返回值的事件只匹配到一个方法。
     */
    Object publish(Object eventObj,String tag,String returnClassName);
}
