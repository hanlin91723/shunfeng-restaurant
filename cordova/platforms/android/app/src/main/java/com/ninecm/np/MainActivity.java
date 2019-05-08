/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.ninecm.np;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.webkit.MimeTypeMap;

import com.runtimepermissions.PermissionsManager;
import com.wallet.sign.MediaScanner;

import org.apache.cordova.CordovaActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends CordovaActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //scanDir(this,"/storage/emulated/0");
        // enable Cordova apps to be started in the background
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }

        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);

        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            try {
//                String[] permissions = new String[]{};
                List<String> permissions = new ArrayList<>();
                //检测是否有写的权限
                if(ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                {
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                if(ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
                {
                    permissions.add(Manifest.permission.CAMERA);
                }

                if (permissions.size()>0) {
                    String[] permissionsArray = permissions.toArray(new String[permissions.size()]);
                    // 没有写的权限，去申请写的权限，会弹出对话框
                    ActivityCompat.requestPermissions(this, permissionsArray, 0x01);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//
//            try {
//                //检测是否有写的权限
//                int permission = ActivityCompat.checkSelfPermission(this,
//                        Manifest.permission.CAMERA);
//                if (permission != PackageManager.PERMISSION_GRANTED) {
//                    // 没有写的权限，去申请写的权限，会弹出对话框
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0x02);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }


        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //scanDir(this,"/storage/emulated/0");
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }

    private void keepFontSize(){
        Resources res = getResources();
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    //
    /**
     * 保存后用广播扫描，Android4.4以下使用这个方法
     * @author YOLANDA
     */
    private void scanByBroadcast(Context ctx, String filePath){
        //String filePath="/storage/emulated/0/tencent";
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath)));
//        Toast.makeText(ctx, "保存成功：" + filePath, Toast.LENGTH_LONG).show();
    }

    /**
     * 保存后用MediaScanner扫描，通用的方法
     * @author YOLANDA
     */
    private void scanByMediaScannerConnection(Context ctx,String filePath){
        //String filePath ="/storage/emulated/0/tencent" ;
        MediaScanner mediaScanner = new MediaScanner(ctx);
        String[] filePaths = new String[]{filePath};
        String mime= MimeTypeMap.getSingleton().getMimeTypeFromExtension("json");
        String[] mimeTypes = new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension("json")};
        mediaScanner.scanFiles(filePaths, mimeTypes);
//        Toast.makeText(this, "保存成功：" + filePath, Toast.LENGTH_LONG).show();
    }



    public void scanDir(Context ctx, String dir) {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            scanByBroadcast(ctx,dir);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            scanByMediaScannerConnection(ctx,dir);
        }

    }
}
