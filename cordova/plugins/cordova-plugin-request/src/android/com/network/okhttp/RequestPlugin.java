package com.network.okhttp;


import android.content.Intent;
import android.graphics.Bitmap;

import com.xitech.utils.FileUtil;
import com.xitech.utils.GsonQuick;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Created by Administrator on 2018/1/31.
 */

public class RequestPlugin extends CordovaPlugin{
    private CallbackContext callbackContext;
    private String url;
    private String parameters;
    @Override
    public boolean execute(String action,JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext=callbackContext;
        JSONObject jsonObject=args.getJSONObject(0);
        if("post".equals(action)){
         url=jsonObject.getString("url");
         parameters=jsonObject.getString("parameters");
         cordova.getThreadPool().execute(new Runnable() {
             @Override
             public void run() {
                 OkhttpUtils.getInstance().post(url, parameters, cordova.getContext(),new Callback(){
                     @Override
                     public void onFailure(Call call, IOException e) {
                         callbackContext.error(e.getMessage());
                     }
                     @Override
                     public void onResponse(Call call, Response response) throws IOException{
                         try {
                             JSONObject jsonObject=new JSONObject(response.body().string());
                             callbackContext.success(jsonObject);
                         } catch (JSONException e) {
                             e.printStackTrace();
                             callbackContext.error(e.getMessage());
                         }
                     }
                 });
             }
         });
         return true;
        }else if("get".equals(action)){
            url=jsonObject.getString("url");
            parameters=jsonObject.getString("parameters");
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    OkhttpUtils.getInstance().get(url,parameters,cordova.getContext(),new Callback(){
                        @Override
                        public void onFailure(Call call, IOException e) {
                            callbackContext.error(e.getMessage());
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException{
                            try {
                                JSONObject jsonObject=new JSONObject(response.body().string());
                                callbackContext.success(jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                callbackContext.error(e.getMessage());
                            }
                        }
                    });
                }
            });
            return true;
        }else if("download".equals(action)){
            url=jsonObject.getString("url");
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    final String fileDir="download";
                    OkhttpUtils.getInstance().downloadFile(url,cordova.getContext(),new FileCallBack() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            callbackContext.error(e.getMessage());
                        }
                        @Override
                        public void onResponse(Call call, Response response){
                            InputStream is=null;
                            FileOutputStream fos=null;
                            try {
                                if (response != null) {
                                    is = response.body().byteStream();
                                    fos = new FileOutputStream(new File(FileUtil.initPath(fileDir) + "/" +url.substring(url.lastIndexOf("/")+1)));
                                    int len = 0;
                                    byte[] buffer = new byte[2048];
                                    while (-1 != (len = is.read(buffer))) {
                                        fos.write(buffer, 0, len);
                                    }
                                    fos.flush();
                                }
                            }catch (IOException e){

                            } finally
                            {
                                try
                                {
                                    response.body().close();
                                    if (is != null) is.close();
                                } catch (IOException e)
                                {
                                }
                                try
                                {
                                    if (fos != null) fos.close();
                                } catch (IOException e)
                                {
                                }

                            }
                        }
                        @Override
                        public void onProgress(long currentLength, long totalLength, boolean isCompleted) {
                            try {
                                JSONObject jsonObject=new JSONObject();
                                jsonObject.put("code","200");
                                int progress = (int) (currentLength * 100 / totalLength);
                                jsonObject.put("progress",String.valueOf(progress));
                                jsonObject.put("completed",String.valueOf(currentLength));
                                jsonObject.put("total",String.valueOf(totalLength));
                                jsonObject.put("cachepath","");
                                PluginResult progressResult = new PluginResult(PluginResult.Status.OK,jsonObject);
                                progressResult.setKeepCallback(true);
                                callbackContext.sendPluginResult(progressResult);
                            } catch (JSONException e) {
                                callbackContext.error(e.getMessage());
                            }
                        }
                    });
                }
            });
            return true;
        }else if("uploadimage".equals(action)){
            url=jsonObject.getString("url");
            parameters=jsonObject.getString("parameters");
            String isoriginal=jsonObject.getString("isoriginal");
            JSONArray filePaths=jsonObject.getJSONArray("filePaths");
            final ArrayList<String> listPaths=new ArrayList<String>();
            if("1".equals(isoriginal)){
                for (int i = 0; i <filePaths.length() ; i++) {
                    String path=filePaths.getString(i);
                    listPaths.add(path);
                }
            }else{
                for (int i = 0; i <filePaths.length() ; i++) {
                    String path=filePaths.getString(i);
                    commpressImage(path,listPaths);
                }
            }
            cordova.getThreadPool().execute(new Runnable(){
                @Override
                public void run() {
                  OkhttpUtils.getInstance().uploadImage(url, GsonQuick.toJsonMap(parameters),listPaths,cordova.getContext(),new FileCallBack(){
                      @Override
                      public void onFailure(Call call, IOException e) {
                          callbackContext.error(e.getMessage());
                      }
                      @Override
                      public void onResponse(Call call, Response response){

                      }
                      @Override
                      public void onProgress(long currentLength, long totalLength, boolean isCompleted) {
                          try{
                              JSONObject jsonObject=new JSONObject();
                              jsonObject.put("code","200");
                              int progress = (int) (currentLength * 100 / totalLength);
                              jsonObject.put("progress",String.valueOf(progress));
                              jsonObject.put("completed",String.valueOf(currentLength));
                              jsonObject.put("total",String.valueOf(totalLength));
                              jsonObject.put("cachepath","");
                              PluginResult progressResult = new PluginResult(PluginResult.Status.OK,jsonObject);
                              progressResult.setKeepCallback(true);
                              callbackContext.sendPluginResult(progressResult);
                          } catch (JSONException e) {
                              callbackContext.error(e.getMessage());
                          }
                      }
                  });
                }
            });
            return true;
        }else if("getVersionCode".equals(action)){
            this.callbackContext.success(DeviceUtil.getVersionCode(cordova.getContext()));
            return true;
        }else if("getVersionName".equals(action)){
            this.callbackContext.success(DeviceUtil.getVersionName(cordova.getContext()));
            return true;
        }else if("updateAppVersion".equals(action)){
            url=jsonObject.getString("url");
            String versionSize=jsonObject.getString("versionSize");
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    Intent intentService=new Intent(cordova.getActivity(),UpdateService.class);
                    intentService.putExtra("downloadpath",url);
                    intentService.putExtra("versionSize",versionSize);
                    cordova.getActivity().startService(intentService);
                }
            });
            return true;
        }
        return false;
    }
    private void commpressImage(String path,ArrayList<String> listPaths) {
            String tmpPath = null;
            Bitmap bitmap = CompressImage.getBitmap(path);
            Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
            int quality = 100;
            OutputStream stream = null;
            try {
                tmpPath =FileUtil.initPath("thumbnail") + File.separator + UUID.randomUUID() + ".png";
                stream = new FileOutputStream(tmpPath);
                bitmap.compress(format, quality, stream);
                // 上传图片
                File file = new File(tmpPath);
                listPaths.add(tmpPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}
