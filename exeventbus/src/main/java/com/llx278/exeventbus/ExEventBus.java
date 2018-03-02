package com.llx278.exeventbus;

import android.os.Parcelable;

import com.llx278.exeventbus.EventBus;

/**
 * IRouter添加了进程间的消息订阅
 * Created by llx on 2018/3/2.
 */

public interface ExEventBus extends EventBus {

    /**
     * 向EventBus上面发布一个事件,eventObj和tag以及returnClassName组成了一个用来标志这个事件的唯一标志
     * @param eventObj 待执行的发布对象
     * @param tag 这个事件的标志
     * @param returnClassName 执行这个事件方法返回值的类的名字
     * @return 返回执行这个事件方法的返回值,如果有多个事件匹配那么会返回最先注册的那个方法的返回值
     *         请尽量确保有返回值的事件只匹配到一个方法。
     */
    Parcelable publishToRemote(Parcelable eventObj, String tag, String returnClassName);
}
