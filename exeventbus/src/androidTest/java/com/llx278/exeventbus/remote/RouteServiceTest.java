package com.llx278.exeventbus.remote;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by llx on 2018/3/27.
 */

@RunWith(AndroidJUnit4.class)
public class RouteServiceTest {

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    private IRouter mRoute;

    @Before
    public void before() {

    }

    @After
    public void after() {

    }

    @Test
    public void testService() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        Intent routeIntent = new Intent(context,RouteService.class);
        IBinder routeBinder = mServiceRule.bindService(routeIntent);
        mRoute = IRouter.Stub.asInterface(routeBinder);
        mRoute.addReceiveListener(Address.createOwnAddress().toString(),new ReceiverImpl());

        Intent service15 = new Intent(context,TestService15.class);
        mServiceRule.bindService(service15);
    }
}
