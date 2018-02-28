// IMyTestInterface.aidl
package com.llx278.exeventbus;

// Declare any non-default types here with import statements

interface IMyTestInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    String getProcessName();

    String getBroadcastStr();

    String getReceiveStr();

    void clear();
}
