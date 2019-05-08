package com.wallet.sign;

import android.content.Context;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ninecm.np.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



/**
 * =====================================
 * 作    者: 白小兵
 * 版    本：1.0.0
 * 创建日期：2018/1/12
 * 描    述：搜索钱包文件时的适配器
 * =====================================
 */
public class FileAdapter extends BaseAdapter {
    private List<FileBean> fileBeans;
    private LayoutInflater inflater;
    private LinearLayout   llDetail;
    public FileAdapter(Context context, List<FileBean> fileBeans) {
        this.inflater=LayoutInflater.from(context);
        if (fileBeans==null){
            this.fileBeans=new ArrayList<>();
        }else{
            this.fileBeans=fileBeans;
        }
    }
    @Override
    public int getCount() {
        return fileBeans.size();
    }
    @Override
    public FileBean getItem(int i) {
        return fileBeans.get(i);
    }
    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (view==null){
            view=inflater.inflate(R.layout.item_serach, viewGroup, false);
        }
        TextView tvFileName = (TextView) ViewHolder.get(view,R.id.tvFileName);
        final ImageView imageFileSpread = (ImageView) ViewHolder.get(view,R.id.image_file_spread);
        TextView tvFileSize = (TextView) ViewHolder.get(view,R.id.tvFileSize);
        final LinearLayout llDetail = (LinearLayout) ViewHolder.get(view,R.id.ll_detail);
        TextView tvFilePath=(TextView) ViewHolder.get(view,R.id.item_file_path);
        TextView item_file_size=(TextView) ViewHolder.get(view,R.id.item_file_size);
        TextView tvFileUpdateTime=(TextView) ViewHolder.get(view,R.id.item_fileuppdate_time);
        final FileBean fileBean=getItem(i);
        tvFileName.setText(fileBean.getName());
        tvFileSize.setText(formetFileSize(fileBean.getSize()));
        long time = fileBean.getModifiedTime();
        //Log.d("文件的时间",String.valueOf(time));
        Date d = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        tvFileUpdateTime.setText(String.format("%s%s","时间：",sdf.format(d)));
        tvFilePath.setText(String.format("%s%s","位置：",fileBean.getPath()));
        item_file_size.setText(String.format("%s%s","大小：",formetFileSize(fileBean.getSize())));
        imageFileSpread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if (imageFileSpread.isSelected()){
                    imageFileSpread.setSelected(false);
                    llDetail.setVisibility(View.GONE);
                }else{
                    imageFileSpread.setSelected(true);
                    llDetail.setVisibility(View.VISIBLE);
                }
            }
        });
        return view;
    }
    public static class ViewHolder{
        public static <T extends View> T get(View view, int id) {
            SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
            if (viewHolder == null) {
                viewHolder = new SparseArray<View>();
                view.setTag(viewHolder);
            }
            View childView = viewHolder.get(id);
            if (childView == null) {
                childView = view.findViewById(id);
                viewHolder.put(id, childView);
            }
            return (T) childView;

        }
    }
    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public String formetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }
}
