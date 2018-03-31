package com.llx278.exeventbus.remote;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * IMockPhysicalLayer定义了一个进程如何通信
 * Created by llx on 2018/2/28.
 */

public interface IMockPhysicalLayer {

    void init();

    void destroy();

    /**
     * 一条消息通过send方法发送给另一个进程
     * @param address 用一个字符串抽象出一个进程的具体地址
     * @param message 发送的消息体，考虑到android进程间通讯的机制，用Bundle来封装消息的内容这样可以传递
     *                复杂的对象
     */
    void send(String address, Bundle message);

    /**
     * 设置监听接口
     * @param listener
     */
    void setOnReceiveListener(Receiver listener);

    /**
     * 获得可用的地址
     * @return 可用的地址
     */
    ArrayList<String> getAvailableAddress();
}
