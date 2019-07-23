package com.android.hjq.wxmanager.activity;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.hjq.wxmanager.R;
import com.android.hjq.wxmanager.interfaces.MyInterface;
import com.android.hjq.wxmanager.receiver.MyReceiver;
import com.android.hjq.wxmanager.service.ViewService;
import com.android.hjq.wxmanager.utils.AccessibilityHelper;
import com.android.hjq.wxmanager.utils.SpUtils;
import com.android.hjq.wxmanager.utils.StatusBarUtil;
import com.android.hjq.wxmanager.utils.Utils;

import java.util.concurrent.ScheduledExecutorService;

public class MainActivity extends AppCompatActivity implements MyInterface {
    private EditText editText,etBiaoqian,etImg;
    private Button btn,btn1,btn2,btnSelect;
    private Intent serviceIntent;
    public static String ACTION_TAG = "com.android.hjq.wxmanager.action.MainActivity";
    private  MyReceiver receiver = new MyReceiver();
    public static ScheduledExecutorService service;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 避免从桌面启动程序后，会重新实例化入口类的activity
        if (!this.isTaskRoot()) { // 当前类不是该Task的根部，那么之前启动
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) { // 当前类是从桌面启动的
                    finish(); // finish掉该类，直接打开该Task中现存的Activity
                    return;
                }
            }
        }
        setContentView(R.layout.activity_main);
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this,true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this,0x55000000);
        }
        StatusBarUtil.setStatusBarColor(this,0x55000000);
        editText = findViewById(R.id.et_content);
        etBiaoqian = findViewById(R.id.et_biaoqian);
        etImg = findViewById(R.id.et_img);
        btn = findViewById(R.id.btnSend);
        btn1 = findViewById(R.id.btnSend1);
        btn2 = findViewById(R.id.btnSend2);
        btnSelect = findViewById(R.id.btnSelect);

        SpUtils.setDestory(true);//禁止监听

        serviceIntent = new Intent(this, ViewService.class);
        serviceIntent.putExtra("name","启动悬浮布局");

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_TAG);
        receiver.setListener(this);
        registerReceiver(receiver,filter);

        if (!AccessibilityHelper.isAccessibilitySettingsOn(this)) {
            AccessibilityHelper.openAccessibilityServiceSettings(this);
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = editText.getText().toString().trim();
                if(TextUtils.isEmpty(content)){
                    Toast.makeText(MainActivity.this,"请输入要发送的内容",Toast.LENGTH_SHORT).show();
                    return;
                }
                String xiabiao = etImg.getText().toString().trim();
                SpUtils.saveImage(xiabiao);
                Utils.copy(content);
                SpUtils.setFirstStart(true);
                SpUtils.saveType(1);
                getWechatApi();
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = editText.getText().toString().trim();
                if(TextUtils.isEmpty(content)){
                    Toast.makeText(MainActivity.this,"请输入要发送的内容",Toast.LENGTH_SHORT).show();
                    return;
                }
                String biaoqian = etBiaoqian.getText().toString().trim();
                if(TextUtils.isEmpty(biaoqian)){
                    Toast.makeText(MainActivity.this, "请输入标签", Toast.LENGTH_SHORT).show();
                    return;
                }
                String xiabiao = etImg.getText().toString().trim();
                SpUtils.saveImage(xiabiao);
                Utils.copy(content);
                SpUtils.saveBiaoQian(biaoqian);
                SpUtils.saveType(2);
                getWechatApi();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//群发
                String content = editText.getText().toString().trim();
                if(TextUtils.isEmpty(content)){
                    Toast.makeText(MainActivity.this,"请输入要发送的内容",Toast.LENGTH_SHORT).show();
                    return;
                }
                String xiabiao = etImg.getText().toString().trim();
                SpUtils.saveImage(xiabiao);
                Utils.copy(content);
                Intent intent = new Intent(MainActivity.this,QunListActivity.class);
                startActivity(intent);
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SayHelloActivity.class);
                startActivityForResult(intent,11);
            }
        });
    }

    /**
     * 跳转到微信
     */
    private void getWechatApi(){
        try {
            SpUtils.setDestory(false);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName cmp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.LauncherUI");
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cmp);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // TODO: handle exception
            Toast.makeText(MainActivity.this,"检查到您手机没有安装微信，请安装后使用该功能",Toast.LENGTH_SHORT).show();
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (! Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 10);
            }else {
                startService(serviceIntent);
            }
        }else {
            startService(serviceIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(serviceIntent != null){
                stopService(serviceIntent);
            }
            SpUtils.setDestory(true);
            if(receiver != null){
                unregisterReceiver(receiver);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if(MainActivity.service != null){
            MainActivity.service.shutdown();
            MainActivity.service = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(this)) {
                   Toast.makeText(MainActivity.this,"请打开权限",Toast.LENGTH_SHORT).show();
                }
            }
        }
        if(requestCode == 12){
            if(!AccessibilityHelper.isAccessibilitySettingsOn(this)){
                AccessibilityHelper.openAccessibilityServiceSettings(this);
            }
        }
        if(requestCode == 11){
            if(data != null) {
                editText.setText(data.getStringExtra("data"));
            }
        }
    }

    @Override
    public void onListerner(int type) {
        stopService(serviceIntent);
    }
}
