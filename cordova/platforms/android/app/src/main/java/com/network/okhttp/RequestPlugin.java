package com.network.okhttp;


import android.content.Intent;

import com.ninecm.utils.FileUtil;
import com.ninecm.utils.GsonQuick;

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
import java.util.ArrayList;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * =====================================
 * 作    者: 白小兵
 * 版    本：1.0.0
 * 创建日期：2016/1/12
 * 描    述：网络请求插件处理类
 * =====================================
 */

public class RequestPlugin extends CordovaPlugin{
    private CallbackContext callbackContext;
    private String url;
    private String parameters;
    private File file;
    @Override
    public boolean execute(String action,JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext=callbackContext;
        final JSONObject jsonObject=args.getJSONObject(0);
        /**
         * 发送post请求
         */
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
            /**
             * 发送post请求
             */
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
            /**
             * 下载图片
             */
        }else if("download".equals(action)){
            url=jsonObject.getString("url");
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    final String fileDir="download";
                    OkhttpUtils.getInstance().downloadFile(url, cordova.getContext(), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            InputStream is=null;
                            FileOutputStream fos=null;
                            ResponseBody responseBody=response.body();
                            long contentLength=responseBody.contentLength();
                            try {
                                if (response != null) {
                                    long sum=0;
                                    is = responseBody.byteStream();
                                    fos = new FileOutputStream(new File(FileUtil.initPath(fileDir) + "/" +url.substring(url.lastIndexOf("/")+1)));
                                    int len = 0;
                                    byte[] buffer = new byte[2048];
                                    while (-1 != (len = is.read(buffer))) {
                                        sum+=len;
                                        fos.write(buffer, 0, len);
                                        JSONObject jsonObject=new JSONObject();
                                        jsonObject.put("code","200");
                                        int progress = (int) (sum * 100 / contentLength);
                                        jsonObject.put("progress",String.valueOf(progress));
                                        jsonObject.put("completed",String.valueOf(sum));
                                        jsonObject.put("total",String.valueOf(contentLength));
                                        jsonObject.put("cachepath","");
                                        PluginResult progressResult = new PluginResult(PluginResult.Status.OK,jsonObject);
                                        progressResult.setKeepCallback(true);
                                        callbackContext.sendPluginResult(progressResult);
                                    }
                                    fos.flush();
                                }
                            }catch (JSONException e){

                            }
                            catch (IOException e){

                            } finally
                            {
                                try
                                {
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
                    });
                }
            });
            return true;
            /**
             * 上传图片和文字
             */
        }else if("uploadimage".equals(action)){
            url=jsonObject.getString("url");
            parameters=jsonObject.getString("parameters");
            String isoriginal=jsonObject.getString("isoriginal");
            String imageTag=jsonObject.getString("imageTag");
            cordova.getThreadPool().execute(new Runnable(){
                @Override
                public void run() {
                    JSONArray filePaths= null;
                    ArrayList<String> listPaths=new ArrayList<String>();
                    try {
                        filePaths = jsonObject.getJSONArray("filePaths");
                        if(filePaths!=null&&filePaths.length()>0){
                            for (int i = 0; i <filePaths.length() ; i++) {
                                String path=filePaths.getString(i);
                                file=new File(path);
                                listPaths.add(path);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                  OkhttpUtils.getInstance().uploadImage(url,imageTag, GsonQuick.toJsonMap(parameters), listPaths, cordova.getContext(), new Callback() {
                      @Override
                      public void onFailure(Call call, IOException e) {
                          callbackContext.error("网络通讯异常");
                      }
                      @Override
                      public void onResponse(Call call, Response response) throws IOException {
                          try {
                              String str=response.body().string();
                              try {
                                  JSONObject jsonObject=new JSONObject(str);
                                  callbackContext.success(jsonObject);
                              } catch (JSONException e) {
                                  callbackContext.error("JSON数据解析异常");
                              }
                          } catch (IOException e) {
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
                  /*  Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(url);
                    intent.setData(content_url);
                    cordova.getActivity().startActivity(intent);*/
                }
            });
            return true;
        }
        return false;
    }
}
