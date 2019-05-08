package com.wallet.sign;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.widget.Toast;

import com.xitech.utils.FileUtil;
import com.xitech.utils.GsonQuick;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StreamCorruptedException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
/**
 * =====================================
 * 作    者: 白小兵
 * 版    本：1.0.0
 * 创建日期：2018/1/12
 * 描    述：处理插件异常的信息返回
 * =====================================
 */
public class SignUtils {
    // 算法名称
    final static String KEY_ALGORITHM = "AES";
    // 加解密算法/模式/填充方式
    final static String algorithmStr = "AES/CBC/PKCS7Padding";
    static byte[] iv = {0x31,0x31,0x32,0x32,0x33,0x33,0x34,0x34,0x35,0x35,0x36,0x36,0x37,0x37,0x38,0x38};
    public static WalletBean walletParsing(JSONObject jsonObject){
       try {
           WalletBean walletBean=new WalletBean();
           walletBean.setName("我的钱包");
           walletBean.setUserid(getMD5(jsonObject.getString("userId")));
           WalletBean.CertBean certBean=new WalletBean.CertBean();
           certBean.setAccount_id(jsonObject.getString("account_id"));
           walletBean.setPasswd("");
           certBean.setCert(jsonObject.getString("userCert"));
           certBean.setKey(jsonObject.getString("userKey"));
           certBean.setSerial_num(jsonObject.getString("serialNumber"));
           List<WalletBean.CertBean> certBeans=new ArrayList<>();
           certBeans.add(certBean);
           walletBean.setCerts(certBeans);
           StringBuilder stringBuilder=new StringBuilder();
           stringBuilder.append(walletBean.getUserid());
           stringBuilder.append(certBean.getAccount_id());
           stringBuilder.append(certBean.getSerial_num());
           stringBuilder.append(certBean.getCert());
           stringBuilder.append(certBean.getKey());
           String check=getMD5(stringBuilder.toString());
           walletBean.setCheck(check);
           return walletBean;
       } catch (JSONException e) {
           e.printStackTrace();
       }
      return null;
   }
    /**
     * 存储Json文件
     *
     *
     * @param walletBean
     *            json字符串
     * @param fileName
     *            存储的文件名
     * @param append
     *            true 增加到文件末，false则覆盖掉原来的文件
     */
    public static Results createWalletJsonFile(WalletBean walletBean, String fileName, boolean append) {
        //由于存储封装异常和正常返回信息
        Results results=new Results();
        //创建钱包文件
        File ff = new File(FileUtil.initPath(walletBean.getUserid()),File.separator+fileName);
        //把钱包文件字符串转换成对应的实体类
        String json= GsonQuick.toJsonFromBean(walletBean);
        FileOutputStream fos = null;
        BufferedWriter rd=null;
        try {
            boolean boo = ff.exists();
            //创建文件输入流
            fos = new FileOutputStream(ff, append);
            //数据写入缓冲流
            rd= new BufferedWriter(new OutputStreamWriter(fos,"utf-8"));
            //判断写入时是否要覆盖原文件内容并且判断文件是否已经存在，如果需要覆盖并且存在，则写入
            if (append && boo) {
                FileChannel fc = fos.getChannel();//此文件输出流关联的唯一文件通道对象
                fc.truncate(fc.position() - 4);
            }
            rd.write(new String(json.getBytes("utf-8")));//把Json数据写入到文件中去
            results.setIsResult(true);
            results.setResultMsg(ff.getAbsolutePath());
        } catch (FileNotFoundException e) {
            results.setIsResult(false);
            results.setResultMsg(e.getMessage());
        } catch (IOException e) {
            results.setIsResult(false);
            results.setResultMsg(e.getMessage());
        } finally {//最后记得关闭输入和输出流，防止内存泄露
            if (rd != null) {

                try {
                    rd.close();
                } catch (IOException e) {
                    results.setIsResult(false);
                    results.setResultMsg(e.getMessage());
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    results.setIsResult(false);
                    results.setResultMsg(e.getMessage());
                }
            }
        }
        return  results;
    }

