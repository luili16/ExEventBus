package com.llx278.exeventbus.entry;

import com.llx278.exeventbus.ExEventBus;
import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.ThreadModel;
import com.llx278.exeventbus.Type;
import com.llx278.exeventbus.event.Event8;
import com.llx278.exeventbus.remote.Address;

import static junit.framework.Assert.assertNotNull;

/**
 *
 * Created by llx on 2018/3/21.
 */

public class SubscribeEntry13 {
    public String mTestMethod1Tag;

    @Subscriber(tag = "event8_sticky",model = ThreadModel.POOL,type = Type.DEFAULT,remote = true)
    public void testMethod1(Event8 event8) throws Exception {
        assertNotNull(event8);
        mTestMethod1Tag = event8.getMsg();
    }
}

