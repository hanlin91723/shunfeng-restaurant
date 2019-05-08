package com.network.okhttp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Matrix;

import android.media.ExifInterface;

import com.ninecm.utils.FileUtil;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;

import java.io.OutputStream;
import java.util.UUID;

public class CompressImage {

	public static File compressToFile(String srcPath,int imageTargetWidth,int imageTargetHeight,int image_limit_quality,int maxSize){
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 800f;// 这里设置高度为800f
		float ww = 480f;// 这里设置宽度为480f
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (w > h && w > imageTargetWidth) {// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / imageTargetWidth);
		} else if (w < h && h > imageTargetHeight) {// 如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / imageTargetHeight);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置缩放比例
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		bitmap=getBitMap(bitmap,srcPath);
		return compressImage(bitmap,image_limit_quality,maxSize);// 压缩好比例大小后再进行质量压缩
	}
	public static File compressImage(Bitmap image,int quality,int maxSize) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, quality, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int size =80;
		while (baos.toByteArray().length / 1024 > maxSize) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, size, baos);// 这里压缩options%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		try {
			if(isBm!=null){
				isBm.close();
			}
			if(baos!=null){
				baos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return commpressImage(bitmap);
	}
	public static File commpressImage(Bitmap bitmap) {
		String tmpPath = null;
		Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
		int quality = 100;
		OutputStream stream = null;
		try {
			tmpPath = FileUtil.initPath("thumbnail") + File.separator + UUID.randomUUID() + ".png";
			stream = new FileOutputStream(tmpPath);
			bitmap.compress(format, quality, stream);
			if(bitmap!=null&&!bitmap.isRecycled()){
				bitmap.recycle();   //回收图片所占的内存
				System.gc();//提醒系统及时回收
			}
			stream.flush();
			stream.close();
			// 上传图片
			File file = new File(tmpPath);
			return file;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static Bitmap getBitMap(Bitmap bitmap,String filePath){
		ExifInterface exif;
		try {
			exif = new ExifInterface(filePath);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
			Matrix matrix = new Matrix();
			if (orientation == 6) {
				matrix.postRotate(90);
			} else if (orientation == 3) {
				matrix.postRotate(180);
			} else if (orientation == 8) {
				matrix.postRotate(270);
			}
			Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			bitmap1.setDensity(bitmap.getDensity());
			return bitmap;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
