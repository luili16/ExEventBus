package com.llx278.exeventbus.entry;


import android.util.Log;

import com.llx278.exeventbus.ExEventBus;
import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.ThreadModel;
import com.llx278.exeventbus.Type;
import com.llx278.exeventbus.event.Event10;
import com.llx278.exeventbus.event.Event11;
import com.llx278.exeventbus.event.Event8;
import com.llx278.exeventbus.event.Event9;
import com.llx278.exeventbus.remote.Address;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * 测试执行默认tag
 * Created by llx on 2018/2/6.
 */

public class SubscribeEntry8 {

    private final CountDownLatch mDownSignal;

    public String mTestMethod1Tag;
    public String mTestMethod2Tag;
    public String mTestMethod3Tag;
    public String mTestMethod4Tag;


    public SubscribeEntry8(CountDownLatch downSignal) {
        mDownSignal = downSignal;
    }

    public SubscribeEntry8() {
        this(null);
    }

    @Subscriber(tag = "event8", model = ThreadModel.POOL, type = Type.BLOCK_RETURN,remote = true)
    public void testMethod1(Event8 event8) {
        assertNotNull(event8);
        mTestMethod1Tag = event8.getMsg();
        String splite[] = event8.getMsg().split("#");
        String body = splite[0];
        String tag = splite[1];
        String uuid = splite[2];

        Event8 returnEvent = new Event8();
        String returnClassName = void.class.getName();
        String address = Address.createOwnAddress().toString();
        String msg = body + "#" + address+uuid;
        returnEvent.setMsg(msg);
        ExEventBus.getDefault().remotePublish(returnEvent,tag,returnClassName,1000 * 2);
    }

    @Subscriber(tag = "event9_SubscribeEntry8", model = ThreadModel.MAIN, type = Type.BLOCK_RETURN, remote = true)
    public String testMethod2(Event9 event9) {
        assertNotNull(event9);
        return "return_" + event9.getMsg();
    }

    @Subscriber(tag = "event10_SubscribeEntry8", model = ThreadModel.HANDLER, type = Type.BLOCK_RETURN, remote = true)
    public String testMethod3(Event10 event10) {
        assertNotNull(event10);
        return "return_" + event10.getMsg();
    }

    @Subscriber(tag = "event11_SubscribeEntry8", model = ThreadModel.POST, type = Type.BLOCK_RETURN, remote = true)
    public String testMethod4(Event11 event11) {
        assertNotNull(event11);
        return "return_" + event11.getMsg();
    }
}
