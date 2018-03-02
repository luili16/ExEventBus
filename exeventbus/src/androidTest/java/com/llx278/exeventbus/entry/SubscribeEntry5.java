package com.llx278.exeventbus.entry;

import android.util.Log;

import com.llx278.exeventbus.EventBusImpl;
import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.ThreadModel;
import com.llx278.exeventbus.event.Event4;
import com.llx278.exeventbus.event.Event5;
import com.llx278.exeventbus.event.Event6;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * 测试执行默认tag
 * Created by llx on 2018/2/6.
 */

public class SubscribeEntry5 {

    private final CountDownLatch mDownSignal;

    public SubscribeEntry5(CountDownLatch downSignal) {
        mDownSignal = downSignal;
    }
    public SubscribeEntry5() {
        this(null);
    }

    public void register() {
        EventBusImpl.getDefault().register(this);
    }

    public void unRegister() {
        EventBusImpl.getDefault().unRegister(this);
    }

    @Subscriber(model = ThreadModel.MAIN,tag = "event6")
    public void event1Method(Event6 event6) {
        assertNotNull(event6);
        assertEquals(event6.msg,"event6");
        Log.d("main","event6Method finish");
        if (mDownSignal != null) {
            mDownSignal.countDown();
        }
    }

    @Subscriber(model = ThreadModel.HANDLER,tag = "event4")
    public void event1Method1(Event4 event4) {
        assertNotNull(event4);
        assertEquals(event4.msg,"event4");
        Log.d("main","event4Method finish");
        if (mDownSignal != null) {
            mDownSignal.countDown();
        }
    }

    @Subscriber(model = ThreadModel.POOL,tag = "event5")
    public String event2Method(Event5 event5) {

        assertNotNull(event5);
        assertEquals(event5.msg,"event5");
        Log.d("main","event5Method finish");
        if (mDownSignal != null) {
            mDownSignal.countDown();
        }
        return "hello world!";
    }
}
