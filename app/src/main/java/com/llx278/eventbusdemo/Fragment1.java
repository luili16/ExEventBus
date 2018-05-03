package com.llx278.eventbusdemo;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.llx278.exeventbus.ExEventBus;
import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.exception.TimeoutException;
import com.llx278.exeventbus.execute.ThreadModel;
import com.llx278.exeventbus.execute.Type;

/**
 *
 * Created by liu on 18-3-31.
 */

public class Fragment1 extends Fragment {

    private TextView mTextView;
    private Button mToggle;
    private boolean mStart = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExEventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_1,container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Button mTab1 = view.findViewById(R.id.bt1);
        mTab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExEventBus.getDefault().publish("tab1","subscribeTestMethod");
            }
        });
        Button mTab2 = view.findViewById(R.id.bt2);
        mTab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExEventBus.getDefault().publish("tab2","subscribeTestMethod");
            }
        });
        Button mTab3 = view.findViewById(R.id.bt3);
        mTab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExEventBus.getDefault().publish("tab3","subscribeTestMethod");
            }
        });
        Button mTab4 = view.findViewById(R.id.bt4);
        mTab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExEventBus.getDefault().publish("tab4","subscribeTestMethod");
            }
        });

        mToggle = view.findViewById(R.id.bt5);
        mToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mStart) {
                    mStart = true;
                    mToggle.setText("stop");
                    try {
                        ExEventBus.getDefault().remotePublish(true,"toggle",1000 * 2);
                    } catch (TimeoutException e) {
                        Log.e("main","",e);
                    }
                } else {
                    mStart = false;
                    mToggle.setText("start");
                    try {
                        ExEventBus.getDefault().remotePublish(false,"toggle",1000 * 2);
                    } catch (TimeoutException e) {
                        Log.e("main","",e);
                    }
                }
            }
        });

        mTextView = view.findViewById(R.id.text_message_receive);
    }

    /**
     * 此订阅事件可以由其他进程发布
     * @param event
     */
    @Subscriber(tag = "remoteReceiveMethod",remote = true,model = ThreadModel.MAIN,type = Type.DEFAULT)
    public void remoteReceiveMethod(Event event) {
        String msg = event.getMsg();
        int pid = event.getPid();
        String text = "pid : " + pid + "\nmsg : " + msg;
        mTextView.setText(text);
    }
}
