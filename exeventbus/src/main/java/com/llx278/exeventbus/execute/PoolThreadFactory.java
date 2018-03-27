package com.llx278.exeventbus.execute;

import android.support.annotation.NonNull;

import java.util.concurrent.ThreadFactory;

/**
 * 对线程命名
 * Created by llx on 2018/3/26.
 */

public class PoolThreadFactory implements ThreadFactory {
    private static final String THREAD_NAME = "ExEventBus-pool_thread";
    private int mNum = 0;

    @Override
    public Thread newThread(@NonNull Runnable r) {
        mNum++;
        return new Thread(r,THREAD_NAME + "-" + mNum);
    }
}
