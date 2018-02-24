package com.llx278.exeventbus.entry;

import android.util.Log;

import com.llx278.exeventbus.EventBus;
import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.ThreadModel;
import com.llx278.exeventbus.event.Event1;
import com.llx278.exeventbus.event.Event2;
import com.llx278.exeventbus.event.Event3;
import com.llx278.exeventbus.event.Event4;
import com.llx278.exeventbus.event.Event5;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * 测试执行默认tag
 * Created by llx on 2018/2/6.
 */

public class SubscribeEntry4 {

    private final CountDownLatch mDownSignal;

    public SubscribeEntry4(CountDownLatch downSignal) {
        mDownSignal = downSignal;
    }

    public void register() {
        EventBus.getDefault().register(this);
    }

    public void unRegister() {
        EventBus.getDefault().unRegister(this);
    }

    @Subscriber(mode = ThreadModel.MAIN,tag = "subscribe_entry4_event3")
    public void event1Method(Event3 event4) {
        assertNotNull(event4);
        assertEquals(event4.msg,"event4");
        Log.d("main","event1Method finish");
        mDownSignal.countDown();
    }

    @Subscriber(mode = ThreadModel.HANDLER,tag = "subscribe_entry4_event4")
    public void event1Method1(Event4 event4) {
        assertNotNull(event4);
        assertEquals(event4.msg,"event4");
        Log.d("main","event2Method finish");
        mDownSignal.countDown();
    }

    @Subscriber(mode = ThreadModel.MAIN,tag = "subscribe_entry4_event5")
    public void event2Method(Event5 event5) {
        assertNotNull(event5);
        assertEquals(event5.msg,"event5");
        Log.d("main","event3Method finish");
        mDownSignal.countDown();
    }
}
