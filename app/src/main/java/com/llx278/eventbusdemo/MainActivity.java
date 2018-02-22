package com.llx278.eventbusdemo;

import android.content.Intent;
import android.os.FileUriExposedException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.llx278.exeventbus.EventBus;
import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.ThreadModel;

public class MainActivity extends AppCompatActivity {

    public static final String MAIN_TAG = "updateTextView";

    TextView textView;
    TextView event1Text;
    TextView event2Text;
    TextView event3Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bt = findViewById(R.id.bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post("hello from service",MainActivity.MAIN_TAG);
                Event1 event1 = new Event1();
                event1.msg = "hello from event 1";
                EventBus.getDefault().post(event1);
                Event2 event2 = new Event2();
                event2.msg = "hello from event 2";
                EventBus.getDefault().post(event2);
                Event3 event3 = new Event3();
                event3.msg = "hello from event 3";
                EventBus.getDefault().post(event3);
            }
        });
        textView = findViewById(R.id.text);
        event1Text = findViewById(R.id.event1);
        event2Text = findViewById(R.id.event2);
        event3Text = findViewById(R.id.event3);

    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unRegister(this);

    }

    @Subscriber(tag = MAIN_TAG,mode = ThreadModel.POST)
    public void updateTextView(String msg) {
        try {
            textView.setText(msg);
        } catch (Exception e) {
            Log.e("main",Log.getStackTraceString(e));
        }
    }

    @Subscriber
    public void event1(Event1 event1) {
        event1Text.setText(event1.msg);
    }

    @Subscriber
    public void event2(Event2 event1) {
        event2Text.setText(event1.msg);
    }

    @Subscriber
    public void event3(Event3 event1) {
        event3Text.setText(event1.msg);
    }
}
