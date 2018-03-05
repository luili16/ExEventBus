package com.llx278.exeventbus;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.intent.IntentStubberRegistry;

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



    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();


    @Before
    public void before() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();

        Intent intent10 = new Intent(context, TestService10.class);
        IBinder binder10 = mServiceRule.bindService(intent10);
        mTest10 = IRouterInteractInterface.Stub.asInterface(binder10);


    }

    @After
    public void after() {
    }

    /**
     * 测试发布一个不需要返回值的消息到其他进程
     */
    @Test
    public void publishToOtherProcess() {

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
