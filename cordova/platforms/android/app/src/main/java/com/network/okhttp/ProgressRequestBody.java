package com.network.okhttp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * 包装的请求体，处理进度
 */
public  class ProgressRequestBody extends RequestBody {
    public static final int UPDATE = 0x01;
    //实际的待包装请求体
    private  RequestBody requestBody;
    //进度回调接口
    private FileCallBack fileCallBack;
    //包装完成的BufferedSink
    private BufferedSink bufferedSink;

    private MyHandler myHandler;
    class MyHandler extends Handler {
        //放在主线程中显示
        public MyHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE:
                    ProgressModel progressModel = (ProgressModel) msg.obj;
                    if (fileCallBack !=null) fileCallBack.onProgress(progressModel.getCurrentLength(),progressModel.getTotalLength(),progressModel.isCompleted());
                    break;

            }
        }


    }

    /**
     * 构造函数，赋值
     * @param requestBody 待包装的请求体
     * @param fileCallBack 回调接口
     */
    public ProgressRequestBody(RequestBody requestBody, FileCallBack fileCallBack) {
        this.requestBody = requestBody;
        this.fileCallBack = fileCallBack;
        if(this.myHandler==null){
            myHandler=new MyHandler();
        }
    }

    /**
     * 重写调用实际的响应体的contentType
     * @return MediaType
     */
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    /**
     * 重写调用实际的响应体的contentLength
     * @return contentLength
     * @throws IOException 异常
     */
    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    /**
     * 重写进行写入
     * @param sink BufferedSink
     * @throws IOException 异常
     */
    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (bufferedSink == null) {
//            //包装
            bufferedSink = Okio.buffer(sink(sink));

        }
        //写入
        requestBody.writeTo(bufferedSink);
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink.flush();


    }

    /**
     * 写入，回调进度接口
     * @param sink Sink
     * @return Sink
     */
    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            //当前写入字节数
            long currentLength = 0L;
            //总字节长度，避免多次调用contentLength()方法
            long totalLength = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (totalLength == 0) {
                    //获得contentLength的值，后续不再调用
                    totalLength = contentLength();
                }
                //增加当前写入的字节数
                currentLength += byteCount;
                //回调
                Message msg = Message.obtain();
                msg.what = UPDATE;
                msg.obj = new ProgressModel(currentLength,totalLength,currentLength==totalLength);
                myHandler.sendMessage(msg);
            }
        };
    }
}