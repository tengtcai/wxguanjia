package com.android.hjq.wxmanager;

import android.view.View;

public interface ItemClickListener<T extends Object> {
    void onItemClick(T data, int position, View view);
}
