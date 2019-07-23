package com.android.hjq.wxmanager.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.hjq.wxmanager.R;
import com.android.hjq.wxmanager.utils.SpUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddSayActivity extends AppCompatActivity {
    @BindView(R.id.et_content)
    EditText etContent;
    @BindView(R.id.btn_save)
    Button btnSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_say);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_save)
    public void onViewClicked() {
        String str = etContent.getText().toString();
        if(TextUtils.isEmpty(str)){
            Toast.makeText(this,"请输入内容",Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> strings = SpUtils.getSay();
        strings.add(str);
        Log.e("HJQresult",str);
        SpUtils.saveSay(strings);
        finish();
    }
}
