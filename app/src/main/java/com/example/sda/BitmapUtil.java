package com.example.sda;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class BitmapUtil {
	public static  Bitmap compressImage(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		Log.e("compressImage", baos.toByteArray().length / 1024+"  kb");
		while ( baos.toByteArray().length / 1024>200) {	//循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();//重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;//每次都减少10
		}
		Log.e("compressImage", baos.toByteArray().length / 1024+"  kb");
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm);//把ByteArrayInputStream数据生成图片
		try {
			File f = new File(Environment.getExternalStorageDirectory(),
					"edtimg.jpg");
			f.createNewFile();

			FileOutputStream fOut = new FileOutputStream(f);
//			Log.e("-----", bitmap.getHeight() + "_____" + bitmap.getWidth()
//					+ "________" + bitmap.getByteCount()+ "________" + bitmap.getRowBytes());
//			bitmap = ThumbnailUtils.extractThumbnail(bitmap, 300, 300);
//			Log.e("======", bitmap.getHeight() + "_____" + bitmap.getWidth()
//					+ "________" + bitmap.getByteCount()+ "________" + bitmap.getRowBytes());
			bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
			fOut.flush();
			fOut.close();
		} catch (Exception e1) {



			e1.printStackTrace();
		}

		return bitmap;
	}


	public static Bitmap comp(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		Log.e("comp", baos.toByteArray().length / 1024+"  kb");
		if( baos.toByteArray().length / 1024>200) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
			baos.reset();//重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
		}
		Log.e("comp2", baos.toByteArray().length / 1024+"  kb");
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
//		 DisplayMetrics metrics = new DisplayMetrics();
//		 activity.getWindowManager().getDefaultDisplay()
//	                .getMetrics(metrics);
//			float hh = metrics.heightPixels;//这里设置高度为800f
//			float ww = metrics.heightPixels;//这里设置宽度为480f
		//现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 800f;//这里设置高度为800f
		float ww = 480f;//这里设置宽度为480f
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;//be=1表示不缩放
		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//设置缩放比例
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		isBm = new ByteArrayInputStream(baos.toByteArray());
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
	}
}
