package com.cjt2325.cameralibrary;

import android.content.Intent;
import android.graphics.BitmapFactory;
import com.ninecm.utils.FileUtil;

import net.bither.util.NativeUtil;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;

/**
 * =====================================
 * 作    者: 白小兵
 * 版    本：1.0.0
 * 创建日期：2016/1/12
 * 描    述：拍照插件处理类
 * =====================================
 */
public class XiCamera extends CordovaPlugin{
    private  CallbackContext callbackContext;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("showCamera")) {
            this.callbackContext=callbackContext;
            Intent intent = new Intent(cordova.getActivity().getApplicationContext(), CameraActivity.class);
            cordova.startActivityForResult((CordovaPlugin)this,intent, 100);
            return true;
        }
        return false;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode ==CameraActivity.RESULT_CODE) {
            if (intent != null && requestCode == 100) {
                try {
                    File oldFile=new File(intent.getStringExtra(CameraActivity.EXTRA_RESULT_URL));
                   // File newFile = CompressImage.compressToFile(oldFile.getAbsolutePath(),720, 1280,100,1000);
                    File newFile = new File(FileUtil.initPath("libpeg"), "compress_" + System.currentTimeMillis() + ".png");
                    NativeUtil.compressBitmap(BitmapFactory.decodeFile(oldFile.getAbsolutePath()), newFile.getAbsolutePath());
                    JSONObject jsonObject=new JSONObject();
                    try {
                        jsonObject.put("oldFilePath",oldFile.getAbsolutePath());
                        jsonObject.put("oldFileSize",formetFileSize(oldFile.length()));
                        if(newFile!=null&&newFile.exists()&&newFile.isFile()){
                            if(oldFile.length()>newFile.length()){
                                jsonObject.put("tempFilePath",newFile.getAbsolutePath());
                                jsonObject.put("tempFileSize",formetFileSize(newFile.length()));
                            }else{
                                jsonObject.put("tempFilePath",oldFile.getAbsolutePath());
                                jsonObject.put("tempFileSize",formetFileSize(oldFile.length()));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        this.callbackContext.error(e.getMessage());
                    }
                    this.callbackContext.success(jsonObject);
                } catch (Exception e) {
                    e.printStackTrace();
                    this.callbackContext.success(e.getMessage());
                }
            }
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

