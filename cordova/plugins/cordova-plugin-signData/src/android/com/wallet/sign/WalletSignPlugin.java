package com.wallet.sign;



import android.content.Intent;
import android.util.Log;


import com.xitech.utils.FileUtil;
import com.xitech.utils.GsonQuick;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.pkcs.PKCS10CertificationRequest;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;




import static android.app.Activity.RESULT_OK;

/**
 * =====================================
 * 作    者: 白小兵
 * 版    本：1.0.0
 * 创建日期：2018/1/12
 * 描    述：钱包插件的处理类
 * =====================================
 */

public class WalletSignPlugin extends CordovaPlugin {
    private CallbackContext callbackContext;
    private String userId;
    private String path;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext=callbackContext;
        if (action.equals("buildWallet")) {//创建钱包
            /**
             * 1.获取传入的钱包内容
             * 2.把钱包内容的JSON字符串转换成对应的实体类
             * 3.创建对应的钱包文件，并把钱包内容写入钱包文件中
             * 4.把钱包文件的路径存入文件中
             */
            //1.获取传入的钱包内容
            JSONObject jsonObject=args.getJSONObject(0);
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                        //2.把钱包内容的JSON字符串转换成对应的实体类
                        WalletBean walletBean= SignUtils.walletParsing(jsonObject);
                        //3.创建对应的钱包文件，并把钱包内容写入钱包文件中
                        Results results=SignUtils.createWalletJsonFile(walletBean,"xiwallet.json",false);
                        //4.把钱包文件的路径存入文件中
                        if (results.getIsResult()){
                            File file=new File(FileUtil.initPath("tempWalletPath")+File.separator+"path.json");
                            if(file!=null&&file.exists()){
                               String path=SignUtils.readJsonFile(file.getAbsolutePath());
                               List<String> listPath=GsonQuick.toList(path,String.class);
                               if(listPath!=null&&listPath.size()>0){
                                   listPath.add(results.getResultMsg());
                               }else{
                                   listPath=new ArrayList<>();
                                   listPath.add(results.getResultMsg());
                               }
                               SignUtils.createJsonFile(listPath,"path.json",false);
                            }else{
                                try {
                                    if(file.createNewFile()){
                                        List<String> list=new ArrayList<>();
                                        list.add(results.getResultMsg());
                                        SignUtils.createJsonFile(list,"path.json",false);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            callbackContext.success("钱包文件创建成功");
                        }else{
                            callbackContext.error(results.getResultMsg());
                        }
                }
            });
            return true;
        }else if(action.equals("changeWalletPsd")){//设置钱包密码
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
            JSONObject jsonObject=args.getJSONObject(0);
            String userid=jsonObject.getString("userid");
            String oldPsd=jsonObject.getString("oldPsd");
            String newPsd=jsonObject.getString("newPsd");
            if(SignUtils.isEmpty(userid)){
                callbackContext.error("用户id不能为空");
                return true;
            }
           /* if(SignUtils.isEmpty(oldPsd)){
                this.callbackContext.error("旧密码不能为空");
                return true;
            }*/
            if(SignUtils.isEmpty(newPsd)){
                callbackContext.error("新密码不能为空");
                return true;
            }
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    Results results1=checkWalletExistPath(userid);
                    if(!results1.getIsResult()){
                        callbackContext.error(results1.getResultMsg());
                    }
                    Results results=SignUtils.updatePassword(path,userid,oldPsd,newPsd);
                    if(results.getIsResult()){
                        callbackContext.success("密码修改成功");
                    }else{
                        callbackContext.error(results.getResultMsg());
                    }
                }
            });
            return true;
        }else if(action.equals("getwallet")){//获取钱包内容
            /**
             * 1.从本地钱包存储文件中获取钱包的文件的路径
             * 2.对钱包文件的userId check password进行校验
             * 3.如果校验成功获取钱包内容
             *
             */
            JSONObject jsonObject=args.getJSONObject(0);
            String userid=jsonObject.getString("userid");
            if(SignUtils.isEmpty(userid)){
                this.callbackContext.error("用户Id不能为空");
                return true;
            }
            Results results=checkWalletExistPath(userid);
            if(!results.getIsResult()){
                this.callbackContext.error(results.getResultMsg());
                return true;
            }
            if(SignUtils.isEmpty(path)){
                this.callbackContext.error("文件不存在,请重新创建钱包");
                return true;
            }
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    Results results=SignUtils.walletCheck(path,userid,null);
                    if(results.getIsResult()){
                        JSONObject json= null;
                        try {
                            json = new JSONObject(results.getResultMsg());
                            callbackContext.success(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error(e.getMessage());
                        }
                    }else{
                        callbackContext.error(results.getResultMsg());
                    }
                }
            });
            return true;
        }else if(action.equals("signToData")){//获取签名
            /**
             * 1.从本地钱包存储文件中获取钱包的文件的路径
             * 2.对钱包文件的check，userId和password进行校验
             * 3.如果校验成功获取钱包内容
             * 4.从钱包文件中获取的是加密后的私钥，并对私钥进行解密，拿到原始的私钥
             * 5.拿着私钥给传入的数据进行签名
             */
            JSONObject jsonObject=args.getJSONObject(0);
             String userid=jsonObject.getString("userid");
             String accountid=jsonObject.getString("accountid");
             String psd=jsonObject.getString("psd");
             String signdata=jsonObject.getString("signdata");
             //1.从本地钱包存储文件中获取钱包的文件的路径
            Results results=checkWalletExistPath(userid);
            if(!results.getIsResult()){
                this.callbackContext.error(results.getResultMsg());
                return true;
            }
            if (SignUtils.isEmpty(path)){
                this.callbackContext.error("钱包文件不存在");
                return true;
            }
             if(SignUtils.isEmpty(userid)){
                 this.callbackContext.error("用户Id不能为空");
                 return true;
             }
             if(SignUtils.isEmpty(accountid)){
                 this.callbackContext.error("钱包地址不能为空");
                 return true;
             }
             if(SignUtils.isEmpty(psd)){
                 this.callbackContext.error("用户密码不能为空");
                 return true;
             }
            if(SignUtils.isEmpty(signdata)){
                this.callbackContext.error("签名数据不能为空");
                return true;
            }
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    //2.对钱包文件的userId check password进行校验
                    Results results = SignUtils.walletCheck(path,userid,psd);
                    if (results.getIsResult()) {
                       // 3.如果校验成功获取钱包内容
                        WalletBean walletBean = GsonQuick.toObject(results.getResultMsg(), WalletBean.class);
                        List<WalletBean.CertBean> certBeans = walletBean.getCerts();
                        String key=null;
                        //4.从钱包文件中获取的是加密后的私钥，并对私钥进行解密，拿到原始的私钥
                        for (int i = 0; i < certBeans.size(); i++) {
                            if (accountid.equals(certBeans.get(i).getAccount_id())) {
                                key = certBeans.get(i).getKey();
                            }
                        }
                        String oldKey = SignUtils.decrypt(key,psd);
                        Log.d("oldKey", oldKey);
                        String newKey = oldKey.replace("\\n", "\n");
                        //5.拿着私钥给传入的数据进行签名
                        signToData(newKey, signdata);
                    } else {
                        callbackContext.error(results.getResultMsg());
                    }
                  }
               });
            return true;
        }else if("exportWallet".equals(action)){//导出钱包
            /**1.获取钱包的文件的存储路径
             * 2.校验钱包的check，userid和password
             * 3.如果校验通过，
             */
            JSONObject jsonObject=args.getJSONObject(0);
            String userid=jsonObject.getString("userid");
            String password=jsonObject.getString("password");
            Results results=checkWalletExistPath(userid);
            if(!results.getIsResult()){
                this.callbackContext.error(results.getResultMsg());
                return true;
            }
            if(SignUtils.isEmpty(path)){
                this.callbackContext.error("钱包文件不存在，请重新导入");
                return true;
            }
            if(SignUtils.isEmpty(userid)){
                this.callbackContext.error("用户Id不能为空");
                return true;
            }
            if(SignUtils.isEmpty(password)){
                this.callbackContext.error("钱包密码不能为空");
                return true;
            }
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    Results results=SignUtils.walletCheck(path,userid,password);
                    if(results.getIsResult()){
                        SignUtils.exportWalletFile(cordova.getActivity(),path);
                        callbackContext.success("分享钱包");
                    }else{
                        callbackContext.error(results.getResultMsg());
                    }
                }
            });
            return true;
        }else if("importWallet".equals(action)){//导入钱包
            JSONObject jsonObject=args.getJSONObject(0);
            userId=jsonObject.getString("userid");
            String password=jsonObject.getString("password");
            String walletPath=jsonObject.getString("wallpath");
            File file=new File(walletPath);
            if(SignUtils.isEmpty(walletPath)){
                this.callbackContext.error("钱包文件不存在");
                return true;
            }else if(!file.exists()){
                this.callbackContext.error("钱包文件不存在");
                return true;
            }
            if(SignUtils.isEmpty(userId)){
                this.callbackContext.error("用户Id不能为空");
                return true;
            }
            if(SignUtils.isEmpty(password)){
                this.callbackContext.error("钱包密码不能为空");
                return true;
            }
            Results results=SignUtils.walletCheck(walletPath,userId,password);
            if(results.getIsResult()){
                String newPath= FileUtil.initPath(SignUtils.getMD5(userId))+ File.separator+"xiwallet.json";
                Results results1=SignUtils.copyFile(walletPath,newPath);
                if(results1.getIsResult()){
                    File file1=new File(FileUtil.initPath("tempWalletPath")+File.separator+"path.json");
                    if(file1!=null&&file1.exists()){
                        String path=SignUtils.readJsonFile(file1.getAbsolutePath());
                        List<String> listPath=GsonQuick.toList(path,String.class);
                        if(listPath!=null&&listPath.size()>0){
                            listPath.add(results1.getResultMsg());
                        }else{
                            listPath=new ArrayList<>();
                            listPath.add(results1.getResultMsg());
                        }
                        SignUtils.createJsonFile(listPath,"path.json",false);
                    }else{
                        try {
                            if(file1.createNewFile()){
                                List<String> list=new ArrayList<>();
                                list.add(results1.getResultMsg());
                                SignUtils.createJsonFile(list,"path.json",false);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    JSONObject jsonObject1=new JSONObject(results.getResultMsg());
                    this.callbackContext.success(jsonObject1);
                    return true;
                }else{
                    this.callbackContext.error(results1.getResultMsg());
                    return true;
                }
            }else{
                this.callbackContext.error(results.getResultMsg());
                return true;
            }
        }else if("selectWallet".equals(action)){//选择钱包
            JSONObject jsonObject=args.getJSONObject(0);
            userId=jsonObject.getString("userid");
            if(SignUtils.isEmpty(userId)){
                this.callbackContext.error("用户Id不能为空");
                return true;
            }
            Intent intent = new Intent(cordova.getActivity().getApplicationContext(), SearchActivity.class);
            cordova.startActivityForResult((CordovaPlugin)this,intent, 101);
            return true;
        }else if("checkeWalletPassword".equals(action)){//检查钱包密码
            JSONObject jsonObject=args.getJSONObject(0);
            String userid=jsonObject.getString("userid");
            String password=jsonObject.getString("password");
            Results results=checkWalletExistPath(userid);
            if(!results.getIsResult()){
                this.callbackContext.error(results.getResultMsg());
                return true;
            }
            path=results.getResultMsg();
            if(SignUtils.isEmpty(userid)){
                this.callbackContext.error("用户Id不能为空");
                return true;
            }
            if(SignUtils.isEmpty(password)){
                this.callbackContext.error("钱包密码不能为空");
                return true;
            }
            cordova.getThreadPool().execute(new Runnable(){
                @Override
                public void run() {
                    Results results=SignUtils.walletCheck(path,userid,password);
                    if(results.getIsResult()){
                        callbackContext.success("钱包正确");
                    }else{
                        callbackContext.error(results.getResultMsg());
                    }
                }
            });
            return true;
        } else if ("getCSR".equals(action)) {
            JSONObject jsonObject=args.getJSONObject(0);
            String accountId=jsonObject.getString("accountId");
            Log.d("ccc",accountId);
            //构造csr
            try {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(512, new SecureRandom());
                KeyPair keyPair = keyGen.generateKeyPair();

                PKCS10CertificationRequest csr = CSRHelper.generateCSR(keyPair, "accoutId");
                String pem = CSRHelper.certificationRequestToPEM(csr);
                callbackContext.success(pem);//返回csr文件内容

                Log.d("ccc",pem);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (OperatorCreationException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

//            //构造csr
//            Map<String, String> csrMap = new HashMap<>();
//            csrMap.put("CN", accountId);
//            csrMap.put("C", "CN");
//            csrMap.put("ST", "beijing");
//            csrMap.put("O", "example.com");
//            csrMap.put("OU", "OU");
//            csrMap.put("L", "beijing");
//            try {
//                CsrFileAndPrivateKey csrFileAndPrivateKey = GenerateCSR.generateCSR(csrMap);
//                String csr = csrFileAndPrivateKey.getCsr();
//                callbackContext.success(csr);//返回csr文件内容
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            return true;

        } else if(action.equals("signString")){//对固定数据获取签名
            /**
             * 1.从本地钱包存储文件中获取钱包的文件的路径
             * 2.对钱包文件的check，userId和password进行校验
             * 3.如果校验成功获取钱包内容
             * 4.从钱包文件中获取的是加密后的私钥，并对私钥进行解密，拿到原始的私钥
             * 5.拿着私钥给传入的数据进行签名
             */
            JSONObject jsonObject=args.getJSONObject(0);
            String userid=jsonObject.getString("userid");
            String accountid=jsonObject.getString("accountid");
            String psd=jsonObject.getString("psd");
            String signdata=jsonObject.getString("signdata");
            //1.从本地钱包存储文件中获取钱包的文件的路径
            Results results=checkWalletExistPath(userid);
            if(!results.getIsResult()){
                this.callbackContext.error(results.getResultMsg());
                return true;
            }
            if (SignUtils.isEmpty(path)){
                this.callbackContext.error("钱包文件不存在");
                return true;
            }
            if(SignUtils.isEmpty(userid)){
                this.callbackContext.error("用户Id不能为空");
                return true;
            }
            if(SignUtils.isEmpty(accountid)){
                this.callbackContext.error("钱包地址不能为空");
                return true;
            }
            if(SignUtils.isEmpty(psd)){
                this.callbackContext.error("用户密码不能为空");
                return true;
            }
            if(SignUtils.isEmpty(signdata)){
                this.callbackContext.error("签名数据不能为空");
                return true;
            }
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    //2.对钱包文件的userId check password进行校验
                    Results results = SignUtils.walletCheck(path,userid,psd);
                    if (results.getIsResult()) {
                        // 3.如果校验成功获取钱包内容
                        WalletBean walletBean = GsonQuick.toObject(results.getResultMsg(), WalletBean.class);
                        List<WalletBean.CertBean> certBeans = walletBean.getCerts();
                        String key=null;
                        //4.从钱包文件中获取的是加密后的私钥，并对私钥进行解密，拿到原始的私钥
                        for (int i = 0; i < certBeans.size(); i++) {
                            if (accountid.equals(certBeans.get(i).getAccount_id())) {
                                key = certBeans.get(i).getKey();
                            }
                        }
                        String oldKey = SignUtils.decrypt(key,psd);
                        Log.d("oldKey", oldKey);
                        String newKey = oldKey.replace("\\n", "\n");
                        //5.拿着私钥给传入的数据进行签名
                        signToData(newKey, signdata);
                    } else {
                        callbackContext.error(results.getResultMsg());
                    }
                }
            });
            return true;
        } else if(action.equals("signCreateAccount")){//第一次创建钱包时的获取签名
            /**
             * 1.从本地钱包存储文件中获取钱包的文件的路径
             * 2.对钱包文件的check，userId和password进行校验
             * 3.如果校验成功获取钱包内容
             * 4.从钱包文件中获取的是加密后的私钥，并对私钥进行解密，拿到原始的私钥
             * 5.拿着私钥给传入的数据进行签名
             */
            JSONObject jsonObject=args.getJSONObject(0);
            String userid=jsonObject.getString("userid");
            String accountid=jsonObject.getString("accountid");
            String psd=jsonObject.getString("psd");
            String signdata=jsonObject.getString("signdata");
            //1.从本地钱包存储文件中获取钱包的文件的路径
            Results results=checkWalletExistPath(userid);
            if(!results.getIsResult()){
                this.callbackContext.error(results.getResultMsg());
                return true;
            }
            if (SignUtils.isEmpty(path)){
                this.callbackContext.error("钱包文件不存在");
                return true;
            }
            if(SignUtils.isEmpty(userid)){
                this.callbackContext.error("用户Id不能为空");
                return true;
            }
            if(SignUtils.isEmpty(accountid)){
                this.callbackContext.error("钱包地址不能为空");
                return true;
            }
            if(SignUtils.isEmpty(psd)){
                this.callbackContext.error("用户密码不能为空");
                return true;
            }
            if(SignUtils.isEmpty(signdata)){
                this.callbackContext.error("签名数据不能为空");
                return true;
            }
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    //2.对钱包文件的userId check password进行校验
                    Results results = SignUtils.walletCheck(path,userid,psd);
                    if (results.getIsResult()) {
                        // 3.如果校验成功获取钱包内容
                        WalletBean walletBean = GsonQuick.toObject(results.getResultMsg(), WalletBean.class);
                        List<WalletBean.CertBean> certBeans = walletBean.getCerts();
                        String key=null;
                        //4.从钱包文件中获取的是加密后的私钥，并对私钥进行解密，拿到原始的私钥
                        for (int i = 0; i < certBeans.size(); i++) {
                            if (accountid.equals(certBeans.get(i).getAccount_id())) {
                                key = certBeans.get(i).getKey();
                            }
                        }
                        String oldKey = SignUtils.decrypt(key,psd);
                        Log.d("oldKey", oldKey);
                        String newKey = oldKey.replace("\\n", "\n");
                        //5.拿着私钥给传入的数据进行签名
                        signToData(newKey, signdata);
                    } else {
                        callbackContext.error(results.getResultMsg());
                    }
                }
            });
            return true;
        }
        return false;
    }
    /**
     * 签名
     * @param signKey
     * @param dataToSign
     */
    private void signToData(String signKey,String dataToSign) {
            try {
                //获取私钥
                ECPrivateKey pk = (ECPrivateKey) EncryptedFileUtils.getPrivateKeyFromBytes(signKey);
                //进行Base64编码
                byte[] dattosign = Base64.decode(dataToSign);
                //对数据用私钥进行加密
                BigInteger sign[]= EncryptedFileUtils.ecdsaSignToBytes(pk, dattosign);
                JSONObject jsonObject=new JSONObject();
                //封装sign1到json对象
                jsonObject.put("sign1",String.valueOf(sign[0]));
                //封装sign2到json对象
                jsonObject.put("sign2",String.valueOf(sign[1]));
                callbackContext.success(jsonObject);
            }catch (InvalidKeySpecException e){
                callbackContext.error(e.getMessage());
            } catch (NoSuchProviderException e){
                callbackContext.error(e.getMessage());
            }catch (NoSuchAlgorithmException e){
                callbackContext.error(e.getMessage());
            } catch (CryptoException e){
                callbackContext.error(e.getMessage());
            } catch (IOException e){
                callbackContext.error(e.getMessage());
            } catch (JSONException e) {
                callbackContext.error(e.getMessage());
            }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode ==RESULT_OK) {
            if (intent != null && requestCode == 101) {
                String path = intent.getStringExtra("filePath");
                if(SignUtils.isEmpty(path)){
                    this.callbackContext.error("文件不存在,请重新选择钱包文件");
                }
                cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        Results results=SignUtils.walletCheck(path,userId,null);
                        if(results.getIsResult()){
                            callbackContext.success(path);
                        }else{
                            callbackContext.error(results.getResultMsg());
                        }
                    }
                });
            }
        }
    }

    /**
     * 用于检测钱包路径是否保存在在文件中
     * @param userId
     * @return
     */
    private Results checkWalletExistPath(String userId){
        Results results=new Results();
        File file=new File(FileUtil.initPath("tempWalletPath")+File.separator+"path.json");
        if(file!=null&&file.exists()){
            String paths=SignUtils.readJsonFile(file.getAbsolutePath());
            List<String> listPath=GsonQuick.toList(paths,String.class);
            if(listPath!=null&&listPath.size()>0){
                for (int i = 0; i <listPath.size() ; i++) {
                    if(listPath.get(i).contains(SignUtils.getMD5(userId))){
                        path=listPath.get(i);
                        results.setIsResult(true);
                        results.setResultMsg(path);
                        return results;
                    }else{
                        results.setIsResult(false);
                        results.setResultMsg("文件不存在,请重新创建钱包");
                    }
                }
            }else{
                results.setIsResult(false);
                results.setResultMsg("文件不存在,请重新创建钱包");
            }
        }else{
            results.setIsResult(false);
            results.setResultMsg("文件不存在,请重新创建钱包");
        }
        return results;
    }
}
