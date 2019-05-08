package com.google.zxing.activity;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;

import com.giants.imagepicker.GlideImageLoader;
import com.giants.imagepicker.bean.ImageItem;
import com.giants.imagepicker.util.Utils;
import com.giants.imagepicker.view.SuperCheckBox;
import com.xitech.xiapp.R;


import java.util.ArrayList;




/**
 * Created by Administrator on 2018/1/11.
 */

public class ImageRecyclerAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Activity mActivity;
    private ArrayList<ImageItem> images;       //当前需要显示的所有的图片数据
    private int mImageSize;               //每个条目的大小
    private LayoutInflater mInflater;
    private OnImageItemClickListener listener;   //图片被点击的监听
    public ImageRecyclerAdapter(Activity mActivity,ArrayList<ImageItem> imageItems){
        this.mActivity=mActivity;
        if(imageItems==null||imageItems.size()==0){
            this.images=new ArrayList<>();
        }else{
            this.images=imageItems;
        }
        this.mImageSize= Utils.getImageItemWidth(mActivity);
        this.mInflater=LayoutInflater.from(mActivity);

    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ImageViewHolder(mInflater.inflate(R.layout.adapter_image_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ImageViewHolder){
            ((ImageViewHolder) holder).bindImageVIew(position);
        }
    }
    public void refreshData(ArrayList<ImageItem> imageItems){
      if(imageItems==null||imageItems.size()==0){
          this.images=new ArrayList<>();
      }else{
          this.images=imageItems;
      }
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return this.images.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
    public ImageItem getImageItem(int position){
       if(this.images.size()>0){
         return this.images.get(position);
       }
       return null;
    }
    interface OnImageItemClickListener{
        void onImageItemClick(View view, ImageItem imageItem, int position);
    }
    public void setOnImageItemClickListener(OnImageItemClickListener listener){
        this.listener=listener;
    }
    private class ImageViewHolder extends RecyclerView.ViewHolder{
        View rootView;
        ImageView ivThumb;
        SuperCheckBox superCheckBox;
        public ImageViewHolder(View itemView) {
            super(itemView);
            this.rootView=itemView;
            this.ivThumb=this.itemView.findViewById(R.id.iv_thumb);
            this.superCheckBox=this.itemView.findViewById(R.id.cb_check);
            this.superCheckBox.setVisibility(View.GONE);
            itemView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,mImageSize));
        }
        public void bindImageVIew(final int position){
            final ImageItem imageItem=getImageItem(position);
            ivThumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener!=null){
                        listener.onImageItemClick(rootView,imageItem,position);
                    }
                }
            });
           new  GlideImageLoader().displayImage(mActivity,imageItem.path,ivThumb,mImageSize,mImageSize);
        }
    }
}
