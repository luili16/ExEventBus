package com.llx278.exeventbus.remote;

import android.os.Bundle;

/**
 * 此接口提供了发送消息的功能
 * Created by llx on 2018/2/27.
 */

public interface ISender {
    /**
     * 发送一条消息
     * @param address 消息地址
     * @param message 消息体
     */
    void send(String address, Bundle message);
}
