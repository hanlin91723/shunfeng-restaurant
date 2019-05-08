package com.google.zxing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import com.giants.imagepicker.compresshelper.StringUtil;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.encoding.EncodingHandler;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static android.app.Activity.RESULT_OK;


/**
 * This class echoes a string called from JavaScript.
 */
public class BarcodeScanner extends CordovaPlugin {
    private CallbackContext callbackContext;
    //打开扫描界面请求码
    private int REQUEST_CODE = 0x01;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("scan")) {
            this.callbackContext=callbackContext;
            this.scan();
            return true;
        }else if(action.equals("encode")){
            this.callbackContext=callbackContext;
            decodeBarcode(args);
            return true;
        }
        return false;
    }

    private void scan(){
        //打开二维码扫描界面
        if(CommonUtil.isCameraCanUse()){
            Intent intent = new Intent(cordova.getActivity().getApplicationContext(),CaptureActivity.class);
            cordova.startActivityForResult(this,intent, REQUEST_CODE);
        }else{
            Toast.makeText(cordova.getActivity(),"请打开此应用的摄像头权限！",Toast.LENGTH_SHORT).show();
        }
    }
    private  void decodeBarcode(JSONArray args){
        try {
            JSONObject param=args.getJSONObject(0);
            String type=param.getString("type");
            String data=param.getString("data");
            String logoUrl=param.getString("url");
            String options=param.getString("option");
            String path=null;
            if(!"".equals(logoUrl)&&logoUrl!=null){
                File file=new File(logoUrl);
                if(!file.exists()){
                    path= EncodingHandler.createQRCode(data,1000);
                }else{
                    Bitmap bitmap= BitmapFactory.decodeFile(logoUrl);
                    //根据输入的文本生成对应的二维码并且显示出来
                    path= EncodingHandler.createQRCode(data, 1000,1000,bitmap);

                }
            }else{
                path= EncodingHandler.createQRCode(data,1000);
            }
            if (!StringUtil.isEmpty(path)){
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("format","二维码");
                jsonObject.put("file",path);
                this.callbackContext.success(jsonObject);
            }else{
                this.callbackContext.error("二维码生成失败");
            }
        }catch (WriterException e){
            this.callbackContext.error(e.getMessage());
        } catch (JSONException e) {
            this.callbackContext.error(e.getMessage());
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //扫描结果回调
        if (resultCode == RESULT_OK){
            Bundle bundle = intent.getExtras();
            String scanResult = bundle.getString("qr_scan_result");
            if(scanResult.equals("未发现二维码")){
                this.callbackContext.error(scanResult);
            }else{
                JSONObject jsonObject=new JSONObject();
                try {
                    jsonObject.put("text",scanResult);
                    jsonObject.put("format","二维码");
                    jsonObject.put("cancelled","");
                    this.callbackContext.success(jsonObject);
                } catch (JSONException e) {
                    this.callbackContext.success(e.getMessage());
                }

            }
        }
    }
}