    /**
     * 把钱包文件的访问地址存入文件
     * @param list
     * @param fileName
     * @param append
     * @return
     */
    public static Results createJsonFile(List<String> list,String fileName, boolean append) {
        Results results=new Results();
        File ff = new File(FileUtil.initPath("tempWalletPath"),File.separator+fileName);
        String json= GsonQuick.toJsonFromList(list);
        FileOutputStream fos = null;
        BufferedWriter rd=null;
        try {
            boolean boo = ff.exists();
            fos = new FileOutputStream(ff, append);
            rd= new BufferedWriter(new OutputStreamWriter(fos,"utf-8"));
            if (append && boo) {
                FileChannel fc = fos.getChannel();
                fc.truncate(fc.position() - 4);
            }
            rd.write(new String(json.getBytes("utf-8")));
            results.setIsResult(true);
            results.setResultMsg(ff.getAbsolutePath());
        } catch (FileNotFoundException e) {
            results.setIsResult(false);
            results.setResultMsg(e.getMessage());
        } catch (IOException e) {
            results.setIsResult(false);
            results.setResultMsg(e.getMessage());
        } finally {
            if (rd != null) {

                try {
                    rd.close();
                } catch (IOException e) {
                    results.setIsResult(false);
                    results.setResultMsg(e.getMessage());
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    results.setIsResult(false);
                    results.setResultMsg(e.getMessage());
                }
            }
        }
        return  results;
    }
    /**
     * 读取json数据
     *
     *
     * @param filePath
     * @return 返回值为list
     */
    public static String readWalletJsonFile(String filePath) {
        FileInputStream fis = null;
        BufferedReader br=null;
        StringBuilder stringBuffer=new StringBuilder();
        File des = new File(filePath);
        if(des.exists()){
            try {
                fis = new FileInputStream(des);
                br=new BufferedReader(new InputStreamReader(fis,"utf-8"));
                String str=null;
                while ((str = br.readLine())!=null){
                    stringBuffer.append(str);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return  stringBuffer.toString();
        }else{
            return null;
        }
    }
    /**
     * 读取存放钱包文件的地址的Json数据
     *
     *
     * @param filePath
     * @return 返回值为list
     */
    public static String readJsonFile(String filePath) {
        FileInputStream fis = null;
        BufferedReader br=null;
        StringBuilder stringBuffer=new StringBuilder();
        File des = new File(filePath);
        if(des.exists()){
            try {
                fis = new FileInputStream(des);
                br=new BufferedReader(new InputStreamReader(fis,"utf-8"));
                String str=null;
                while ((str = br.readLine())!=null){
                    stringBuffer.append(str);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return  stringBuffer.toString();
        }else{
            return null;
        }
    }

    /**
     * 更新密码
     * @param path
     * @param userId
     * @param oldPasswd
     * @param newPasswd
     * @return
     */
    public static Results updatePassword(String path,String userId,String oldPasswd,String newPasswd){
        /**
         * 1.读取钱包文件
         * 2.比对用户id是否一致
         * 3.比对check的值
         * 4.校验新旧密码
         * 5.更新密码
         * 6.更新key
         * 7.更新check
         * 8.更新钱包文件
         */
        Results results=new Results();
        String str=readWalletJsonFile(path);
        WalletBean walletBean=GsonQuick.toObject(str,WalletBean.class);
        if(!getMD5(userId).equals(walletBean.getUserid())){
            results.setResultMsg("用户id校验失败");
            results.setIsResult(false);
            return results;
        }
        walletBean.setUserid(getMD5(userId));
        String newCheck=generateCheck(walletBean);
        if(!newCheck.equals(walletBean.getCheck())){
            results.setResultMsg("check校验失败");
            results.setIsResult(false);
            return results;
        }
        if("".equals(walletBean.getPasswd())){//没有旧密码
            walletBean.setPasswd(getMD5(newPasswd));
            List<WalletBean.CertBean> certBeans=walletBean.getCerts();
            for (int i = 0; i <certBeans.size(); i++) {
                String oldEncryptionValue=certBeans.get(i).getKey();//原始值
                try {
                    certBeans.get(i).setKey(encrypt(oldEncryptionValue,newPasswd));//用新密码对原始值进行加密
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else{
            if(isEmpty(oldPasswd)){
                results.setResultMsg("旧密码不能为空");
                results.setIsResult(false);
                return results;
            }else if(!getMD5(oldPasswd).equals(walletBean.getPasswd())){
                results.setResultMsg("旧密码不一致");
                results.setIsResult(false);
                return results;
            }else if(isEmpty(newPasswd)){
                results.setResultMsg("新密码不能为空");
                results.setIsResult(false);
                return results;
            }else{
                walletBean.setPasswd(getMD5(newPasswd));
                encryptionKey(walletBean.getCerts(),oldPasswd,newPasswd);
            }

        }
        String check=generateCheck(walletBean);
        walletBean.setCheck(check);
        results=createWalletJsonFile(walletBean,"xiwallet.json",false);
        if(results.getIsResult()){
            results.setResultMsg("密码修改成功");
        }
        return results;
    }

    /**
     * 生成check
     * @param walletBean
     * @return
     */
    public static String generateCheck(WalletBean walletBean){
        StringBuilder str=new StringBuilder();
        if(!isEmpty(walletBean.getPasswd())){
            str.append(walletBean.getPasswd());
        }
        str.append(walletBean.getUserid());
        List<WalletBean.CertBean> certBeans=walletBean.getCerts();
        for (int i = 0; i <certBeans.size(); i++) {
            str.append(certBeans.get(i).getAccount_id());
            str.append(certBeans.get(i).getSerial_num());
            str.append(certBeans.get(i).getCert());
            str.append(certBeans.get(i).getKey());

        }
        return getMD5(str.toString());
    }

    /**
     * 校验钱包的正确性
     * @param filePath
     * @return
     */
    public static Results walletCheck(String filePath,String userId,String passwd){
        Results results=new Results();
        String walletContent=readWalletJsonFile(filePath);
        if(SignUtils.isEmpty(walletContent)){
            results.setResultMsg("钱包内容为空");
            results.setIsResult(false);
            return results;
        }
        WalletBean walletBean=GsonQuick.toObject(walletContent,WalletBean.class);
        String newCheck=generateCheck(walletBean);
        if(!newCheck.equals(walletBean.getCheck())){
            results.setResultMsg("check校验失败");
            results.setIsResult(false);
            return results;
        }
        if(!getMD5(userId).equals(walletBean.getUserid())){
            results.setResultMsg("用户Id不匹配");
            results.setIsResult(false);
            return results;
        }
        if(passwd!=null) {
            if (!getMD5(passwd).equals(walletBean.getPasswd())) {
                results.setResultMsg("钱包密码错误");
                results.setIsResult(false);
                return results;
            }
        }
        results.setResultMsg(walletContent);
        results.setIsResult(true);
        return results;
    }

    /**
     * 加密key
     * @param certBeans
     * @return
     */
    public  static void encryptionKey( List<WalletBean.CertBean> certBeans,String oldPasswd,String newPasswd){
        for (int i = 0; i <certBeans.size(); i++) {
            String oldEncryptionValue=certBeans.get(i).getKey();//旧的加密后私钥
            try {
                String origValue = decrypt(oldEncryptionValue,oldPasswd);//获取私钥的原始值
                String newEncryptionValue=encrypt(origValue,newPasswd);//新加密后的私钥
                certBeans.get(i).setKey(newEncryptionValue);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    /**
     *
     * 功能:MD5加密
     * @param s
     */

    public static String getMD5(String s) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str).toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static byte[] toMakekey(byte[] keyBytes){
        // 如果密钥不足16位，那么就补足.  这个if 中的内容很重要
        int base = 16;
        if (keyBytes.length % base != 0) {
            int groups = keyBytes.length / base + (keyBytes.length % base != 0 ? 1 : 0);
            byte[] temp = new byte[groups * base];
            Arrays.fill(temp, (byte) 0);
            System.arraycopy(keyBytes, 0, temp, 0, keyBytes.length);
            keyBytes = temp;
        }
        return keyBytes;
    }
    /**
     * 加密方法
     *
     * @param content
     *            要加密的字符串
     * @param keyBytes
     *            加密密钥
     * @return
     */
    public static String encrypt(String content,String keyBytes) {
        byte[] encryptedText = null;
        String strHex=null;
        content=content.replace("\\n","\n");
        try {
            // 初始化
            Security.addProvider(new BouncyCastleProvider());
            // 转化成JAVA的密钥格式
            SecretKeySpec key = new SecretKeySpec(toMakekey(keyBytes.getBytes("utf-8")), KEY_ALGORITHM);
            // 初始化cipher
            Cipher cipher = Cipher.getInstance(algorithmStr, "BC");
            //String strIv="1122334455667788";
            cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(iv));
            encryptedText = cipher.doFinal(content.getBytes());
            strHex= parseByte2HexStr(encryptedText);
            return Base64.encodeToString(strHex.getBytes("utf-8"),Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 解密方法
     *
     * @param encryptedData
     *            要解密的字符串
     * @param keyBytes
     *            解密密钥
     * @return
     */
    public static String decrypt(String encryptedData, String keyBytes) {
       byte[] encryptedText = null;
       byte[] bytesHex= Base64.decode(encryptedData,Base64.NO_WRAP);
        try {
            // 初始化
            Security.addProvider(new BouncyCastleProvider());
            // 转化成JAVA的密钥格式
            SecretKeySpec key = new SecretKeySpec(toMakekey(keyBytes.getBytes("utf-8")), KEY_ALGORITHM);
            // 初始化cipher
            Cipher cipher = Cipher.getInstance(algorithmStr, "BC");
            //String strIv="1122334455667788";
            cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(iv));
            encryptedText = cipher.doFinal(parseHexStr2Byte(new String(bytesHex)));
           return new String(encryptedText,"utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 导出钱包文件
     * @param activity
     * @param path
     */
    public static void exportWalletFile(Activity activity, String path){
        // 获取文件的 url
        File shareFile = new File(path);
        Uri fileUri=null;
        if(!shareFile.exists()||!shareFile.isFile()){
            Toast.makeText(activity,"钱包文件件不存在",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        // 系统版本大于N的统一用FileProvider处理
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            // 将文件转换成content://Uri的形式
            fileUri= FileProvider.getUriForFile(activity, activity.getPackageName()+ ".provider",shareFile);
            // 申请临时访问权限
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }else{
            fileUri = Uri.fromFile(shareFile);
        }
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM,fileUri);
        activity.startActivity(intent);
    }
    /**
     * 复制单个文件
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static Results copyFile(String oldPath, String newPath) {
        boolean append=false;
        Results results=new Results();
        File ff = new File(newPath);
        String str=readWalletJsonFile(oldPath);
        FileOutputStream fos = null;
        BufferedWriter rd=null;
        try {
            boolean boo = ff.exists();
            fos = new FileOutputStream(ff, append);
            rd= new BufferedWriter(new OutputStreamWriter(fos,"utf-8"));
            if (append && boo) {
                FileChannel fc = fos.getChannel();
                fc.truncate(fc.position() - 4);
            }
            rd.write(new String(str.getBytes("utf-8")));
            results.setIsResult(true);
            results.setResultMsg(ff.getAbsolutePath());
        } catch (FileNotFoundException e) {
            results.setIsResult(false);
            results.setResultMsg(e.getMessage());
        } catch (IOException e) {
            results.setIsResult(false);
            results.setResultMsg(e.getMessage());
        } finally {
            if (rd != null) {

                try {
                    rd.close();
                } catch (IOException e) {
                    results.setIsResult(false);
                    results.setResultMsg(e.getMessage());
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    results.setIsResult(false);
                    results.setResultMsg(e.getMessage());
                }
            }
        }
        return  results;
    }
    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toLowerCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }
    /**
     * 把16进制转化为字节数组
     * @param hexString
     * @return
     */
    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }


    /**
     * 二进制转字符,转成了16进制
     * 0123456789abcdefg
     * @param buf
     * @return
     */
    private final static String HEX = "0123456789abcdef";
    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }
    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }
    /**
     * 判断字符串是否为null或长度为0
     *
     * @param s 待校验字符串
     * @return {@code true}: 空<br> {@code false}: 不为空
     */
    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }
}
