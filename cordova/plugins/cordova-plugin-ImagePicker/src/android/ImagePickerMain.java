package com.giants.imagepicker;
import android.content.Intent;
import android.graphics.BitmapFactory;
import com.giants.imagepicker.bean.ImageItem;
import com.giants.imagepicker.compresshelper.CompressHelper;
import com.giants.imagepicker.ui.ImageGridActivity;
import com.giants.imagepicker.view.CropImageView;
import com.network.okhttp.CompressImage;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Administrator on 2017/8/29.
 */
public class ImagePickerMain extends CordovaPlugin {

    private static final String TAG = "MultiImagesPicker";
    private static final int IMAGE_DEFAULT_WIDTH = 1920;
    private static final int IMAGE_DEFAULT_HEIGHT = 1440;
    private static final int IMAGE_DEFAULT_QUALITY = 100;
    private static final int IMAGE_DEFAULT_MAX_IMAGES_COUNT = 9;
    private int imageTargetWidth;
    private int imageTargetHeight;

    private int image_limit_width = 0;
    private int image_limit_height = 0;
    private int image_limit_quality = 0;


    private CallbackContext callbackContext;
    private JSONObject params;
    private ImagePicker imagePicker = ImagePicker.getInstance();
    ArrayList<ImageItem> images = null;

    private File oldFile;
    private File newFile;




    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();

        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器

        imagePicker.setShowCamera(false);  //显示拍照按钮
        imagePicker.setCrop(false);        //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); //是否按矩形区域保存
        imagePicker.setSelectLimit(9);    //选中数量限制
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);//保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);//保存文件的高度。单位像素
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        this.callbackContext = callbackContext;
        this.params = args.getJSONObject(0);

        if (action.equals("getPictures")) {
            if(this.params != null){
                imagePicker.setSelectLimit(this.params.getInt("maximumImagesCount"));
                image_limit_width = this.params.getInt("width");
                image_limit_height = this.params.getInt("height");
                image_limit_quality = this.params.getInt("quality");
            }else {
                imagePicker.setSelectLimit(IMAGE_DEFAULT_MAX_IMAGES_COUNT);
                image_limit_width = IMAGE_DEFAULT_WIDTH;
                image_limit_height = IMAGE_DEFAULT_HEIGHT;
                image_limit_quality = IMAGE_DEFAULT_QUALITY;
            }

            Intent intent = new Intent(cordova.getActivity().getApplicationContext(), ImageGridActivity.class);
            intent.putExtra(ImageGridActivity.EXTRAS_IMAGES,images);
            cordova.startActivityForResult((CordovaPlugin)this,intent, 100);


            return true;
        }

        return false;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == 100) {
                images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                JSONObject jsonObject=new JSONObject();
                for (Iterator it = images.iterator(); it.hasNext(); ) {
                    ImageItem imageItem = (ImageItem) it.next();
                    oldFile = new File(imageItem.path);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(imageItem.path, options);
                    //获取图片的宽高
                    int height = options.outHeight;
                    int width = options.outWidth;
                    if (width >= height) {
                        imageTargetWidth = image_limit_width;
                        imageTargetHeight = image_limit_height;
                    } else {
                        imageTargetWidth = image_limit_height;
                        imageTargetHeight = image_limit_width;

                    }
                    newFile = CompressImage.compressToFile(oldFile.getAbsolutePath(), imageTargetWidth, imageTargetHeight, image_limit_quality);
                    try {
                        jsonObject.put("oldFilePath",oldFile.getAbsolutePath());
                        jsonObject.put("oldFileSize",formetFileSize(oldFile.length()));
                         if(newFile.exists()&&newFile.isFile()){
                             jsonObject.put("tempFilePath",newFile.getAbsolutePath());
                             jsonObject.put("tempFileSize",formetFileSize(newFile.length()));
                         }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        this.callbackContext.error(e.getMessage());
                    }
                }
                images.clear();
                this.callbackContext.success(jsonObject);
            }
        }
    }
    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public String formetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }
}
