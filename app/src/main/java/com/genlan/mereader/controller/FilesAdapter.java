package com.genlan.mereader.controller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.genlan.mereader.R;
import com.genlan.mereader.model.FileInfo;

import java.util.ArrayList;

/**
 * Description
 * Author Genlan
 * Date 2017/7/18
 */

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesVH> {

    private ArrayList<FileInfo> mList;
    private OnItemClickListener mListener;
    private LayoutInflater mInflate;
    private Context mContext;

    public FilesAdapter(ArrayList<FileInfo> list, Context context, OnItemClickListener listener){
        this.mInflate = LayoutInflater.from(context);
        this.mContext = context;
        this.mList = list;
        this.mListener = listener;
    }

    @Override
    public FilesVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FilesVH(mInflate.inflate(R.layout.item_file_info,parent,false),mListener);
    }

    @Override
    public void onBindViewHolder(FilesVH holder, int position) {
        holder.tvFileParent.setText(mContext.getString(R.string.text_file_info,"文件路径",mList.get(position).getFileParent()));
        holder.tvFileName.setText(mContext.getString(R.string.text_file_info,"文件名称",mList.get(position).getFileName()));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class FilesVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private OnItemClickListener mListener;
        private TextView tvFileName;
        private TextView tvFileParent;

        FilesVH(View itemView,OnItemClickListener listener) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.mListener = listener;
            tvFileName = (TextView) itemView.findViewById(R.id.tv_item_name);
            tvFileParent = (TextView) itemView.findViewById(R.id.tv_item_parent);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null)
                mListener.onClick(getLayoutPosition(),v);
        }
    }
}

