package com.llx278.exeventbus.remote;

import android.os.Bundle;

/**
 * 此接口保证了消息可以准确的发送到其他的进程
 * Created by llx on 2018/2/28.
 */

public interface IControlLayer {

    void send(String address, Bundle message);

    void send(String address,Bundle message,long timeout);

    void receive(String where,Bundle message);
}
