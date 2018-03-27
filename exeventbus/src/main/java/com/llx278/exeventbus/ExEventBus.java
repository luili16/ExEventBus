package com.llx278.exeventbus;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

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
     * 推荐在Application的onCreate()方法内调用create，但请另开一个线程来执行，
     * 因为底层用广播来做进程的消息同步，主线程执行会阻塞!
     * @param context context
     */
    public static void create(Context context) {
        if (sExEventBus != null) {
            throw new IllegalStateException("ExEventBus has been init!");
        }
        checkThread();
        sExEventBus = new ExEventBus(context);
    }

    /**
     * ExEventBus应该在进程退出之前在合适的地方调用destroy()，这主要是为了解除广播的注册
     * 但请另开一个线程来执行，
     * 因为底层用广播来做进程的消息同步，主线程执行会阻塞!
     */
    public static void destroy() {
        checkThread();
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
        checkThread();
        ArrayList<Event> newEventList = mEventBus.register(subscriber);
        mRouter.add(newEventList);
    }

    /**
     * 从EventBus上取消一个subscriber
     *  请另开一个线程来执行，
     * 因为底层用广播来做进程的消息同步，主线程执行会阻塞!
     * @param subscriber 待取消的subscriber
     */
    public void unRegister(Object subscriber) {
        checkThread();
        ArrayList<Event> removedEventList = mEventBus.unRegister(subscriber);
        mRouter.remove(removedEventList);
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
     * 向EventBus上面发布一个粘滞事件，粘滞事件不支持返回值
     * 注意：不要尝试粘滞发布一个Type为:{@link Type#BLOCK_RETURN}事件，因为当对应事件在总线上注册之后会
     * 立即执行，如果TYpe为{@link Type#BLOCK_RETURN}的话会阻塞注册的过程，如果注册的过程是在主线程执行的话
     * 那么可能会引起页面无响应.
     * @param eventObj 待执行的发布对象
     * @param tag      这个事件的标志
     */
    public void stickyPublish(Object eventObj, String tag) {
       mEventBus.stickyPublish(eventObj,tag);
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
        checkThread();
        return mRouter.route(eventObj,tag,returnClassName,timeout);
    }

    /**
     * 远程发布一个粘滞事件,粘滞事件的返回值没有意义
     * 如果此事件已经注册了，那么就直接执行，如果没有注册，那么当接收到注册此事件的进程发送的注册信息以后
     * 立即执行，而此方法并不会等待其他进程的执行结果
     */
    public void stickyRemotePublish(Object eventObject, String tag, long timeout) throws TimeoutException {
        checkThread();
        mRouter.stickyRoute(eventObject,tag,timeout);
    }

    private static void checkThread() {
        Looper myLooper = Looper.myLooper();
        Looper mainLooper = Looper.getMainLooper();
        if (myLooper == mainLooper) {
            throw new RuntimeException("Don't remote publish an Event in Main Thread!");
        }
    }
}
