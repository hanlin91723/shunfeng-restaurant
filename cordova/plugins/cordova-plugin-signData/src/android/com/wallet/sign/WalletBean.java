package com.wallet.sign;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * =====================================
 * 作    者: 白小兵
 * 版    本：1.0.0
 * 创建日期：2018/1/12
 * 描    述：钱包文件信息的工具类
 * =====================================
 */

public class WalletBean {
private String name;//钱包名称
    private String passwd;//钱包密码
    private String check;//钱包校验
    private String userid;//用户id

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    @Override
    public String toString() {
        return "WalletBean{" +
                "name='" + name + '\'' +
                ", passwd='" + passwd + '\'' +
                ", check='" + check + '\'' +
                ", userid='" + userid + '\'' +
                ", certs=" + certs +
                '}';
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public List<CertBean> getCerts() {
        return certs;
    }

    public void setCerts(List<CertBean> certs) {
        this.certs = certs;
    }

    private List<CertBean> certs;//钱包账户数组

    public static class CertBean implements Comparable<CertBean> {
        private String account_id;//账号
        private String serial_num;//序列号
        private String cert;//证书
        private String key;//私钥

        public String getAccount_id() {
            return account_id;
        }

        public void setAccount_id(String account_id) {
            this.account_id = account_id;
        }

        public String getSerial_num() {
            return serial_num;
        }

        public void setSerial_num(String serial_num) {
            this.serial_num = serial_num;
        }

        public String getCert() {
            return cert;
        }

        public void setCert(String cert) {
            this.cert = cert;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
        @Override
        public String toString(){
            return "CertBean{" +
                    "account_id='" + account_id + '\'' +
                    ", serial_num='" + serial_num + '\'' +
                    ", cert='" + cert + '\'' +
                    ", key='" + key + '\'' +
                    '}';
        }
        @Override
        public int compareTo(@NonNull CertBean certBean){
            if(certBean==null){
                throw  new NullPointerException();
            }
            return this.serial_num.compareTo(certBean.serial_num);
        }
    }
}
