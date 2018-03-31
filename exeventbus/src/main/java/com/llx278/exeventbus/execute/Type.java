package com.llx278.exeventbus.execute;

/**
 * Created by llx on 2018/2/26.
 */

public enum Type {

    /**
     * 默认的订阅类型，发布事件的线程与执行事件的线程互不影响
     */
    DEFAULT,

    /**
     * 订阅一个阻塞的事件，发布事件的线程将会阻塞，直到执行完毕返回
     */
    BLOCK_RETURN
}
