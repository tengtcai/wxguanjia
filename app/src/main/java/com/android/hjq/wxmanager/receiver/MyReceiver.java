package com.android.hjq.wxmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.hjq.wxmanager.interfaces.MyInterface;

public class MyReceiver extends BroadcastReceiver {
    private MyInterface myInterface;

    @Override
    public void onReceive(Context context, Intent intent) {
        int type = intent.getIntExtra("type",0);
        switch (type){
            case 1:
                if(myInterface != null){
                    myInterface.onListerner(type);
                }
                break;
        }
    }

    public void setListener(MyInterface myInterface){
        this.myInterface = myInterface;
    }
}
