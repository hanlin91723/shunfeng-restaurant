package com.google.zxing.activity;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.giants.imagepicker.GlideImageLoader;
import com.giants.imagepicker.bean.ImageFolder;
import com.giants.imagepicker.util.Utils;
import com.xitech.xiapp.R;


import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2018/1/11.
 */

public class ImageFolderAdapter extends BaseAdapter{
    private Activity mActivity;
    private LayoutInflater mInflater;
    private int mImageSize;
    private List<ImageFolder> imageFolders;
    private int lastSelected = 0;
    public ImageFolderAdapter(Activity mActivity, ArrayList<ImageFolder> imageFolders){
        this.mActivity=mActivity;
        if(imageFolders==null||imageFolders.size()==0){
            this.imageFolders=new ArrayList<>();
        }else{
            this.imageFolders=imageFolders;
        }
        mImageSize= Utils.getImageItemWidth(mActivity);
        mInflater=LayoutInflater.from(mActivity);
    }
    public void refreshData(List<ImageFolder> folders) {
        if (folders != null && folders.size() > 0) imageFolders = folders;
        else imageFolders.clear();
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return this.imageFolders.size();
    }

    @Override
    public ImageFolder getItem(int position) {
        if (imageFolders.size()>0){
            return imageFolders.get(position);
        }
        return null;
    }
    public void setSelectIndex(int i) {
        if (lastSelected == i) {
            return;
        }
        lastSelected = i;
        notifyDataSetChanged();
    }
    public int getSelectIndex() {
        return lastSelected;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(convertView==null){
          convertView=mInflater.inflate(R.layout.adapter_folder_list_item,viewGroup,false);
            viewHolder=new ViewHolder(convertView);
        }else{
           viewHolder=(ViewHolder) convertView.getTag();
        }
        ImageFolder imageFolder=getItem(position);
        viewHolder.folderName.setText(imageFolder.name);
        viewHolder.imageCount.setText(mActivity.getString(R.string.folder_image_count, imageFolder.images.size()));
        new GlideImageLoader().displayImage(mActivity,imageFolder.cover.path,viewHolder.cover,mImageSize,mImageSize);
        if (lastSelected==position){
         viewHolder.folderCheck.setVisibility(View.VISIBLE);
        }else{
            viewHolder.folderCheck.setVisibility(View.GONE);
        }
        return convertView;
    }
    private class ViewHolder {
        ImageView cover;
        TextView folderName;
        TextView imageCount;
        ImageView folderCheck;
        public ViewHolder(View view) {
            cover = (ImageView) view.findViewById(R.id.iv_cover);
            folderName = (TextView) view.findViewById(R.id.tv_folder_name);
            imageCount = (TextView) view.findViewById(R.id.tv_image_count);
            folderCheck = (ImageView) view.findViewById(R.id.iv_folder_check);
            view.setTag(this);
        }
    }
}
