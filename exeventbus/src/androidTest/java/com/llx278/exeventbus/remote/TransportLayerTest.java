package com.llx278.exeventbus.remote;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.llx278.exeventbus.IMyTestInterface;
import com.llx278.exeventbus.remote.test.*;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

/**
 * 测试TransportLayer
 * Created by llx on 2018/3/1.
 */

@RunWith(AndroidJUnit4.class)
public class TransportLayerTest implements ITransportLayer.ReceiverListener {

    private TransportLayer mTransportLayer;
    private IMyTestInterface mTest5;
    private IMyTestInterface mTest6;
    private IMyTestInterface mTest7;
    private IMyTestInterface mTest8;

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Before
    public void before() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        mTransportLayer = new TransportLayer(new MockPhysicalLayer(context));
        mTransportLayer.setOnReceiveListener(this);

        Intent intent5 = new Intent(context, TestService5.class);
        IBinder binder5 = mServiceRule.bindService(intent5);
        mTest5 = IMyTestInterface.Stub.asInterface(binder5);

        Intent intent6 = new Intent(context, TestService6.class);
        IBinder binder6 = mServiceRule.bindService(intent6);
        mTest6 = IMyTestInterface.Stub.asInterface(binder6);

        Intent intent7 = new Intent(context, TestService7.class);
        IBinder binder7 = mServiceRule.bindService(intent7);
        mTest7 = IMyTestInterface.Stub.asInterface(binder7);

        Intent intent8 = new Intent(context, TestService8.class);
        IBinder binder8 = mServiceRule.bindService(intent8);
        mTest8 = IMyTestInterface.Stub.asInterface(binder8);

    }

    @After
    public void after() {
        mTransportLayer.destroy();
    }

    @Test
    public void sendMessage() throws Exception {

        clearAll();

        Bundle message = new Bundle();
        String receiveStr = "receive";
        message.putString(com.llx278.exeventbus.remote.test.Constant.KEY_RECEIVE, receiveStr);
        Address address5 = createTest5Address();
        mTransportLayer.send(address5.toString(),message,1000 * 2);

        Address address6 = createTest6Address();
        mTransportLayer.send(address6.toString(),message,1000 * 2);

        Address address7 = createTest7Address();
        mTransportLayer.send(address7.toString(),message,1000 * 2);

        Address address8 = createTest8Address();
        mTransportLayer.send(address8.toString(),message,1000 * 2);

        String expectedStr1 = receiveStr + ":" + address5.toString();
        String expectedStr2 = receiveStr + ":" + address6.toString();
        String expectedStr3 = receiveStr + ":" + address7.toString();
        String expectedStr4 = receiveStr + ":" + address8.toString();
        long timeout = 2000;
        long endTime = SystemClock.uptimeMillis() + timeout;
        boolean isReceive = false;
        while (SystemClock.uptimeMillis() < endTime) {
            String receiveStr1 = mTest5.getReceiveStr();
            String receiveStr2 = mTest6.getReceiveStr();
            String receiveStr3 = mTest7.getReceiveStr();
            String receiveStr4 = mTest8.getReceiveStr();
            if (expectedStr1.equals(receiveStr1) &&
                    expectedStr2.equals(receiveStr2) &&
                    expectedStr3.equals(receiveStr3) &&
                    expectedStr4.equals(receiveStr4)) {
                isReceive = true;
                break;
            }
            Thread.sleep(100);
        }
        Assert.assertTrue(isReceive);
    }

    private Address createTest8Address() throws Exception {
        String processName1 = mTest8.getProcessName();
        // 模拟向TestService1所在的进程发送消息
        int pid1 = Integer.parseInt(processName1.split("-")[1]);
        return Address.createAddress(pid1);
    }

    private Address createTest7Address() throws Exception {
        String processName1 = mTest7.getProcessName();
        // 模拟向TestService1所在的进程发送消息
        int pid1 = Integer.parseInt(processName1.split("-")[1]);
        return Address.createAddress(pid1);

    }

    private Address createTest6Address() throws Exception {
        String processName1 = mTest6.getProcessName();
        // 模拟向TestService1所在的进程发送消息
        int pid1 = Integer.parseInt(processName1.split("-")[1]);
        return Address.createAddress(pid1);
    }

    private Address createTest5Address() throws Exception {
        String processName1 = mTest5.getProcessName();
        // 模拟向TestService1所在的进程发送消息
        int pid1 = Integer.parseInt(processName1.split("-")[1]);
        return Address.createAddress(pid1);
    }

    private void clearAll() throws Exception {
        mTest5.clear();
    }

    @Override
    public void onMessageReceive(String where, Bundle message) {

    }
}
