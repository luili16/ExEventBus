package com.llx278.exeventbus;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.Log;

import com.llx278.exeventbus.entry.SubscribeEntry8;
import com.llx278.exeventbus.event.Event8;
import com.llx278.exeventbus.event.Event9;
import com.llx278.exeventbus.remote.Address;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 对Router类进行测试
 * Created by llx on 2018/3/4.
 */

@RunWith(AndroidJUnit4.class)
public class RouterTest {

    private Router mRouter;
    EventBus mEventBus;

    IRouterInteractInterface mTest9;

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Before
    public void before() throws Exception {
        mEventBus = new EventBus();
        Context context = InstrumentationRegistry.getTargetContext();
        mRouter = new Router(context, mEventBus);

        Intent serviceIntent = new Intent(context, TestService9.class);
        IBinder binder = mServiceRule.bindService(serviceIntent);
        mTest9 = IRouterInteractInterface.Stub.asInterface(binder);
    }

    @After
    public void after() {
    }

    @Test
    public void routerTest() throws Exception {
        Event9 event9 = new Event9("event9");
        String tag = "event9_SubscribeEntry9";
        String returnClassName = String.class.getName();
        Object returnValue = mRouter.route(event9, tag, returnClassName, 1000 * 10);
        assertNotNull(returnValue);
        assertEquals(returnValue.getClass(),String.class);
        assertEquals("return_event9",returnValue.toString());
    }
}
