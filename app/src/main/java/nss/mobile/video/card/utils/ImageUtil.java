
package nss.mobile.video.card.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @description 图片处理 （加载 、释放资源）工具类
 * @author liwei
 * @date 2016年10月11日
 * @version icon_1.0
 */
public class ImageUtil {

	/** 水平方向模糊度 */
	private static float hRadius = 5;
	/** 竖直方向模糊度 */
	private static float vRadius = 5;
	/** 模糊迭代度 */
	private static int iterations =3;
	public static int clamp(int x, int a, int b) {
		return (x < a) ? a : (x > b) ? b : x;
	}

	/**
	 * 从本地 读取 图片
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Bitmap readBitmap(Context context, int resId){
		  BitmapFactory.Options opt = new  BitmapFactory.Options();
	      opt.inPreferredConfig =  Bitmap.Config.RGB_565;
	      opt.inPurgeable = true;
	      opt.inInputShareable = true;
	      //  获取资源图片
	      InputStream is =  context.getResources().openRawResource(resId);
	      return  BitmapFactory.decodeStream(is, null, opt);
	}
	
	/**
	 * 释放图片资源
	 * @param bitmap
	 */
	public static void recycle(Bitmap bitmap){
		if(bitmap != null && !bitmap.isRecycled()){
			bitmap.recycle();
			System.gc();
			bitmap = null;
		}
	}
	 
	 /**
	  * 图片高斯模糊处理
	  * @param bmp
	  * @param context
	  * @return
	  */
	public static Drawable BlurImages(Bitmap bmp, Context context) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bmp.getPixels(inPixels, 0, width, 0, 0, width, height);
		for (int i = 0; i < iterations; i++) {
			blur(inPixels, outPixels, width, height, hRadius);
			blur(outPixels, inPixels, height, width, vRadius);
		}
		blurFractional(inPixels, outPixels, width, height, hRadius);
		blurFractional(outPixels, inPixels, height, width, vRadius);
		bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
		Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
		return drawable;
	}

	/**
	 * 图片高斯模糊算法
	 */
	public static void blur(int[] in, int[] out, int width, int height,
			float radius) {
		int widthMinus1 = width - 1;
		int r = (int) radius;
		int tableSize = 2 * r + 1;
		int divide[] = new int[256 * tableSize];
		for (int i = 0; i < 256 * tableSize; i++)
			divide[i] = i / tableSize;
		int inIndex = 0;
		for (int y = 0; y < height; y++) {
			int outIndex = y;
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int i = -r; i <= r; i++) {
				int rgb = in[inIndex + clamp(i, 0, width - 1)];
				ta += (rgb >> 24) & 0xff;
				tr += (rgb >> 16) & 0xff;
				tg += (rgb >> 8) & 0xff;
				tb += rgb & 0xff;
			}
			for (int x = 0; x < width; x++) {
				out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16)
						| (divide[tg] << 8) | divide[tb];
				int i1 = x + r + 1;
				if (i1 > widthMinus1)
					i1 = widthMinus1;
				int i2 = x - r;
				if (i2 < 0)
					i2 = 0;
				int rgb1 = in[inIndex + i1];
				int rgb2 = in[inIndex + i2];
				ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
				tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
				tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
				tb += (rgb1 & 0xff) - (rgb2 & 0xff);
				outIndex += height;
			}
			inIndex += width;
		}
	}

	/**
	 * 图片高斯模糊算法 
	 */
	public static void blurFractional(int[] in, int[] out, int width,
			int height, float radius) {
		radius -= (int) radius;
		float f = 1.0f / (1 + 2 * radius);
		int inIndex = 0;
		for (int y = 0; y < height; y++) {
			int outIndex = y;
			out[outIndex] = in[0];
			outIndex += height;
			for (int x = 1; x < width - 1; x++) {
				int i = inIndex + x;
				int rgb1 = in[i - 1];
				int rgb2 = in[i];
				int rgb3 = in[i + 1];
				int a1 = (rgb1 >> 24) & 0xff;
				int r1 = (rgb1 >> 16) & 0xff;
				int g1 = (rgb1 >> 8) & 0xff;
				int b1 = rgb1 & 0xff;
				int a2 = (rgb2 >> 24) & 0xff;
				int r2 = (rgb2 >> 16) & 0xff;
				int g2 = (rgb2 >> 8) & 0xff;
				int b2 = rgb2 & 0xff;
				int a3 = (rgb3 >> 24) & 0xff;
				int r3 = (rgb3 >> 16) & 0xff;
				int g3 = (rgb3 >> 8) & 0xff;
				int b3 = rgb3 & 0xff;
				a1 = a2 + (int) ((a1 + a3) * radius);
				r1 = r2 + (int) ((r1 + r3) * radius);
				g1 = g2 + (int) ((g1 + g3) * radius);
				b1 = b2 + (int) ((b1 + b3) * radius);
				a1 *= f;
				r1 *= f;
				g1 *= f;
				b1 *= f;
				out[outIndex] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
				outIndex += height;
			}
			out[outIndex] = in[width - 1];
			inIndex += width;
		}
	}

	/**
	 * 加载本地图片 
	 * @param context
	 */
	public static Drawable loadLocalImage(Context context, String photoUrl){
		if(photoUrl==null) return null;
        try {
            // 获取指定路径下的图片，正常有的图片需要压缩，否则内存泄露
            Bitmap photos = BitmapFactory.decodeStream(new FileInputStream(photoUrl));
            if (photos != null) {
                Drawable drawable = new BitmapDrawable(context.getResources(),	photos);
                return drawable;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return null;
	}

	/**
	 * 本地读取图片
	 * @param path
	 * @return
	 */
	public static Bitmap readImage(String path){
		if(path == null) return null;
		Bitmap photos = null;
		try {
			photos = BitmapFactory.decodeStream(new FileInputStream(path));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return photos;
	}
	
	/**
	 * 通过Base32将Bitmap转换成Base64字符串 
	 * @param bit
	 * @return
	 */
	public static String bitmap2StrByBase64(Bitmap bit) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bit.compress(CompressFormat.JPEG, 40, bos);// 参数100表示不压缩
		byte[] bytes = bos.toByteArray();
		return Base64.encodeToString(bytes, Base64.DEFAULT);
	} 
	
	/**
	 * 把 base64位 转成 bitmap
	 * 
	 * @param string
	 * @return
	 */
	public static Bitmap stringtoBitmap(String string) {
		// 将字符串转换成Bitmap类型
		Bitmap bitmap = null;
		try {
			byte[] bitmapArray;
			bitmapArray = Base64.decode(string, Base64.DEFAULT);
			bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
					bitmapArray.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	//将图像保存到SD卡中
	@SuppressLint("SdCardPath")
	public static String saveMyBitmap(String bitName, Bitmap mBitmap) {
		String path =  Environment.getExternalStorageDirectory() +"/" + bitName + ".png";
		File f = new File(path);
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mBitmap.compress(CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}
}
