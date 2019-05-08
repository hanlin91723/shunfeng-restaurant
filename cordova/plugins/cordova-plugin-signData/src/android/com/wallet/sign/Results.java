package com.wallet.sign;

/**
 * =====================================
 * 作    者: 白小兵
 * 版    本：1.0.0
 * 创建日期：2018/1/12
 * 描    述：处理插件异常的信息返回
 * =====================================
 */

public class Results {
    private String resultMsg;//异常信息
    private boolean isResult;//是否成功
    private String  resultCode;

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public boolean getIsResult() {
        return isResult;
    }

    public void setIsResult(boolean isResult) {
        this.isResult = isResult;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public String toString() {
        return "Results{" +
                "resultMsg='" + resultMsg + '\'' +
                ", isResult=" + isResult +
                ", resultCode='" + resultCode + '\'' +
                '}';
    }
}
