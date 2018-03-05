package com.llx278.exeventbus.entry;


import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.ThreadModel;
import com.llx278.exeventbus.Type;
import com.llx278.exeventbus.event.Event4;
import com.llx278.exeventbus.event.Event5;
import com.llx278.exeventbus.event.Event6;
import com.llx278.exeventbus.event.Event7;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * 测试执行默认tag
 * Created by llx on 2018/2/6.
 */

public class SubscribeEntry6 {

    private final CountDownLatch mDownSignal;

    public SubscribeEntry6(CountDownLatch downSignal) {
        mDownSignal = downSignal;
    }
    public SubscribeEntry6() {
        this(null);
    }



    @Subscriber(tag = "event7",model = ThreadModel.POOL,type = Type.BLOCK_RETURN)
    public String testMethod1(Event7 event7) {
        assertNotNull(event7);
        return "return_" + event7.msg;
    }

    @Subscriber(tag = "event6",model = ThreadModel.MAIN,type = Type.BLOCK_RETURN)
    public String testMethod2(Event6 event6) {
        assertNotNull(event6);
        return "return_" + event6.msg;
    }

    @Subscriber(tag = "event5",model = ThreadModel.HANDLER,type = Type.BLOCK_RETURN)
    public String testMethod3(Event5 event5) {
        assertNotNull(event5);
        return "return_"+event5.msg;
    }

    @Subscriber(tag = "event4",model = ThreadModel.POST,type = Type.BLOCK_RETURN)
    public String testMethod4(Event4 event4) {
        assertNotNull(event4);
        return "return_" + event4.msg;
    }
}
