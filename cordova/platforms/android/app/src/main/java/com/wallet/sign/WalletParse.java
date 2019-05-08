package com.wallet.sign;

/**
 * Created by ccc on 2018/10/28.
 */

public class WalletParse {
    private WalletBean walletBean;
    private String accountId;
    private String realKey;
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getRealKey() {
        return realKey;
    }

    public void setRealKey(String realKey) {
        this.realKey = realKey;
    }

    public WalletBean getWalletBean() {

        return walletBean;
    }

    public void setWalletBean(WalletBean walletBean) {
        this.walletBean = walletBean;
    }
}
