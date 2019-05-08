package com.network.okhttp;

import android.content.Context;


import com.xitech.utils.GsonQuick;
import com.xitech.xiapp.R;



import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;


import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static java.lang.String.valueOf;

/**
 * Created by Administrator on 2018/1/25.
 */

public class OkhttpUtils {
    private volatile static OkhttpUtils mInstance;
    public OkHttpClient getOkHttpClient(Context context) {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.test_cert);
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            builder.connectTimeout(10000L, TimeUnit.MILLISECONDS);
            builder.readTimeout(10000L, TimeUnit.MILLISECONDS);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            builder.sslSocketFactory(HttpsUtils.getSocketFactory(inputStream));
            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private OkhttpUtils() {
    }
    public static OkhttpUtils getInstance() {
        if (mInstance == null) {
            synchronized (OkhttpUtils.class) {
                if (mInstance == null) {
                    mInstance = new OkhttpUtils();
                }
            }
        }
        return mInstance;
    }
    public OkHttpClient getOkHttpClient(Context context, Interceptor interceptor) {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.test_cert);
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            builder.connectTimeout(10000L, TimeUnit.MILLISECONDS);
            builder.readTimeout(10000L, TimeUnit.MILLISECONDS);
            builder.addNetworkInterceptor(interceptor);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            builder.sslSocketFactory(HttpsUtils.getSocketFactory(inputStream));
            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public void post(String url,String params,Context context,Callback callback){
        OkHttpClient client =getOkHttpClient(context);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON,params);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }
    public void get(String url,String params,Context context,Callback callback){
        HashMap<String,Object> paramsMap= GsonQuick.toJsonMap(params);
        StringBuilder tempParams = new StringBuilder();
        OkHttpClient client = getOkHttpClient(context);
        try {
            //处理参数
            int pos = 0;
            for (String key : paramsMap.keySet()) {
                if (pos > 0) {
                    tempParams.append("&");
                }
                //对参数进行URLEncoder
                tempParams.append(String.format("%s=%s", key, URLEncoder.encode((String)paramsMap.get(key), "utf-8")));
                pos++;
            }
            //补全请求地址
            String requestUrl = String.format("%s?%s",url, tempParams.toString());
            Request request = new Request.Builder()
                    .url(requestUrl)
                    .get()
                    .build();
            client.newCall(request).enqueue(callback);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    /**
     * 上传文件
     * @param url
     * @param map
     * @param filePaths
     *
     */
    public void uploadImage(String url, Map<String, Object> map, ArrayList<String> filePaths,Context context,FileCallBack fileCallBack) {
        OkHttpClient client =getOkHttpClient(context);
        // form 表单形式上传
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if(filePaths != null&&filePaths.size()>0){
            for (int i = 0; i<filePaths.size(); i++) {
                File file=new File(filePaths.get(i));
                // MediaType.parse() 里面是上传的文件类型。
                RequestBody body = RequestBody.create(MediaType.parse("image/*"), file);
                String filename = file.getName();
                // 参数分别为， 请求key ，文件名称 ， RequestBody
                requestBody.addFormDataPart("picFile", file.getName(), body);
            }
        }
        if (map != null) {
            // map 里面是请求中所需要的 key 和 value
            for (Map.Entry entry : map.entrySet()){
                requestBody.addFormDataPart(valueOf(entry.getKey()), valueOf(entry.getValue()));
            }
        }
        MultipartBody multipartBody =requestBody.build();
        Request request = new Request.Builder().url(url).post(new ProgressRequestBody(multipartBody, fileCallBack)).tag(context).build();
        // readTimeout("请求超时时间" , 时间单位);
        client.newCall(request).enqueue(fileCallBack);
    }
    public void downloadFile(String url,Context context,final FileCallBack fileCallBack){
        OkHttpClient client=getOkHttpClient(context, new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                return response.newBuilder().body(new ProgressResponseBody(response.body(),fileCallBack)).build();
            }
        });
        Request request  = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(fileCallBack);
    }
}
