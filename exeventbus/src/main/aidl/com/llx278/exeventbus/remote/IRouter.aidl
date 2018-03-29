// IRoute.aidl
package com.llx278.exeventbus.remote;

// Declare any non-default types here with import statements
import android.os.Bundle;
import com.llx278.exeventbus.remote.IReceiver;
interface IRouter {

    void connect(String where);

    void disConnect(String where);

    void send(String where,String address,in Bundle message);

    void addReceiveListener(String where,in IReceiver listener);

    void removeReceiveListener(String where);

    List<String> getConnectedClient(String where);

}
