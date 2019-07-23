package com.android.hjq.wxmanager.activity;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hjq.wxmanager.ItemClickListener;
import com.android.hjq.wxmanager.MyApplication;
import com.android.hjq.wxmanager.R;
import com.android.hjq.wxmanager.adapter.QunListAdapter;
import com.android.hjq.wxmanager.model.QunInfo;
import com.android.hjq.wxmanager.service.ViewService;
import com.android.hjq.wxmanager.utils.AccessibilityHelper;
import com.android.hjq.wxmanager.utils.SpUtils;
import com.android.hjq.wxmanager.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class QunListActivity extends AppCompatActivity {
    @BindView(R.id.btn_check)
    Button btnChe;
    @BindView(R.id.btn_select)
    TextView btnSelect;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.rv_qun)
    RecyclerView rvQun;
    @BindView(R.id.et_time)
    EditText etTime;
    @BindView(R.id.et_start)
    EditText etStart;
    @BindView(R.id.et_end)
    EditText etEnd;
    @BindView(R.id.btn_send_time)
    Button btnSendTime;

    private Intent serviceIntent;
    private QunListAdapter adapter;
    private List<QunInfo> list = new ArrayList<>();
    private boolean quanxuan = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qun);
        ButterKnife.bind(this);

        serviceIntent = new Intent(this, ViewService.class);
        serviceIntent.putExtra("name","启动悬浮布局");
        if(SpUtils.getIsTimeTask()){
            btnSendTime.setText("取消定时发送");
        }else {
            btnSendTime.setText("开始定时发送");
        }
        rvQun.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new QunListAdapter(this, list);
        rvQun.setAdapter(adapter);

        btnSendTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String time = etTime.getText().toString().trim();
                if(TextUtils.isEmpty(time)){
                    Toast.makeText(QunListActivity.this,"请输入间隔时间",Toast.LENGTH_SHORT).show();
                    return;
                }
                int start = 0;
                int end = 0;
                if(!TextUtils.isEmpty(etStart.getText().toString().trim())){
                    start = Integer.parseInt(etStart.getText().toString().trim());
                }
                if(!TextUtils.isEmpty(etEnd.getText().toString().trim())){
                    end = Integer.parseInt(etEnd.getText().toString().trim());
                }
                int t = Integer.parseInt(time);
                if(t>60 ||t<1){
                    Toast.makeText(QunListActivity.this,"间隔时间不能大于60分钟或者小于一分钟",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!SpUtils.getIsTimeTask()) {//没有开启定时
                    MainActivity.service = Executors.newSingleThreadScheduledExecutor();
                    MainActivity.service.scheduleAtFixedRate(new ScanScheduledExecutor(start,end), 0, t, TimeUnit.MINUTES);
                    SpUtils.saveIsTimeTask(true);
                    btnSendTime.setText("取消定时发送");
                }else {
                    MainActivity.service.shutdown();
                    MainActivity.service = null;
                    SpUtils.saveIsTimeTask(false);
                    btnSendTime.setText("开始定时发送");
                }
            }
        });
    }


    //静态弱引用方式，防止内存泄露
    static class ScanScheduledExecutor implements Runnable {
        int star = 0;
        int end = 0;
        ScanScheduledExecutor(int star,int end) {
            this.star = star;
            this.end = end;
        }
        @Override
        public void run() {
            try {
                Calendar ca=Calendar.getInstance();
                int hour = ca.get(Calendar.HOUR_OF_DAY) ;//获取当时时间数
                if(star>0 && end>0) {
                    if (hour < star || hour > end) return;
                }
                ///处理逻辑
                Log.e("HJQresult","开始发送了");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick({R.id.btn_check, R.id.btn_select, R.id.btn_send})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_check://检测群
                SpUtils.saveType(4);
                getWechatApi();
                break;
            case R.id.btn_select://全选
                if(quanxuan) {
                    for (int i = 0; i < list.size(); i++) {
                        list.get(i).setSelect(true);
                    }
                    btnSelect.setText("取消全选");
                    quanxuan = false;
                }else {
                    for (int i = 0; i < list.size(); i++) {
                        list.get(i).setSelect(false);
                    }
                    btnSelect.setText("全选");
                    quanxuan = true;
                }
                adapter.refresh(list);
                break;
            case R.id.btn_send://发送
                List<QunInfo> qunInfos = new ArrayList<>();
                for(int i = 0;i<list.size();i++){
                    if(list.get(i).isSelect()){
                        qunInfos.add(list.get(i));
                    }
                }
                SpUtils.saveSelectQunList(qunInfos);
                SpUtils.saveType(3);
                getWechatApi();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        list = SpUtils.getQunList();
        int select = 0;
        for (int i = 0; i < list.size(); i++) {
            if(!list.get(i).isSelect()){
                select = 1;
            }
        }
        if(select == 0){
            btnSelect.setText("取消全选");
            quanxuan = false;
        }else {
            btnSelect.setText("全选");
            quanxuan = true;
        }
        adapter.refresh(list);
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
            Toast.makeText(QunListActivity.this,"检查到您手机没有安装微信，请安装后使用该功能",Toast.LENGTH_SHORT).show();
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (! Settings.canDrawOverlays(QunListActivity.this)) {
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
        stopService(serviceIntent);
        SpUtils.setDestory(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(QunListActivity.this,"请打开权限",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
