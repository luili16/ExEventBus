package com.llx278.exeventbus.remote;

import android.os.Bundle;

/**
 * TransferLayer实现了进程消息通信的通路，但并不对消息是否准确送达负责
 * Created by llx on 2018/2/28.
 */

public interface ITransferLayer {

    /**
     * 一条消息通过send方法发送给另一个进程
     * @param address 用一个字符串抽象出一个进程的具体地址
     * @param message 发送的消息体，考虑到android进程间通讯的机制，用Bundle来封装消息的内容这样可以传递
     *                复杂的对象
     */
    void send(String address, Bundle message);

    /**
     * 一条消息通过receive方法接收从另一个进程发送过来的消息
     * @param where 用一个字符串抽象出一条消息具体由哪个进程发送的
     * @param message 接收的消息体 考虑到android进程间通讯的机制，用Bundle来疯转消息的内容，这样可以传递
     *                复杂的对象
     */
    void receive(String where,Bundle message);
}
