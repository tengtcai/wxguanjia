package com.android.hjq.wxmanager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.hjq.wxmanager.ItemClickListener;
import com.android.hjq.wxmanager.R;
import com.android.hjq.wxmanager.adapter.SayHelloAdapter;
import com.android.hjq.wxmanager.utils.SpUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class SayHelloActivity extends AppCompatActivity implements ItemClickListener {
    private Button btn;
    private RecyclerView rvSay;

    private SayHelloAdapter adapter;
    private List<String> strings = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_say_hello);
        btn = findViewById(R.id.btnAdd);
        rvSay = findViewById(R.id.rv_say);

        rvSay.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new SayHelloAdapter(this,strings,this);
        rvSay.setAdapter(adapter);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SayHelloActivity.this,AddSayActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        strings = SpUtils.getSay();
        Log.e("HJQresult",new Gson().toJson(strings));
        adapter.refresh(strings);
    }

    @Override
    public void onItemClick(Object data, int position, View view) {
        Intent intent = new Intent();
        intent.putExtra("data",data.toString());
        setResult(RESULT_OK,intent);
        finish();
    }
}
