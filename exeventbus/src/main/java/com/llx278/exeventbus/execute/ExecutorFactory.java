package com.llx278.exeventbus.execute;

import com.llx278.exeventbus.ThreadModel;

/**
 * 生产executor
 * Created by llx on 2018/2/5.
 */

public class ExecutorFactory {

    /**
     * 根据{@link ThreadModel}创建一个executor
     * @param threadModel 待匹配的线程模型
     * @return 创建的executor
     */
    public static Executor createExecutor(ThreadModel threadModel) {
        Executor executor = null;
        switch (threadModel) {
            case MAIN:
                executor = new MainThreadExecutor();
                break;
            case HANDLER:
                executor = new HandlerThreadExecutor();
                break;
            case POST:
                executor = new PostThreadExecutor();
                break;
            case POOL:
                executor = new PoolThreadExecutor();
                break;
            default:
        }
        return executor;
    }
}
