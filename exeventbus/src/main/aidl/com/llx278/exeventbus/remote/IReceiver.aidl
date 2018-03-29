// IReceiver.aidl
package com.llx278.exeventbus.remote;

// Declare any non-default types here with import statements
import android.os.Bundle;
interface IReceiver {
    void onMessageReceive(String where,in Bundle message);
}
