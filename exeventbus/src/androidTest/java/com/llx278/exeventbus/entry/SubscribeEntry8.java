package com.llx278.exeventbus.entry;


import android.util.Log;

import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.ThreadModel;
import com.llx278.exeventbus.Type;
import com.llx278.exeventbus.event.Event10;
import com.llx278.exeventbus.event.Event11;
import com.llx278.exeventbus.event.Event8;
import com.llx278.exeventbus.event.Event9;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * 测试执行默认tag
 * Created by llx on 2018/2/6.
 */

public class SubscribeEntry8 {

    private final CountDownLatch mDownSignal;

    public SubscribeEntry8(CountDownLatch downSignal) {
        mDownSignal = downSignal;
    }

    public SubscribeEntry8() {
        this(null);
    }

    @Subscriber(tag = "event8", model = ThreadModel.POOL, type = Type.BLOCK_RETURN,remote = true)
    public void testMethod1(Event8 event8) {
        assertNotNull(event8);
        Log.d("main","SubscribeEntry8 : " + event8.getMsg());
        assertEquals("event8",event8.getMsg());
    }

    @Subscriber(tag = "event9", model = ThreadModel.MAIN, type = Type.BLOCK_RETURN, remote = true)
    public String testMethod2(Event9 event9) {
        assertNotNull(event9);
        return "return_" + event9.getMsg();
    }

    @Subscriber(tag = "event10", model = ThreadModel.HANDLER, type = Type.BLOCK_RETURN, remote = true)
    public String testMethod3(Event10 event10) {
        assertNotNull(event10);
        return "return_" + event10.getMsg();
    }

    @Subscriber(tag = "event11", model = ThreadModel.POST, type = Type.BLOCK_RETURN, remote = true)
    public String testMethod4(Event11 event11) {
        assertNotNull(event11);
        return "return_" + event11.getMsg();
    }
}
