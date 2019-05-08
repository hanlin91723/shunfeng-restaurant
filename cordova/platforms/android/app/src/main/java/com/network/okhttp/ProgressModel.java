package com.network.okhttp;


/**
 * =====================================
 * 作    者: 白小兵
 * 版    本：1.0.0
 * 创建日期：2016/1/12
 * 描    述：用于封装进度跳到实体类
 * =====================================
 */
public class ProgressModel {
    private long currentLength;//当前进度
    private long totalLength;//总长度
    private boolean isCompleted;//是否下载完成

    public ProgressModel(long currentLength, long totalLength, boolean isCompleted) {
        this.currentLength = currentLength;
        this.totalLength = totalLength;
        this.isCompleted = isCompleted;
    }

    public long getCurrentLength() {
        return currentLength;
    }

    public void setCurrentLength(long currentLength) {
        this.currentLength = currentLength;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
