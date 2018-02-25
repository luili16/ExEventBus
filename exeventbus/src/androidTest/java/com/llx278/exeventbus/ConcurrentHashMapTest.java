package com.llx278.exeventbus;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by llx on 2018/2/25.
 */
@RunWith(JUnit4.class)
public class ConcurrentHashMapTest {

    private Map<String,String> map = new ConcurrentHashMap<>();
    private ExecutorService executor = Executors.newCachedThreadPool();
    private CountDownLatch addLatch = new CountDownLatch(100);
    private CountDownLatch removeLatch = new CountDownLatch(100);

    @Test
    public void test1() {
        for (int i = 0; i < 100;i++) {
            map.put(String.valueOf(i),"test" + i);
        }
    }

}
