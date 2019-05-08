package cordova.plugin.xitech;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.encoding.EncodingHandler;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class BarcodeScanner extends CordovaPlugin {
    private CallbackContext callbackContext;
    //打开扫描界面请求码
    private int REQUEST_CODE = 0x01;
    //扫描成功返回码
    private int RESULT_OK = 0xA1;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext=callbackContext;
        if (action.equals("scan")) {
            this.scan();
            return true;
        }else if(action.equals("encode")){
            decodeBarcode(args);
        }
        return false;
    }

    private void scan(){
        Intent intent = new Intent( cordova.getActivity(), CaptureActivity.class);
        cordova.getActivity().startActivityForResult(intent, REQUEST_CODE);
    }
    private  void decodeBarcode(JSONArray args){
        try {
            JSONObject param=args.getJSONObject(0);
            String type=param.getString("type");
            String data=param.getString("data");
            String logoUrl=param.getString("url");
            String options=param.getString("option");
            Bitmap bitmap= BitmapFactory.decodeFile(logoUrl);
            //根据输入的文本生成对应的二维码并且显示出来
            String path = EncodingHandler.createQRCode(data, 1000,1000,bitmap);
            if (!StringUtil.isEmpty(path)){
              callbackContext.success(path);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //扫描结果回调
        if (resultCode == RESULT_OK){
            Bundle bundle = intent.getExtras();
            String scanResult = bundle.getString("qr_scan_result");
            if(scanResult.equals("未发现二维码")){
                callbackContext.error(scanResult);
            }else{
                callbackContext.success(scanResult);
            }
        }
    }
}
