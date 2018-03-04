// IRouterIntereactInterface.aidl
package com.llx278.exeventbus;

// Declare any non-default types here with import statements
// Router与其他进程进行交互测试的api接口
import com.llx278.exeventbus.Event;
interface IRouterInteractInterface {
    // 获得add到当前进程的newEventList
    Event[] getAddRegisterEventList(String address);
}
