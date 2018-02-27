package com.llx278.exeventbus.remote;

import android.os.Bundle;

/**
 * 此接口提供了接收消息的功能
 * Created by llx on 2018/2/27.
 */

public interface IReceiver {

    void init();

    /**
     * 接收到一条消息
     * @param where 从哪里收到
     * @param message 消息实体
     */
    void receive(String where, Bundle message);

    void destroy();
}
