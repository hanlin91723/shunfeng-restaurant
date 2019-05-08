package com.network.okhttp;

import android.content.Context;

import com.ninecm.np.R;
import com.ninecm.utils.GsonQuick;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static java.lang.String.valueOf;

/**
 * =====================================
 * 作    者: 白小兵
 * 版    本：1.0.0
 * 创建日期：2016/1/12
 * 描    述：OkHttp的单向加密和双向验证加密
 * =====================================
 */
public class OkhttpUtils {
    private volatile static OkhttpUtils mInstance;
    public OkHttpClient getOkHttpClient(Context context) {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.xichain);
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            builder.connectTimeout(60000L, TimeUnit.MILLISECONDS);
            builder.readTimeout(60000L, TimeUnit.MILLISECONDS);
            builder.writeTimeout(60000L,TimeUnit.MILLISECONDS);
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

    /**
     * OkhttpUtils单例对象，全局只有一个
     * @return
     */
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
            /**
             * 获取公钥证书的字节输入流
             */
            InputStream inputStream = context.getResources().openRawResource(R.raw.test_cert);
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            //http请求连接超时时间
            builder.connectTimeout(60000L, TimeUnit.MILLISECONDS);
            //http请求读取超时时间
            builder.readTimeout(60000L, TimeUnit.MILLISECONDS);
            //http请求写入超时时间
            builder.writeTimeout(60000L,TimeUnit.MILLISECONDS);
            builder.addNetworkInterceptor(interceptor);
            //设置是否校验证书
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            //设置公钥证书的输入流
            builder.sslSocketFactory(HttpsUtils.getSocketFactory(inputStream));
            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * post请求
     * @param url
     * @param params
     * @param context
     * @param callback
     */
    public void post(String url,String params,Context context,Callback callback){
        OkHttpClient client =getUnsafeOkHttpClient();//getOkHttpClient(context);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON,params);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * post请求
     * @param url
     * @param params
     * @param context
     * @param callback
     */
    public void get(String url,String params,Context context,Callback callback){
        HashMap<String,Object> paramsMap= GsonQuick.toJsonMap(params);
        StringBuilder tempParams = new StringBuilder();
        OkHttpClient client = getUnsafeOkHttpClient();//getOkHttpClient(context);
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
    public void uploadImage(String url, String imageTag,Map<String, Object> map, ArrayList<String> filePaths,Context context,Callback callBack) {
        OkHttpClient client =getUnsafeOkHttpClient();//getOkHttpClient(context);
        // form 表单形式上传
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if(filePaths != null&&filePaths.size()>0){
            for (int i = 0; i<filePaths.size(); i++) {
                File file=new File(filePaths.get(i));
                // MediaType.parse() 里面是上传的文件类型。
                RequestBody body = RequestBody.create(MediaType.parse("image/*"), file);
                String filename = file.getName();
                // 参数分别为， 请求key ，文件名称 ， RequestBody
                requestBody.addFormDataPart(imageTag, file.getName(), body);
            }
        }
        if (map != null) {
            // map 里面是请求中所需要的 key 和 value
            for (Map.Entry entry : map.entrySet()){
                requestBody.addFormDataPart(valueOf(entry.getKey()), valueOf(entry.getValue()));
            }
        }
        MultipartBody multipartBody =requestBody.build();
        Request request = new Request.Builder().url(url).post(multipartBody).tag(context).build();
        // readTimeout("请求超时时间" , 时间单位);
        client.newCall(request).enqueue(callBack);
    }

    /**
     * 文件下载
     * @param url
     * @param context
     * @param callback
     */
    public void downloadFile(String url,Context context,Callback callback){
        OkHttpClient client=getUnsafeOkHttpClient();//getOkHttpClient(context);
        Request request  = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }

    public static OkHttpClient getUnsafeOkHttpClient() {

        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);

            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
