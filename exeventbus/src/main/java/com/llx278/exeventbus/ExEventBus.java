package com.llx278.exeventbus;

import android.content.Context;
import android.os.Looper;

import com.llx278.exeventbus.exception.TimeoutException;

import java.util.ArrayList;

/**
 * 实现进程间的subscribe/publish
 * Created by llx on 2018/3/5.
 */

public class ExEventBus {

    private static ExEventBus sExEventBus;

    public static ExEventBus getDefault() {
        return sExEventBus;
    }

    /**
     * 推荐在Application的onCreate()方法内调用create
     * @param context context
     */
    public static void create(Context context) {
        if (sExEventBus != null) {
            throw new IllegalStateException("ExEventBus has been init!");
        }
        sExEventBus = new ExEventBus(context);
    }

    /**
     * ExEventBus应该在进程退出之前在合适的地方调用destroy()
     */
    public static void destroy() {
        if (sExEventBus != null) {
            sExEventBus.destroyInner();
            sExEventBus = null;
        }
    }

    private EventBus mEventBus;
    private Router mRouter;

    private ExEventBus(Context context) {

        mEventBus = new EventBus();
        mRouter = new Router(context,mEventBus);
    }

    private void destroyInner() {
        mRouter.destroy();
    }

    /**
     * 将此subscriber注册到EventBus上
     * 请另开一个线程来执行，
     * 因为底层用广播来做进程的消息同步，主线程执行会阻塞!
     * @param subscriber 待注册的subscriber
     */
    public void register(Object subscriber) {
        mEventBus.register(subscriber);
    }

    /**
     * 从EventBus上取消一个subscriber
     *  请另开一个线程来执行，
     * 因为底层用广播来做进程的消息同步，主线程执行会阻塞!
     * @param subscriber 待取消的subscriber
     */
    public void unRegister(Object subscriber) {
        mEventBus.unRegister(subscriber);
    }

    /**
     * 向EventBus上面发布一个事件,eventObj和tag以及默认的void.getClass.getName()和remote组成了一个唯一标志,
     * 此方法默认匹配当前进程中注册的所有事件
     *
     * @param eventObj 待执行的发布对象
     * @param tag      这个事件的标志
     */
    public void publish(Object eventObj, String tag) {
       mEventBus.publish(eventObj,tag);
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
    public Object publish(Object eventObj, String tag, String returnClassName){
        return mEventBus.publish(eventObj,tag,returnClassName);
    }

    public void remotePublish(Object eventObj, String tag,long timeout) throws TimeoutException {
        remotePublish(eventObj,tag,void.class.getName(),timeout);
    }

    /**
     * 向其他进程的EventBus发布一个事件,eventObj和tag、returnCLassName以及remote组成了一个唯一标志
     * 此方法将事件发布到其他的进程
     * 此方法不允许在主线程上执行，因为目前底层使用的是广播实现进程间的通讯，主线程执行会阻塞。
     * @param eventObj        待执行的发布对象
     * @param tag             这个事件的标志
     * @param returnClassName 执行这个事件方法返回值得类的名字
     * @return 返回执行这个事件方法的返回值，如果有多个事件匹配那么会返回最先注册的那个方法的返回值，
     * 请尽量确保有返回值的事件只匹配到一个方法
     */
    public Object remotePublish(Object eventObj, String tag, String returnClassName,long timeout)
            throws TimeoutException {
        return mRouter.route(eventObj,tag,returnClassName,timeout);
    }

    /**
     * 返回当前可发送的所有的进程的id
     * @return 当前已经连接的进程的列表
     */
    public ArrayList<Integer> getAvailableProcessId() {
        return mRouter.getAvailableProcessId();
    }
}
