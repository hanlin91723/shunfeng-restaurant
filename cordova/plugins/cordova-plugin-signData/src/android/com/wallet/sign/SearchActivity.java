package com.wallet.sign;


import android.content.ContentResolver;

import android.content.Context;
import android.content.Intent;

import android.database.Cursor;

import android.net.Uri;

import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.xitech.xiapp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * =====================================
 * 作    者: 白小兵
 * 版    本：1.0.0
 * 创建日期：2018/1/12
 * 描    述：钱包文件列表显示页面
 * =====================================
 */
public class SearchActivity extends AppCompatActivity{
    private FileAdapter adapter;
    private ListView listView;
    private ImageView iv_back;
    public static final int HANDLER_WHAT = 111;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        listView=(ListView) findViewById(R.id.listView);
        iv_back=(ImageView) findViewById(R.id.toolbar_back);
        new Thread(){
            @Override
            public void run() {
                super.run();
                List<FileBean> fileBeanList=searchFileName(SearchActivity.this,"xiwallet");
                //获得外部存储的根目录
               Message msg=handler.obtainMessage();
               msg.what=HANDLER_WHAT;
               msg.obj=fileBeanList;
               handler.sendMessage(msg);
            }
        }.start();
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchActivity.this.finish();
            }
        });
    }
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==HANDLER_WHAT){
                List<FileBean> fileBeanList=(List<FileBean>) msg.obj;
                if(fileBeanList.size()<=0){
                    Toast.makeText(SearchActivity.this,"您手机上没有备份钱包文件",Toast.LENGTH_SHORT).show();
                  return;
                }
                adapter=new FileAdapter(SearchActivity.this,fileBeanList);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent=new Intent();
                        FileBean fileBean= (FileBean) adapterView.getItemAtPosition(i);
                        intent.putExtra("filePath",fileBean.getPath());
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                });
            }
        }
    };

    /**
     * 根据文件的名称模糊搜索钱包文件
     * @param context
     * @param fileName
     * @return
     */
    private List searchFileName(Context context, String fileName) {
        List<FileBean> fileList = new ArrayList<FileBean>();
        //获取内容共享者
        ContentResolver resolver = context.getContentResolver();
        //获取手机外部存储空间的UrI
        Uri uri=MediaStore.Files.getContentUri("external");
         //根据uri从sqlite库中查出文件的名称，路径大小，和类型
        Cursor cursor=resolver.query(uri,
                new String[]{  MediaStore.Files.FileColumns.TITLE,   //文件的显示名称  aaa.jpg
                        MediaStore.Files.FileColumns.DATA,           //文件的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
                        MediaStore.Files.FileColumns.SIZE,           //文件的大小，long型  132492
                        MediaStore.Files.FileColumns.MIME_TYPE,      //文件的类型     image/jpeg
                        MediaStore.Files.FileColumns.DATE_ADDED,
                        MediaStore.Files.FileColumns.DATE_MODIFIED
                },
                MediaStore.Files.FileColumns.TITLE+ " like?",
                new String[]{"%"+fileName+"%"}, MediaStore.Files.FileColumns.DATE_MODIFIED+" desc");
        if (cursor != null) {
            //对查询出的数据进行循环遍历，封装到FileBen实体类中
            while (cursor.moveToNext()) {
                FileBean bean = new FileBean();
                String path=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
                bean.setPath(path);
                File file=new File(path);
                bean.setName(file.getName());
                bean.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)));
                bean.setMimeType(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)));
                bean.setAddTime(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)));
                bean.setModifiedTime(file.lastModified()==0?cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED))*1000L:file.lastModified());
                fileList.add(bean);
            }
            //最后记得关闭游标，减少内存消耗
            cursor.close();
        }
        return fileList;
    }
}
