package com.network.okhttp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 包装的响体，处理进度
 */
public class ProgressResponseBody extends ResponseBody {
    //实际的待包装响应体
    private final ResponseBody responseBody;
    //进度回调接口
    private final FileCallBack fileCallBack;
    //包装完成的BufferedSource
    private BufferedSource bufferedSource;

    public static final int UPDATE = 0x01;

    private Handler myHandler;

    /**
     * 构造函数，赋值
     *
     * @param responseBody     待包装的响应体
     * @param fileCallBack 回调接口
     */
    public ProgressResponseBody(ResponseBody responseBody, FileCallBack fileCallBack) {
        this.responseBody = responseBody;
        this.fileCallBack = fileCallBack;
        if (myHandler==null){
            myHandler = new MyHandler();
        }
    }

    /**
     * 将进度放到主线程中显示
     */
    class MyHandler extends Handler {

        public MyHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE:
                    ProgressModel progressModel = (ProgressModel) msg.obj;
                    //接口返回
                    if (fileCallBack !=null) fileCallBack.onProgress(progressModel.getCurrentLength(),progressModel.getTotalLength(),progressModel.isCompleted());
                    break;

            }
        }
    }

    /**
     * 重写调用实际的响应体的contentType
     *
     * @return MediaType
     */
    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    /**
     * 重写调用实际的响应体的contentLength
     *
     * @return contentLength
     * @throws IOException 异常
     */
    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    /**
     * 重写进行包装source
     *
     * @return BufferedSource
     */
    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            //包装
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    /**
     * 读取，回调进度接口
     *
     * @param source Source
     * @return Source
     */
    private Source source(Source source) {

        return new ForwardingSource(source) {
            //当前读取字节数
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                //增加当前读取的字节数，如果读取完成了bytesRead会返回-1
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                //发送消息到主线程
                Message msg = Message.obtain();
                msg.what = UPDATE;
                msg.obj =  new ProgressModel(totalBytesRead,contentLength(),totalBytesRead==contentLength());
                myHandler.sendMessage(msg);
                return bytesRead;
            }
        };
    }
}