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

public class SubscribeEntry2 {

    private final CountDownLatch mDownSignal;

    public SubscribeEntry2(CountDownLatch downSignal) {
        mDownSignal = downSignal;
    }

    public void register() {
        EventBus.getDefault().register(this);
    }

    public void unRegister() {
        EventBus.getDefault().unRegister(this);
    }

    @Subscriber(mode = ThreadModel.HANDLER,tag = "my_event3")
    public void event3Method(Event3 event3) {
        assertNotNull(event3);
        assertEquals(event3.msg,"event3");
        Log.d("main","SubscribeEntry2 event3Method has called!");
        mDownSignal.countDown();
    }

    @Subscriber(mode = ThreadModel.HANDLER)
    public void event4Method(Event4 event4) {
        assertNotNull(event4);
        assertEquals(event4.msg,"event4");
        Log.d("main","event2Method finish");
        mDownSignal.countDown();
    }

    @Subscriber(mode = ThreadModel.MAIN)
    public void event5Method(Event5 event5) {
        assertNotNull(event5);
        assertEquals(event5.msg,"event5");
        Log.d("main","event3Method finish");
        mDownSignal.countDown();
    }
}
