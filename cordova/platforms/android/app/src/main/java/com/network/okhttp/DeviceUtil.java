package com.network.okhttp;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * =====================================
 * 作    者: 白小兵
 * 版    本：1.0.0
 * 创建日期：2016/1/12
 * 描    述：获取设备信息工具类
 * =====================================
 */

public class DeviceUtil {
    /**
     * 获取当前程序的版本号
     */
    public static int getVersionCode(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获取当前程序的版本名称
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    /**
     * 获取sd卡剩余空间大小
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getSdcardAvailableSpace() {

        if (!isSdcardAvailable()){
            return 0;
        }

        File pathFile = Environment.getExternalStorageDirectory();

        StatFs statFs = new StatFs(pathFile.getPath());
        long availCount = 0;
        long size=0;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
            availCount = statFs.getAvailableBlocksLong();
            size= statFs.getBlockCountLong();
        }else {
            availCount = statFs.getAvailableBlocks();
            size = statFs.getBlockCount();
        }
        return size * availCount ;
    }
    /**
     * 检查sd是否可用
     *
     * @return
     */
    public static boolean isSdcardAvailable() {
        boolean exist = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        return exist;
    }

    /**
     * 获取屏幕密度
     *
     * @param ctx
     * @return
     */
    public static float getDisplayDensity(Context ctx) {
        float desity = ctx.getResources().getDisplayMetrics().density;
        return desity;
    }


    /**
     * 得到设备名字
     * */
    public static String getDeviceName() {
        String model = Build.MODEL;
        if (model == null || model.length() <= 0) {
            return "";
        } else {
            return model;
        }
    }

    /**
     * 得到品牌名字
     * */
    public static String getBrandName() {
        String brand = Build.BRAND;
        if (brand == null || brand.length() <= 0) {
            return "";
        } else {
            return brand;
        }
    }
    /**
     * 获得手机厂商信息
     * */
    public static String getManufacturer() {
        String brand = Build.MANUFACTURER;
        if (brand == null || brand.length() <= 0) {
            return "";
        } else {
            return brand;
        }
    }

    /**
     * 得到操作系统版本号
     */
    public static String getOSVersionName() {
        return Build.VERSION.RELEASE;
    }
    /**
     * 获取android系统的版本
     *
     * @return
     */
    public static int getAndroidVersion() {
        return Build.VERSION.SDK_INT;
    }
    /**
     * 获取移动设备国际识别码
     *
     * @param context
     * @return
     */
    public static String getIMEI(Context context) {
        if (context == null) {
            return "";
        }
        try {
            String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            if (null == deviceId || deviceId.length() <= 0) {
                return "";
            } else {
                return deviceId.replace(" ", "");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            return "";
        }
    }


    // 设备唯一标识
    public static String getDeviceId(Context context) {
        String deviceId = "";
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return deviceId;
    }


    /**
     * 获取MAC地址
     *
     * @param context
     * @return
     */
    public static String getMacAddress(Context context) {
        if (context == null) {
            return "";
        }
        try {
            String macAddress = null;
            WifiInfo wifiInfo = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
            macAddress = wifiInfo.getMacAddress();
            if (macAddress == null || macAddress.length() <= 0) {
                return "";
            } else {
                return macAddress;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取分辨率 按xxx_xxx格式输出
     *
     * @param context
     * @return
     */
    public static String getResolution(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return new StringBuilder().append(dm.widthPixels).append("*").append(dm.heightPixels).toString();
    }
    /***
     * 功能:获取当前位置
     * @param context
     */
    public static String getLonLat(Context context) {
        double latitude = 0.0;
        double longitude = 0.0;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    return longitude + ","+latitude;
                }
            } else {
                LocationListener locationListener = new LocationListener() {

                    // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    // Provider被enable时触发此函数，比如GPS被打开
                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    // Provider被disable时触发此函数，比如GPS被关闭
                    @Override
                    public void onProviderDisabled(String provider) {

                    }

                    // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            Log.e("Map", "Location changed : Lat: " + location.getLatitude() + " Lng: " + location.getLongitude());
                        }
                    }
                };
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude(); // 经度
                    longitude = location.getLongitude(); // 纬度
                    return longitude + ","+latitude;
                }
            }
        }catch (SecurityException e){
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 获取SIM卡运营商
     *
     * @param context
     * @return
     */
    public static String getOperators(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = null;
        try {
            String IMSI = tm.getSubscriberId();
            if (IMSI == null || IMSI.equals("")) {
                return operator;
            }
            if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
                operator = "中国移动";
            } else if (IMSI.startsWith("46001")) {
                operator = "中国联通";
            } else if (IMSI.startsWith("46003")) {
                operator = "中国电信";
            }
            return operator;
        }catch (SecurityException e){
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 获取手机imsi号
     * */
    public static String getImsi(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            String _imsi = tm.getSubscriberId();
            if (_imsi != null && !_imsi.equals("")) {
                return _imsi;
            }
            return "未知";
        }catch (SecurityException e){
            e.printStackTrace();
        }
        return "未知";
    }
    /**
     * 获取手机应用信息
     * @param context
     * @return
     */
    public static String getAllApp(Context context) {
        String result = "";
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        for (PackageInfo i : packages) {
            if ((i.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                result += i.applicationInfo.loadLabel(context.getPackageManager()).toString() + ",";
            }
        }
        return result.substring(0, result.length() - 1);
    }
    /**
     * 获取基带版本号
     *
     * @return
     */
    public static String getBaseband() {
        try {

            Class cl = Class.forName("android.os.SystemProperties");

            Object invoker = cl.newInstance();

            Method m = cl.getMethod("get", new Class[]{String.class, String.class});

            Object result = m.invoke(invoker, new Object[]{"gsm.version.baseband", "no message"});

            return (String) result;
        } catch (Exception e) {
            Log.e("bxb","读取基带版本失败" + e.toString());
        }
        return null;
    }
    /**
     * 获取Serial序列号
     *
     * @return
     */
    public static String getSerial() {
        try {
            String serial = Build.SERIAL;

            return serial;
        } catch (Exception e) {
            Log.e("bxb","读取基带版本失败" + e.toString());
        }
        return null;
    }
    /**
     * 获取手机的总内存和可用内存
     * @param context
     * @return
     */
    public static String[] getTotalMemory(Context context) {
        String[] result = {"",""};  //1-total 2-avail
        ActivityManager mActivityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        mActivityManager.getMemoryInfo(mi);
        long mTotalMem = 0;
        long mAvailMem = mi.availMem;
        String str1 = "/proc/meminfo";
        String str2;
        String[] arrayOfString;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            localBufferedReader.close();
            mTotalMem = Long.valueOf(arrayOfString[1]).longValue()*1024;
        } catch (IOException e) {
            e.printStackTrace();
        }
        result[0] = mTotalMem+"";
        result[1] = mAvailMem+"";
        return result;
    }
}
