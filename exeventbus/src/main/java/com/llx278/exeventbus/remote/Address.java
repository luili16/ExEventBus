package com.llx278.exeventbus.remote;

import android.os.Process;
import android.text.TextUtils;

import com.llx278.exeventbus.ELogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 封装了地址管理相关的方法
 * 目前考虑用一个进程的唯一pid来作为这个进程的地址
 * Created by llx on 2018/2/27.
 */

public class Address {

    private static final String PID = "pid";

    private int mPid;

    private Address(int pid) {
        mPid = pid;
    }

    public int getPid() {
        return mPid;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PID,mPid);
        } catch (JSONException e) {
            ELogger.e("",e);
        }
        return jsonObject.toString();
    }

    public static Address createOwnAddress() {
        return new Address(Process.myPid());
    }

    public static Address createAddress(int pid) {
        return new Address(pid);
    }

    public static Address toAddress(String address) {

        if (TextUtils.isEmpty(address)) {
            return null;
        }

        Address addressObj = null;
        try {
            JSONObject jsonObject = new JSONObject(address);
            int pid = jsonObject.getInt(PID);
            addressObj = new Address(pid);
        } catch (JSONException e) {
            ELogger.e("",e);
        }
        return addressObj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        return mPid == address.mPid;
    }

    @Override
    public int hashCode() {
        return mPid;
    }
}
