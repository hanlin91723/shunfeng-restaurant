package com.cjt2325.cameralibrary;


import android.content.Intent;
import android.graphics.Bitmap;

import android.os.Bundle;
import android.os.Environment;

import android.support.v7.app.AppCompatActivity;

import android.view.WindowManager;


import com.cjt2325.cameralibrary.listener.JCameraListener;
import com.xitech.xiapp.R;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Random;


public class CameraActivity extends AppCompatActivity{
    public final static int RESULT_CODE=1234; //自定义结果码
    public final static String EXTRA_RESULT_URL="extra_result_url";
    private JCameraView jCameraView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        jCameraView = (JCameraView) findViewById(R.id.jcameraview);
        jCameraView.setFeatures(JCameraView.BUTTON_STATE_ONLY_CAPTURE);//设置只能拍照
        //设置视频保存路径
        jCameraView.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "JCamera");
        jCameraView.setCameraActivity(this);
        //JCameraView监听
        jCameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap){
                String rootPath=Environment.getExternalStorageDirectory().getPath()+File.separator+"JCamera";
                File file1 = new File(rootPath);
                if (!file1.exists()) {
                    file1.mkdir();
                }
                String url=rootPath+File.separator+new Random().nextInt(10000) + ".jpg";
                //String url=;
                Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
                int quality = 100;
                OutputStream stream = null;
                try {
                    stream = new FileOutputStream(url);
                    bitmap.compress(format, quality, stream);
                    File headFile = new File(url);
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_RESULT_URL,headFile.getAbsolutePath());
                    setResult(RESULT_CODE, intent);
                    finish();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {

            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        jCameraView.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        jCameraView.onPause();
    }
}
