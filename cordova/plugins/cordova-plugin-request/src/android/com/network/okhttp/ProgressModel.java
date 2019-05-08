package com.network.okhttp;

/**
 * Created by Administrator on 2018/1/29.
 */

public class ProgressModel {
    private long currentLength;
    private long totalLength;
    private boolean isCompleted;

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
