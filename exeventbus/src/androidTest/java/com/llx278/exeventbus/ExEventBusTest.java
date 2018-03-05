package com.llx278.exeventbus;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by llx on 2018/3/5.
 */

@RunWith(AndroidJUnit4.class)
public class ExEventBusTest {

    @Before
    public void before() {
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
