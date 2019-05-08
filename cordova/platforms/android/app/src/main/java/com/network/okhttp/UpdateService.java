package com.network.okhttp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;

import com.ninecm.np.R;
import com.ninecm.utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 处理app版本在后台服务中静默更新
 */
public class UpdateService extends Service {
    private NotificationManager notificationManager;
    private String app_name="顺风饭店";
    private String path;
    private float versionSize;
    private Notification.Builder builder;
    private Notification notification=null;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("channel_01", "顺风饭店版本更新", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 每次服务启动的时候都调用，
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String appDownLoadApk = intent.getStringExtra("downloadpath");
        versionSize =Float.parseFloat(intent.getStringExtra("versionSize"));
        //拿到下载app的sdcard路径
        String appName = "xiyue.apk";
        path = FileUtil.initPath("apk")+ File.separator + appName;
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        //notifyUser(app_name,"正在下载", 0);
        OkhttpUtils.getInstance().downloadFile(appDownLoadApk, UpdateService.this, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                notifyUser(app_name,"下载失败",0);
                notificationManager.cancel(0);
                stopSelf();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                File apkfile = new File(path);
                InputStream is = null;
                FileOutputStream fos = null;
                long sum=0;
                try {
                    if (response != null) {
                        is = response.body().byteStream();
                        fos = new FileOutputStream(apkfile);
                        int len = 0;
                        byte[] buffer = new byte[2048];
                        while (-1 != (len = is.read(buffer))) {
                            fos.write(buffer, 0, len);
                            sum+=len;
                            notifyUser(app_name,"正在下载",(int)(sum*100/versionSize));
                        }
                        fos.flush();
                        notifyUser(app_name,"下载完成",100);
                        notificationManager.cancel(0);
                    }
                } catch (IOException e) {

                } finally {
                    try {
                        if (is != null) is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null) fos.close();
                    } catch (IOException e) {
                    }

                }
                stopSelf();
              }
        });
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(0);
    }
    /**
     * 更新notification
     * @param title
     * @param msg
     * @param progress
     */
    private void notifyUser(String title, String msg, int progress){
        if(builder==null){
            builder = new Notification.Builder(this);
        }
        builder.setTicker(title);
        builder.setSmallIcon(R.mipmap.icon);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.icon));
        builder.setAutoCancel(true);
        //PendingIntent contentIntent = progress>=100 ? getContentIntent(): ;
        builder.setContentTitle(app_name);
        builder.setContentText("已完成"+progress+"%");
        PendingIntent contentIntent = progress>=100 ? getContentIntent(): PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
//        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setOngoing(true);
        if(progress>0 && progress<=100){
            builder.setProgress(100,progress,false);
        }else{
            builder.setProgress(0, 0, false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("channel_01");
        }
        notification = builder.build();
        notificationManager.notify(0, notification);
    }
    /**
     * 进入apk安装程序
     * @return
     */
    private PendingIntent getContentIntent(){
        File apkFile = new File(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri fileUri = null;
        // 系统版本大于N的统一用FileProvider处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 将文件转换成content://Uri的形式
            fileUri = FileProvider.getUriForFile(UpdateService.this, UpdateService.this.getPackageName() + ".provider", apkFile);
            // 申请临时访问权限
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            fileUri = Uri.fromFile(apkFile);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        startActivity(intent);
        return pendingIntent;

    }
}
