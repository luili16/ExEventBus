package com.llx278.exeventbus.remote;

import android.os.Bundle;

/**
 * 定义了消息处理的接口
 * Created by llx on 2018/3/3.
 */

public interface Receiver {

    void onMessageReceive(String where,Bundle message);

}
