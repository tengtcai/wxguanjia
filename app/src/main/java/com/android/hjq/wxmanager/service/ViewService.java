package com.android.hjq.wxmanager.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.hjq.wxmanager.R;

public class ViewService extends Service {
    private View view;
    private WindowManager windowManager;
    private ImageView chatHead;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("HJQresult",intent.getStringExtra("name"));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        showView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(view != null) {
            windowManager.removeView(view);
        }
//        if (chatHead != null) {
//            windowManager.removeView(chatHead);
//        }
    }

    private void showView(){
        view = LayoutInflater.from(this).inflate(R.layout.layout_xuanfu,null);
        chatHead = new ImageView(this);
        chatHead.setImageResource(R.mipmap.ic_launcher);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = 200;
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.alpha = 0.5f;
        try {
            windowManager.addView(view, layoutParams);
        }catch (Exception e){
            e.printStackTrace();
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.type = WindowManager.LayoutParams.TYPE_TOAST;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        params.x = dm.widthPixels-250;
        params.y = dm.heightPixels/4*3;
//        windowManager.addView(chatHead, params);

        setChatHeadTouchListener(params);


    }

    private void setChatHeadTouchListener(final WindowManager.LayoutParams params) {
        chatHead.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX
                                + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY
                                + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(chatHead, params);
                        return true;
                }
                return false;
            }
        });
    }

}
