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

        void mockSendMessage(String address,in Bundle message);

        boolean mockSendMessage1(String address,in Bundle message,long timeout);

        void mockSendBroadcast(in Bundle message);

        void clear();
}
