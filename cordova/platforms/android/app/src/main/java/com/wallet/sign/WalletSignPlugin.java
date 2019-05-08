package com.wallet.sign;


import android.content.Intent;
import android.util.Log;

import com.ninecm.utils.FileUtil;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.pkcs.PKCS10CertificationRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
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
   // private String userId;


    private final static String WALLET_NAME="onbwallet";
    private final static String WALLET_PREFIX="json";

    public static String genWalletName(String accoutId){
        return WALLET_NAME+"_"+accoutId+"."+WALLET_PREFIX;
    }

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

            Log.d("ccc","buildWallet");
            JSONObject jsonObject=args.getJSONObject(0);
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                        //2.把钱包内容的JSON字符串转换成对应的实体类
                        WalletParse walletParse= SignUtils.walletParsing(jsonObject);


                        //3.创建对应的钱包文件，并把钱包内容写入钱包文件中
                        Results results=SignUtils.createWalletJsonFile(walletParse.getUserId() ,walletParse.getWalletBean(),genWalletName(walletParse.getAccountId()),false);


                        //4.把钱包文件的路径存入文件中
                        if (results.getIsResult()){
//                            File file=new File(FileUtil.initPath("tempWalletPath")+File.separator+"path.json");
//                            if(file!=null&&file.exists()){
//                               String path=SignUtils.readJsonFile(file.getAbsolutePath());
//                               List<String> listPath=GsonQuick.toList(path,String.class);
//                               if(listPath!=null&&listPath.size()>0){
//                                   listPath.add(results.getResultMsg());
//                               }else{
//                                   listPath=new ArrayList<>();
//                                   listPath.add(results.getResultMsg());
//                               }
//                               SignUtils.createJsonFile(listPath,"path.json",false);
//                            }else{
//                                try {
//                                    if(file.createNewFile()){
//                                        List<String> list=new ArrayList<>();
//                                        list.add(results.getResultMsg());
//                                        SignUtils.createJsonFile(list,"path.json",false);
//                                    }
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }

                            JSONObject json= null;
                            try {
                                json = new JSONObject(results.getResultMsg());
                                json.put("plaintext",walletParse.getRealKey());
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
            String accountId=jsonObject.getString("accountId");
            if(SignUtils.isEmpty(userid)){
                callbackContext.error("用户id不能为空");
                return true;
            }
           /* if(SignUtils.isEmpty(oldPsd)){
                this.callbackContext.error("旧密码不能为空");
                return true;
            }*/
            if(SignUtils.isEmpty(accountId)){
                callbackContext.error("accoutId不能为空");
                return true;
            }

            if(SignUtils.isEmpty(newPsd)){
                callbackContext.error("新密码不能为空");
                return true;
            }
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    String walletPath=checkWalletExistPath(SignUtils.getMD5(userid),accountId);
                    if(walletPath.isEmpty()){
                        callbackContext.error("钱包文件不存在");
                    }
                    Map<String,Object> map=SignUtils.updatePassword(walletPath,userid,oldPsd,newPsd);
                    if((boolean)map.get("ret")){

						JSONObject rltt = new JSONObject();
                        try {
                            rltt.put("privateKey",(String)map.get("privateKey"));
                            rltt.put("plaintext", (String)map.get("plaintext"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        callbackContext.success(rltt);
                    }else{
                        callbackContext.error((String)map.get("msg"));
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


            List<String> listPath=checkWalletsExistPath(SignUtils.getMD5(userid));
            if(listPath.size()<=0){
                this.callbackContext.error("当前用户目录不存在钱包");
                return true;
            }

            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {

                    JSONArray jsonArray=new JSONArray();

                    for (String path: listPath) {

                        Results results=SignUtils.walletCheck(path,"",null);
                        if(results.getIsResult()){
                            JSONObject json= null;
                            try {
                                json = new JSONObject(results.getResultMsg());
                                json.put("plaintext","");

                                jsonArray.put(json);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                callbackContext.error(e.getMessage());
                            }
                        }else{
                            Log.d("ccc",results.getResultMsg());
                        }

                    }

                    if(jsonArray.length()>0){
                        callbackContext.success(jsonArray);
                    }else{
                        callbackContext.error("读取用户钱包全部失败");
                    }
                }

                //                @Override
//                public void run() {
//                    Results results=SignUtils.walletCheck(path,userid,null);
//                    if(results.getIsResult()){
//                        JSONObject json= null;
//                        try {
//                            json = new JSONObject(results.getResultMsg());
//                            callbackContext.success(json);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            callbackContext.error(e.getMessage());
//                        }
//                    }else{
//                        callbackContext.error(results.getResultMsg());
//                    }
//                }
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

            String accountId=jsonObject.getString("accountId");
            if(SignUtils.isEmpty(userid)){
                callbackContext.error("用户Id不能为空");
                return true;
            }
            if(SignUtils.isEmpty(password)){
                callbackContext.error("钱包密码不能为空");
                return true;
            }
            if(SignUtils.isEmpty(accountId)){
                callbackContext.error("accoutId不能为空");
                return true;
            }

            String walletPath=checkWalletExistPath(SignUtils.getMD5(userid),accountId);
            if(walletPath.isEmpty()){
                this.callbackContext.error("钱包文件不存在");
                return true;
            }

            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    Results results=SignUtils.walletCheck(walletPath,userid,password);
                    if(results.getIsResult()){
                        WalletBean walletBean= null;
                        String filename = "";
                        try {
                            JSONObject contentObject = new JSONObject(results.getResultMsg());
                            JSONArray certsArray = contentObject.getJSONArray("certs");
                            JSONObject accountId = certsArray.getJSONObject(0);
                            String accountIdStr = accountId.getString("account_id");
                            filename = genWalletName(accountIdStr.substring(accountIdStr.length() - 4)) ;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            String filePath = FileUtil.initTempPath();
                            File toFile = new File(filePath,File.separator+filename);
                            File fromFile = new File(walletPath);
                            FileInputStream ins = new FileInputStream(fromFile);
                            FileOutputStream out = new FileOutputStream(toFile);
                            byte[] b = new byte[1024];
                            int n=0;
                            while((n=ins.read(b))!=-1){
                                out.write(b, 0, n);
                            }
                            ins.close();
                            out.close();
                            SignUtils.exportWalletFile(cordova.getActivity(),toFile.getAbsolutePath());
                            callbackContext.success("分享钱包");

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

//                        Results results2 =SignUtils.createTempWalletJsonFile(walletBean,filename,false);

                    }else{
                        callbackContext.error(results.getResultMsg());
                    }
                }
            });
            return true;
        }else if("importWallet".equals(action)){//导入钱包
            JSONObject jsonObject=args.getJSONObject(0);
            String userId=jsonObject.getString("userid");
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

                WalletBean.CertBean bean=SignUtils.walletGetCert(walletPath);
                if(bean == null){
                    this.callbackContext.error("导入钱包文件读取错误");
                    return true;
                }

                String accoutId=bean.getAccount_id();

                String newPath= FileUtil.initPath(SignUtils.getMD5(userId))+ File.separator+genWalletName(accoutId);

                Results results1=SignUtils.copyFile(walletPath,newPath);

                if(results1.getIsResult()){
                    JSONObject jsonObject1=new JSONObject(results.getResultMsg());
                    jsonObject1.put("plaintext",results.getResultData());

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
            String userId=jsonObject.getString("userid");
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
            String accountId=jsonObject.getString("accountId");

            if(SignUtils.isEmpty(userid)){
                this.callbackContext.error("用户Id不能为空");
                return true;
            }
            if(SignUtils.isEmpty(password)){
                this.callbackContext.error("钱包密码不能为空");
                return true;
            }

            String walletPath=checkWalletExistPath(SignUtils.getMD5(userid),accountId);
            if(walletPath.isEmpty()){
                this.callbackContext.error("钱包不存在");
                return true;
            }

            cordova.getThreadPool().execute(new Runnable(){
                @Override
                public void run() {
                    Results results=SignUtils.walletCheck(walletPath,userid,password);
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
            //构造csr
            try {
                //Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime256v1");
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");

//                keyGen.initialize(1024);
                keyGen.initialize(ecSpec, new SecureRandom());
                KeyPair keyPair = keyGen.generateKeyPair();  //产生的私钥 是做过PKCS8的

                String privateKeyPem = CSRHelper.privateKeyToPEM(keyPair.getPrivate());
                String publicKeyPem = CSRHelper.publicKeyToPEM(keyPair.getPublic());


                JSONObject rlt=new JSONObject();
                rlt.put("privateKey", privateKeyPem);


                PKCS10CertificationRequest csr = CSRHelper.generateCSR(keyPair, accountId);
                String CSRPem = CSRHelper.certificationRequestToPEM(csr);

                rlt.put("csr", CSRPem);
                Log.d("ccc", rlt.toString());

                callbackContext.success(rlt);//返回csr文件内容和私钥

            } catch (IOException e) {
                e.printStackTrace();
            } catch (OperatorCreationException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }

            return true;

        } /*else if(action.equals("signToData")){//获取签名

            return true;
        }*/else if("signString".equals(action) || action.equals("signToData")){//用私钥对数据获取签名
            JSONObject jsonObject=args.getJSONObject(0);
            String password=jsonObject.getString("password");//钱包私钥密码
            String privateKey=jsonObject.getString("privateKey");//用作签名的原始私钥
//            String stringToSign = "BaoChain";
            String stringToSign=jsonObject.getString("stringToSign");//待签名字符串

            Log.d("ccc","signString:"+privateKey);
            Log.d("ccc","stringToSign:"+stringToSign);
            if(SignUtils.isEmpty(privateKey)){
                this.callbackContext.error("私钥不能为空");
                return true;
            }
            if(SignUtils.isEmpty(stringToSign)){
                this.callbackContext.error("签名数据不能为空");
                return true;
            }

            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    String privateKeyProcessed = "";
                    if(SignUtils.isEmpty(password)){
                        privateKeyProcessed = privateKey;
                    }else{
                        privateKeyProcessed = SignUtils.decrypt(privateKey, password);//解密私钥
                        if (privateKeyProcessed == null) {
                            callbackContext.error("密码验证错误");
                            return;
                        }
                    }

                    //
                    privateKeyProcessed = privateKeyProcessed.replace("\\n", "\n");
                    signToData2(privateKeyProcessed, stringToSign);
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
//                ECPrivateKey pk = (ECPrivateKey) EncryptedFileUtils.getPrivateKeyFromBytes(signKey);
                ECPrivateKey pk = (ECPrivateKey) CSRHelper.pemToPrivateKey(signKey, "EC");
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
                Log.d("ccc", jsonObject.toString());

            }catch (InvalidKeySpecException e){
                callbackContext.error(e.getMessage());
            } catch (NoSuchAlgorithmException e){
                callbackContext.error(e.getMessage());
            } catch (CryptoException e){
                callbackContext.error(e.getMessage());
            } catch (JSONException e) {
                callbackContext.error(e.getMessage());
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            }
    }

    /**
     * 签名
     * @param signKey
     * @param dataToSign
     */
    private void signToData2(String signKey,String dataToSign) {
        try {
            ECPrivateKey pk = (ECPrivateKey) CSRHelper.pemToPrivateKey(signKey, "EC");
            //进行Base64编码
            byte[] dattosign = Base64.decode(dataToSign);
            //对数据用私钥进行加密
            BigInteger sign[]= EncryptedFileUtils.ecdsaSignToBytes(pk, dattosign);
            JSONObject jsonObject=new JSONObject();
            //封装sign1到json对象
            jsonObject.put("sign1",String.valueOf(sign[0]));
            //封装sign2到json对象
            jsonObject.put("sign2",String.valueOf(sign[1]));

            jsonObject.put("plaintext",signKey);
            callbackContext.success(jsonObject);
            Log.d("ccc", jsonObject.toString());

        }catch (InvalidKeySpecException e){
            callbackContext.error("不能正确解析私钥");
        } catch (NoSuchAlgorithmException e){
            callbackContext.error(e.getMessage());
        } catch (CryptoException e){
            callbackContext.error(e.getMessage());
        } catch (JSONException e) {
            callbackContext.error(e.getMessage());
        } catch (NoSuchProviderException e) {
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
                        Results results=SignUtils.walletCheck(path,"",null);
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



    private String checkWalletExistPath(String userId,String accoutId){
        Results results=new Results();
        File file = new File(FileUtil.initPath(userId),genWalletName(accoutId));

        if(file!=null&&file.exists()){
            return file.getAbsolutePath();
        }
        return "";
    }

    private List<String> checkWalletsExistPath(String userId){

        File dir = new File(FileUtil.initPath(userId));
        List<String> listPath=new ArrayList<>();

        FilenameFilter filter=new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {

                //dir为扫描的文件的上级文件
                //filename为当前扫描到的文件名
                File file = new File(dir, filename);

                if(file.isFile() && filename.toUpperCase().startsWith("ONBWALLET_") && filename.toUpperCase().endsWith(".JSON"))  //不返回 . 和 ..开始的文件
                {
                    return true;
                }


                return false;
            }
        };


        if(dir!=null&&dir.exists()) {

            File files[] = dir.listFiles(filter);
            for(File f:files){
                listPath.add(f.getAbsolutePath());
            }
        }

        return listPath;

    }
}
