package com.wallet.sign;


import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.runtimepermissions.PermissionsManager;
import com.ninecm.np.R;
import com.ninecm.utils.FileUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;


/**
 * =====================================
 * 作    者: 白小兵
 * 版    本：1.0.0
 * 创建日期：2018/1/12
 * 描    述：钱包文件列表显示页面
 * =====================================
 */
public class SearchActivity extends AppCompatActivity implements View.OnClickListener,MediaScanner.OnCompleted{
    private FileAdapter adapter;
    private ListView listView;
    private ImageView iv_back;
    public static final int HANDLER_WHAT = 111;
    public static final int HANDLER_SCANNER_DIR = 112;

    private Button btnRefresh;
    private AlertDialog waitDialog;

    private String scanText;
    volatile int  threadID=0;
	private String WALLETNAME="ONBWALLET";

    private static List<FileBean> globalFileBeanList=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_search);
        listView=(ListView) findViewById(R.id.listView);
        iv_back=(ImageView) findViewById(R.id.toolbar_back);
        btnRefresh=(Button)findViewById(R.id.scanner_toolbar_refesh);

        btnRefresh.setOnClickListener(this);


        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchActivity.this.finish();
            }
        });


        //
        if(globalFileBeanList.size()>0) {
            List<FileBean> fileBeanList=new ArrayList<>();
            for (FileBean bean:globalFileBeanList) {
                if(new File(bean.getPath()).exists()){
                    fileBeanList.add(bean);
                }
            }
            adapter = new FileAdapter(SearchActivity.this, fileBeanList);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent();
                    FileBean fileBean = (FileBean) adapterView.getItemAtPosition(i);
                    intent.putExtra("filePath", fileBean.getPath());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }
        else
        {
            btnRefresh.callOnClick();
        }
    }

    private Map<String,Boolean> initCommonPath()
    {
        Map<String,Boolean> commonPath= new HashMap<>();
        commonPath.put(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"tencent",false);
        commonPath.put(Environment.getExternalStoragePublicDirectory(Environment.
                DIRECTORY_DOWNLOADS).getAbsolutePath(),false);
        commonPath.put(Environment.getExternalStoragePublicDirectory(Environment.
                DIRECTORY_DOCUMENTS).getAbsolutePath(),false);
        commonPath.put(Environment.getExternalStoragePublicDirectory(Environment.
                DIRECTORY_DCIM).getAbsolutePath(),false);

        return commonPath;
    }
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what==HANDLER_WHAT){
                Log.d("ccc","ID:"+String.valueOf(msg.arg1)+" "+String.valueOf(threadID));
                int id=msg.arg1;
                if(msg.arg2==0xff)
                {
                    if(threadID >(id+1))
                    {
                        if(waitDialog.isShowing()) {
                            waitDialog.dismiss();
                        }
                        return;
                    }
                }
                else
                {

                    if( id!=threadID)
                    {
                        if(waitDialog.isShowing()) {
                            waitDialog.dismiss();
                        }
                        return;
                    }
                }
                List<FileBean> fileBeanList = (List<FileBean>) msg.obj;
                Log.d("ccc",String.valueOf(msg.arg1)+" "+String.valueOf(msg.arg2)+" "+String.valueOf(fileBeanList.size())+" "+waitDialog.isShowing());

                if (fileBeanList.size() <= 0) {
                    Toast.makeText(SearchActivity.this, "  您手机上没有备份钱包文件  ", Toast.LENGTH_SHORT).show();
                } else {

                    Collections.sort(fileBeanList);
                    Collections.reverse(fileBeanList);
                    adapter = new FileAdapter(SearchActivity.this, fileBeanList);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Intent intent = new Intent();
                            FileBean fileBean = (FileBean) adapterView.getItemAtPosition(i);
                            intent.putExtra("filePath", fileBean.getPath());
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });

                    globalFileBeanList.clear();
                    globalFileBeanList=fileBeanList;

                }

                if(waitDialog.isShowing()) {
                    waitDialog.dismiss();
                }
            }
            else if(msg.what==HANDLER_SCANNER_DIR)
            {
                if(waitDialog.isShowing())
                {
                    if(msg.arg1 == threadID)
                    {
                        String str=(String)msg.obj;
                        waitDialog.setMessage(str);
                    }
                    else
                    {
                        waitDialog.setMessage("正在停止扫描，请稍候...");
                    }

                }
            }


        }
    };

    /**
     * 保存后用广播扫描，Android4.4以下使用这个方法
     * @author YOLANDA
     */
    private void scanByBroadcast(Context ctx,String filePath){
        //String filePath="/storage/emulated/0/tencent";
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath)));
//        Toast.makeText(ctx, "保存成功：" + filePath, Toast.LENGTH_LONG).show();
    }

    /**
     * 保存后用MediaScanner扫描，通用的方法
     * @author YOLANDA
     */
    private void scanByMediaScannerConnection(Context ctx,String filePath){
        //String filePath ="/storage/emulated/0/tencent" ;
        MediaScanner mediaScanner = new MediaScanner(ctx);
        mediaScanner.setOnCompleted(this);
        String[] filePaths = new String[]{filePath};
        String mime=MimeTypeMap.getSingleton().getMimeTypeFromExtension("json");
        String[] mimeTypes = new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension("json")};
        mediaScanner.scanFiles(filePaths, mimeTypes);
