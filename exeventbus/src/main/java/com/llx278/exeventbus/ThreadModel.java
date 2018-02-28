package com.llx278.exeventbus;

/**
 * 声明了订阅事件被执行时在哪个线程运行
 * Created by llx on 2018/2/4.
 */

public enum  ThreadModel {
    /**
     * 主线程执行
     */
    MAIN("main"),

    /**
     * 在{@link android.os.HandlerThread}中执行
     * 所有订阅事件以队列的形式在一个子线程的Looper中被执行
     */
    HANDLER("handler"),

    /**
     * 在发布订阅事件的那个线程中执行
     */
    POST("publish"),

    /**
     * 在一个自定义的线程池中执行
     */
    POOL("pool");

    private final String mName;

    ThreadModel(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName;
    }
}
