// IRouterIntereactInterface.aidl
package com.llx278.exeventbus;

// Declare any non-default types here with import statements
// Router与其他进程进行交互测试的api接口
import com.llx278.exeventbus.Event;
interface IRouterInteractInterface {
    // 获得add到当前进程的newEventList
    Event[] getAddRegisterEventList(String address);
    // 获得当前进程的地址
    String getAddress();
    // 退出当前的进程
    void killSelf();

    // 获得远程注册对象执行的结果,用来判断是否执行了远程的方法
    String testMethod1Result();
    String testMethod2Result();
    String testMethod3Result();
    String testMethod4Result();

    void start();

    void stop();

    void sendTo(String addrss);
}