//        Toast.makeText(this, "保存成功：" + filePath, Toast.LENGTH_LONG).show();
    }



    public void scanDir(Context ctx, String dir) {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            scanByBroadcast(ctx,dir);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            scanByMediaScannerConnection(ctx,dir);
        }

    }

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
                String name=file.getName();
                if(!name.isEmpty() && name.contains(".")) {
                    String prefix = name.substring(name.lastIndexOf(".") + 1);
                    if(prefix.toUpperCase().equals("JSON")) {
                        bean.setName(file.getName());
                        bean.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)));
                        bean.setMimeType(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)));
                        bean.setAddTime(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)));
                        bean.setModifiedTime(file.lastModified() == 0 ? cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)) * 1000L : file.lastModified());
                        fileList.add(bean);
                    }
                }
            }
            //最后记得关闭游标，减少内存消耗
            cursor.close();
        }
        return fileList;
    }

//    @Override
//    public void onClick(View view) {
//        if(view == btnRefresh)
//        {
//            waitDialog = new ProgressDialog(this);
//            waitDialog.setCancelable(false);
//            waitDialog.setCanceledOnTouchOutside(false);
//            waitDialog.setMessage("正在刷新目录，请稍侯...");
//            waitDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "停止", new DialogInterface.OnClickListener() {
//                @Override public void onClick(DialogInterface dialog, int which) {
//                    new Thread(){
//                        @Override
//                        public void run() {
//                            super.run();
//                            List<FileBean> fileBeanList=searchFileName(SearchActivity.this,WALLETNAME);
//                            //获得外部存储的根目录
//                            Message msg=handler.obtainMessage();
//                            msg.what=HANDLER_WHAT;
//                            msg.obj=fileBeanList;
//                            handler.sendMessage(msg);
//                        }
//                    }.start();
//
//                     }
//                     });
//
//
//            waitDialog.show();
//
//            //
//            String scanPath="/";
//            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                scanPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//            }
//            Log.d("ccc",scanPath);
//            scanDir(this,scanPath);
//        }
//    }

    @Override
    public void onClick(View view) {
        if(view == btnRefresh)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                try {
//                String[] permissions = new String[]{};
                    List<String> permissions = new ArrayList<>();
                    //检测是否有写的权限
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }

                    if (permissions.size() > 0) {
                        String[] permissionsArray = permissions.toArray(new String[permissions.size()]);
                        // 没有写的权限，去申请写的权限，会弹出对话框
                        ActivityCompat.requestPermissions(this, permissionsArray, 0x01);
                    }
                    else{
                        refreshDir();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                refreshDir();
            }

        }
    }

    private void refreshDir()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setNegativeButton("停止", null);

        waitDialog = builder.create();
        waitDialog.setCancelable(false);
        waitDialog.setCanceledOnTouchOutside(false);

        //
        threadID++;
        int id=threadID;

        Map<String,Boolean> commonPath=initCommonPath();
        FileScanResult finalResult=new FileScanResult();
        scanText="正在扫描常用目录，请稍候...\n\n";
        waitDialog.setMessage(scanText+"已扫描文件数： 0\n"+"已找到钱包数： 0");


        waitDialog.show();

        if(waitDialog.getButton(waitDialog.BUTTON_NEGATIVE)!=null) {
            waitDialog.getButton(waitDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(id == threadID) {

                        waitDialog.getButton(waitDialog.BUTTON_NEGATIVE).setVisibility(GONE);
                        threadID++;

                    }

                }
            });
        }



        //
        new Thread(new Runnable() {
            @Override
            public void run() {

                //遍历map中的键
                for (String key : commonPath.keySet()) {
                    if (threadID == id) {
                        System.out.println("Key = " + key);

                        File path = new File(key);
                        if (path.exists()) {
                            FileScanResult result=scanDirNoRecursion(path.getAbsolutePath(),id,commonPath,finalResult);
                            finalResult.scanFileNum+=result.scanFileNum;
                            finalResult.scanTotalNum+=result.scanTotalNum;
                            if(result.scanFileNum>0)
                            {
                                finalResult.fileScanList.addAll(result.fileScanList);
                            }
                        }
                        commonPath.put(key, true);
                    }
                    else
                    {
                        Message msg = handler.obtainMessage();
                        msg.what = HANDLER_WHAT;
                        msg.obj = finalResult.fileScanList;
                        msg.arg1 = id;
                        msg.arg2=0xff;
                        handler.sendMessage(msg);
                        return;
                    }

                }

                if(threadID == id) {
                    Message msg = handler.obtainMessage();
                    msg.what = HANDLER_SCANNER_DIR;
                    scanText = "已扫描完常用目录，正在扫描其他目录，请稍候...\n\n";
                    msg.obj = scanText + "已扫描文件数： " + finalResult.scanTotalNum + "\n" + "已找到钱包数： " + finalResult.scanFileNum;
                    msg.arg1 = id;
                    handler.sendMessage(msg);
                }

                String scanPath = "/";
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    scanPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                }
                FileScanResult result=scanDirNoRecursion(scanPath,id,commonPath,finalResult);
                finalResult.finishScan=result.finishScan;
                finalResult.scanFileNum+=result.scanFileNum;
                finalResult.scanTotalNum+=result.scanTotalNum;
                if(result.scanFileNum>0)
                {
                    finalResult.fileScanList.addAll(result.fileScanList);
                }


                if(threadID == id) {
                    if(finalResult.finishScan) {
                        Message msg1 = handler.obtainMessage();
                        msg1.what = HANDLER_SCANNER_DIR;
                        msg1.arg1 = id;
                        msg1.obj = "扫描完成\n\n已扫描文件数： " + finalResult.scanTotalNum + "\n" + "已找到钱包数： " + finalResult.scanFileNum;
                        handler.sendMessage(msg1);
                    }
                }
                else
                {
                    Message msg = handler.obtainMessage();
                    msg.what = HANDLER_WHAT;
                    msg.obj = finalResult.fileScanList;
                    msg.arg1 = id;
                    msg.arg2=0xff;
                    handler.sendMessage(msg);

                    return;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(threadID == id) {
                    Message msg2 = handler.obtainMessage();
                    msg2.what = HANDLER_WHAT;
                    msg2.obj = finalResult.fileScanList;
                    msg2.arg1 = id;
                    handler.sendMessage(msg2);
                }
                else
                {
                    Message msg = handler.obtainMessage();
                    msg.what = HANDLER_WHAT;
                    msg.obj = finalResult.fileScanList;
                    msg.arg1 = id;
                    msg.arg2=0xff;
                    handler.sendMessage(msg);

                    return;
                }

            }
        }).start();
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
        if(requestCode == 0x01)
        {
            if(grantResults.length >0)
            {
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED)
                {
                    refreshDir();
                }
            }

        }
    }

    @Override
    public void scanCompleted() {


        new Thread(){
            @Override
            public void run() {
                super.run();
                List<FileBean> fileBeanList=searchFileName(SearchActivity.this,WALLETNAME);
                //获得外部存储的根目录
                Message msg=handler.obtainMessage();
                msg.what=HANDLER_WHAT;
                msg.obj=fileBeanList;
                handler.sendMessage(msg);
            }
        }.start();

    }


    class FileScanResult
    {
        public int scanTotalNum=0;
        public int scanFileNum=0;
        List<FileBean> fileScanList=new ArrayList<>();
        boolean finishScan=false;
    }
    //非递归
    public FileScanResult scanDirNoRecursion(String path,int id,Map<String,Boolean> commonPath,FileScanResult currentResult){

        LinkedList listDir = new LinkedList();
        File dir = new File(path);

        FileScanResult result=new FileScanResult();

        FilenameFilter filter=new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {

                //dir为扫描的文件的上级文件
                //filename为当前扫描到的文件名
                File file = new File(dir, filename);
                for (String key : commonPath.keySet()) {
                    if(file.getAbsolutePath().startsWith(key) && commonPath.get(key)) {  //已经扫描过，不再返回
                        return false;
                    }
                }

                result.scanTotalNum++;
                if(result.scanTotalNum %11 ==0) {
                    Message msg = handler.obtainMessage();
                    msg.what = HANDLER_SCANNER_DIR;
                    int totalNum = currentResult.scanTotalNum + result.scanTotalNum;
                    int fileNum = currentResult.scanFileNum + result.scanFileNum;
                    msg.obj = scanText + "已扫描文件数： " + totalNum + "\n" + "已找到钱包数： " + fileNum;
                    msg.arg1 = id;
                    handler.sendMessage(msg);
                }

                if(filename.startsWith(".") || filename.startsWith(".."))  //不返回 . 和 ..开始的文件
                {
                    return false;
                }
                if(file.getAbsolutePath().startsWith(FileUtil.getDataCachetPath())) //不获取当前目录
                {
                    return false;
                }

                if(!file.isHidden()) {
                    if(file.isDirectory())
                    {
                        return true;
                    }
                    else {
                        if (file.getName().toUpperCase().endsWith(".JSON") && file.getName().toUpperCase().startsWith(WALLETNAME)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };

        File file[] = dir.listFiles(filter);
        for (int i = 0; i < file.length; i++) {
            if (id == this.threadID) {
                if (file[i].isDirectory()) {
                    listDir.add(file[i]);

                }
                else {
                    if (file[i].getName().toUpperCase().endsWith(".JSON") && file[i].getName().toUpperCase().startsWith(WALLETNAME)) {
                        FileBean bean = new FileBean();
                        bean.setPath(file[i].getAbsolutePath());

                        bean.setName(file[i].getName());
                        bean.setSize(file[i].length());
                        bean.setMimeType("json");
                        bean.setAddTime(file[i].lastModified());
                        bean.setModifiedTime(file[i].lastModified());
                        result.fileScanList.add(bean);
                        result.scanFileNum++;
                    }
                }
            }
            else
            {
                return result;
            }
        }

        if(listDir.isEmpty())
        {
            result.finishScan=true;
            return result;
        }

        while (!listDir.isEmpty()) {
            if (id == this.threadID) {
                File tmp = (File) listDir.removeFirst();//首个目录
                if (tmp.isDirectory()) {
                    file = tmp.listFiles(filter);
                    if (file == null) {
                        continue;
                    }
                    for (int i = 0; i < file.length; i++) {
                        if (id == this.threadID) {
                            if (file[i].isDirectory()) {
                                listDir.add(file[i]);//目录则加入目录列表，关键

                            } else {
                                if (file[i].getName().toUpperCase().endsWith(".JSON") && file[i].getName().toUpperCase().startsWith(WALLETNAME)) {
                                    FileBean bean = new FileBean();
                                    bean.setPath(file[i].getAbsolutePath());

                                    bean.setName(file[i].getName());
                                    bean.setSize(file[i].length());
                                    bean.setMimeType("json");
                                    bean.setAddTime(file[i].lastModified());
                                    bean.setModifiedTime(file[i].lastModified());
                                    result.fileScanList.add(bean);
                                    result.scanFileNum++;
                                }

                            }
                        } else {
                            return result;
                        }
                    }
                }
            }
            else {
                return result;
            }

        }

        result.finishScan=true;
        return result;
    }

}
