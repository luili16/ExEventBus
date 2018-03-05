package com.llx278.exeventbus;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.intent.IntentStubberRegistry;
import android.util.Log;

import com.llx278.exeventbus.event.Event8;
import com.llx278.exeventbus.remote.TestService5;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by llx on 2018/3/5.
 */

@RunWith(AndroidJUnit4.class)
public class ExEventBusTest {

    private IRouterInteractInterface mTest10;
    private IRouterInteractInterface mTest11;
    private IRouterInteractInterface mTest12;
    private IRouterInteractInterface mTest13;

    private ExEventBus mExEventBus;
    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();


    @Before
    public void before() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();

        Intent intent10 = new Intent(context, TestService10.class);
        IBinder binder10 = mServiceRule.bindService(intent10);
        mTest10 = IRouterInteractInterface.Stub.asInterface(binder10);

        Intent intent11 = new Intent(context,TestService11.class);
        IBinder binder11 = mServiceRule.bindService(intent11);
        mTest11 = IRouterInteractInterface.Stub.asInterface(binder11);

        Intent intent12 = new Intent(context,TestService12.class);
        IBinder binder12 = mServiceRule.bindService(intent12);
        mTest12 = IRouterInteractInterface.Stub.asInterface(binder12);

        Intent intent13 = new Intent(context,TestService13.class);
        IBinder binder13 = mServiceRule.bindService(intent13);
        mTest13 = IRouterInteractInterface.Stub.asInterface(binder13);

        ExEventBus.create(context);
        mExEventBus = ExEventBus.getDefault();
    }

    @After
    public void after() {
        ExEventBus.destroy();
    }

    /**
     * 测试发布一个不需要返回值的消息到其他进程
     */
    @Test
    public void publishToOtherProcess() throws Exception {
        Event8 event8 = new Event8("event8");
        String tag = "event8";
        Thread.sleep(5000);
        mExEventBus.remotePublish(event8,tag,void.class.getName(),1000 * 2);
    }

    /**
     * 测试发布一个需要返回值的消息到其他进程
     */
    @Test
    public void publishToOtherProcessAndWaitReturn() {
    }

    /**
     * 测试其他的进程发布一个消息到此进程
     */
    @Test
    public void otherProcessPublishToHere() {
    }

    /**
     * 测试进程彼此之间互相发送消息
     */
    public void publishToEachOther() {
    }
}
