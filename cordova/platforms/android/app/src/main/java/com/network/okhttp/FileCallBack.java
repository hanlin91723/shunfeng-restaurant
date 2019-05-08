package com.network.okhttp;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
     * 用于文件上传和下载的进度监听
     */
  public interface FileCallBack extends Callback{
       void onProgress(long currentLength, long totalLength, boolean isCompleted);
    @Override
    void onFailure(Call call, IOException e);

    @Override
    void onResponse(Call call, Response response);
}
