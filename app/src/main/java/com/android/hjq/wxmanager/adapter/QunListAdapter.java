package com.android.hjq.wxmanager.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.android.hjq.wxmanager.ItemClickListener;
import com.android.hjq.wxmanager.R;
import com.android.hjq.wxmanager.model.QunInfo;
import com.android.hjq.wxmanager.utils.SpUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QunListAdapter extends RecyclerView.Adapter<QunListAdapter.ViewHolder> {
    private Context context;
    private List<QunInfo> mList;

    public QunListAdapter(Context context, List<QunInfo> list) {
        this.mList = list;
        this.context = context;
    }

    public void refresh(List<QunInfo> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_qun_list, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder mHolder, final int position) {
        final QunInfo bean = mList.get(position);
        mHolder.checkbox.setText(bean.getTitle());
        mHolder.checkbox.setChecked(bean.isSelect());
        mHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bean.setSelect(mHolder.checkbox.isChecked());
                SpUtils.saveQunList(mList);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.checkbox)
        CheckBox checkbox;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}