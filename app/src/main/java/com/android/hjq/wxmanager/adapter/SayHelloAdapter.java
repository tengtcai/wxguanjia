package com.android.hjq.wxmanager.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.hjq.wxmanager.ItemClickListener;
import com.android.hjq.wxmanager.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SayHelloAdapter extends RecyclerView.Adapter<SayHelloAdapter.ViewHolder> {
    private ItemClickListener clickListener;
    private Context context;
    private List<String> mList;

    public SayHelloAdapter(Context context, List<String> list, ItemClickListener clickListener) {
        this.mList = list;
        this.clickListener = clickListener;
        this.context = context;
    }

    public void refresh(List<String> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_say_hello, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder mHolder, final int position) {
        mHolder.tvText.setText(mList.get(position));
        mHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onItemClick(mList.get(position),position,mHolder.itemView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_text)
        TextView tvText;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}