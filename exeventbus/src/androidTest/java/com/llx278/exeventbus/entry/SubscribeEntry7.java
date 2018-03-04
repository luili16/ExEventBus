package com.llx278.exeventbus.entry;

import com.llx278.exeventbus.EventBusImpl;
import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.ThreadModel;
import com.llx278.exeventbus.Type;
import com.llx278.exeventbus.event.Event10;
import com.llx278.exeventbus.event.Event11;
import com.llx278.exeventbus.event.Event4;
import com.llx278.exeventbus.event.Event5;
import com.llx278.exeventbus.event.Event8;
import com.llx278.exeventbus.event.Event9;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertNotNull;

/**
 * 测试执行默认tag
 * Created by llx on 2018/2/6.
 */

public class SubscribeEntry7 {

    private final CountDownLatch mDownSignal;

    public SubscribeEntry7(CountDownLatch downSignal) {
        mDownSignal = downSignal;
    }
    public SubscribeEntry7() {
        this(null);
    }

    public void register() {
        EventBusImpl.getDefault().register(this);
    }

    public void unRegister() {
        EventBusImpl.getDefault().unRegister(this);
    }


    @Subscriber(tag = "event8",model = ThreadModel.POOL,type = Type.BLOCK_RETURN)
    public String testMethod1(Event8 event8) {
        assertNotNull(event8);
        return "return_" + event8.getMsg();
    }

    @Subscriber(tag = "event9",model = ThreadModel.MAIN,type = Type.BLOCK_RETURN)
    public String testMethod2(Event9 event9) {
        assertNotNull(event9);
        return "return_" + event9.msg;
    }

    @Subscriber(tag = "event10",model = ThreadModel.HANDLER,type = Type.BLOCK_RETURN)
    public String testMethod3(Event10 event10) {
        assertNotNull(event10);
        return "return_"+event10.msg;
    }

    @Subscriber(tag = "event11",model = ThreadModel.POST,type = Type.BLOCK_RETURN)
    public String testMethod4(Event11 event11) {
        assertNotNull(event11);
        return "return_" + event11.msg;
    }
}
