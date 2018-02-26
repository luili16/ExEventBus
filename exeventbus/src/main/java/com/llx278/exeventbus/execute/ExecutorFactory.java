package com.llx278.exeventbus.execute;

import com.llx278.exeventbus.ThreadModel;
import com.llx278.exeventbus.Type;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 生产executor
 * Created by llx on 2018/2/5.
 */

public class ExecutorFactory {

    private static final Map<String,Executor> sExecutorTemp = new ConcurrentHashMap<>();
    static {
        sExecutorTemp.put(ThreadModel.MAIN.toString(),new MainThreadExecutor());
        sExecutorTemp.put(ThreadModel.HANDLER.toString(),new HandlerThreadExecutor());
        sExecutorTemp.put(ThreadModel.POST.toString(),new PostThreadExecutor());
        sExecutorTemp.put(ThreadModel.POOL.toString(),new PoolThreadExecutor());
    }

    /**
     * 根据{@link ThreadModel}创建一个executor
     * @param threadModel 待匹配的线程模型
     * @return 创建的executor
     */
    public static Executor createExecutor(ThreadModel threadModel) {
        Executor executor = null;

        switch (threadModel) {
            case MAIN: {
                String key = ThreadModel.MAIN.toString();
                executor = sExecutorTemp.get(key);
                break;
            }
            case HANDLER:{
                String key = ThreadModel.HANDLER.toString();
                executor = sExecutorTemp.get(key);
                break;
            }
            case POST:{
                String key = ThreadModel.POST.toString();
                executor = sExecutorTemp.get(key);
                break;
            }

            case POOL:{
                String key = ThreadModel.POOL.toString();
                executor = sExecutorTemp.get(key);
                break;
            }
            default:
                throw new UnsupportedOperationException("unsupported thread model : " + threadModel + "!");
        }
        return executor;
    }
}
