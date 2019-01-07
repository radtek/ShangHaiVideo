package nss.mobile.video.card.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.util.Base64;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtil {

    /**
     * method:通过一个模板裁剪另一张图
     *
     * @param context   ：上下文
     * @param originPic :被裁剪的对象
     * @param reverPic  ：裁剪的模板
     */
    public Bitmap makeMaskImage(Context context, int originPic, int reverPic) {
        Bitmap originalPic = BitmapLoadOutMerry(context, originPic);
        Bitmap original = setBitmapWidthHight(originalPic, 475, 1448.75);
        Bitmap maskOrigin = BitmapLoadOutMerry(context, reverPic);
        Bitmap mask = setBitmapWidthHight(maskOrigin, 475, 1448.75);
        Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(),
                Config.ARGB_4444);
        Canvas mCanvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mCanvas.drawBitmap(original, 0, 0, null);
        mCanvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(null);
        return result;
    }

    /**
     * method:通过一个模板裁剪另一张图
     * @param context   ：上下文
     * @param originPic :被裁剪的对象
     * @param reverPic  ：裁剪的模板
     */
    public Bitmap makeMaskImageScene(Context context, int originPic,
                                     int reverPic, float width, float hight) {
        //ARGB_8888
        Bitmap originalPic = BitmapLoadOutMerry(context, originPic);
        Bitmap original = setBitmapWidthHight(originalPic, width, hight);
        Bitmap maskOrigin = BitmapLoadOutMerry(context, reverPic);
        Bitmap mask = setBitmapWidthHight(maskOrigin, width, hight);
        Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(),
                Config.ARGB_4444);
        Canvas mCanvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mCanvas.drawBitmap(original, 0, 0, null);
        mCanvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(null);
        return result;
    }

    /**
     * method:按照一定的宽高设置图片
     *
     * @param bm        ：要修改大小的Bitmap对象
     * @param newWidth  ：想得到的宽
     * @param newHeight ：想得到的高
     * @return
     */
    public static Bitmap setBitmapWidthHight(Bitmap bm, double newWidth,
                                             double newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true);
        return newbm;
    }

    /**
     * method:按照一定的高设置图片
     * @param bm        ：要修改大小的Bitmap对象
     * @param newHeight ：想得到的高
     * @return
     */
    public Bitmap setBitmapHight(Bitmap bm, double newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleHeight, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true);
        return newbm;
    }

    /**
     * method:按照一定的比例设置图片
     * @param x ：按照x的比例来缩放
     * @param y ：按照y的比例来缩放
     * @return
     */
    public static Bitmap bitmapScale(Bitmap bitmap, float x, float y) {
        Matrix matrix = new Matrix();
        matrix.postScale(x, y); // 长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }

    /**
     * 以最省内存的方式读取本地资源的图片,以防止超出内存
     * @param context ：上下文
     * @param resId   ：资源图片
     * @return
     */
    public static Bitmap BitmapLoadOutMerry(Context context, int resId) {
        Options opt = new Options();
        opt.inPreferredConfig = Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    /**
     * 获取opt的值，以应用在防止内存溢出的Bitmap对象中
     * @return
     */
    public Options getOpt() {
        Options opt = new Options();
        opt.inPreferredConfig = Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        return opt;
    }

    /**
     * 以最省内存的方式读取本地资源的图片,以防止超出内存
     * @param context ：上下文
     * @return
     */
    public Bitmap BitmapOutMerryOther(Context context, InputStream is) {
        Options opt = new Options();
        opt.inPreferredConfig = Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        return BitmapFactory.decodeStream(is, null, opt);
    }

    /**
     * 获取图片信息，返回Bitmap对象
     * @param resId ：资源图片
     * @return
     */
    public Bitmap getPictureInfo(ImageView image, int resId) {
        Bitmap bitmap = null;
        Drawable drawable = image.getDrawable();// 获取有无图片
        if (drawable != null) {
            image.setDrawingCacheEnabled(true);// 不调用就无法从ImageView对象iv_photo中获取图像；
            bitmap = Bitmap.createBitmap(image.getDrawingCache());
            image.setDrawingCacheEnabled(false);// 调用以清除缓存
        }
        return bitmap;
    }

    /**
     * View对象转为Bitmap对象 注意：这里的view只能通过findviewbyId来实例化，否则会报空
     * @param view ：要转为图片的View对象
     * @param left ：转化为图片的布局在整个布局的位置
     * @param top  ：转化为图片的布局在整个布局的位置
     * @return
     */
    public Bitmap createViewBitmap(View view, int left, int top) {
        /*
         * getWidth(): View在設定好佈局後整個View的寬度。 getMeasuredWidth():
		 * 對View上的內容進行測量後得到的View內容佔據的寬度 所以getWidth()比getMeasuredWidth()大，符合原大小
		 */
        view.setDrawingCacheEnabled(true);
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(left, top, view.getWidth(), view.getHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    /**
     * 保存Bitmap对象到手机相册
     * @param bitmap  ：Bitmap对象
     * @param context ：上下文对象
     * @return
     */
    public void savePicTOAlbum(Bitmap bitmap, Context context) {
        ScannerUtils scanner = new ScannerUtils();
        scanner.saveImageToGallery(context, bitmap, ScannerUtils.ScannerType.MEDIA);
    }

    /**
     * 将控件对象截图
     * @return
     */
    public Bitmap getElementPrintscreen(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();  //启用DrawingCache并创建位图
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache()); //创建一个DrawingCache的拷贝，因为DrawingCache得到的位图在禁用后会被回收
        view.setDrawingCacheEnabled(false);  //禁用DrawingCahce否则会影响性能
        return bitmap;
    }

    public void bitmapDelete(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    /**
     * 获取拍照的路径
     */
    public String getPicturePath(Context context) {
        String path = PreferencesUtils.get(context,"picturePath", "") + "";
        String fileName = PreferencesUtils.get(context,"fileName", "") + "";
        String picturePath = path;
        return picturePath;
    }

    /**
     * 进行页面拍照截图
     */
    public void takePhoto(Context context, ViewGroup view) {
        BitmapUtil bitmapUtil = new BitmapUtil();
        Bitmap bitmap = bitmapUtil.getElementPrintscreen(view);
        bitmapUtil.savePicTOAlbum(bitmap, context);
    }

    /**
     * 将图片剪裁为圆形
     */
    public static Bitmap createCircleImage(Bitmap source) {
        int length = source.getWidth() < source.getHeight() ? source.getWidth() : source.getHeight();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(length, length, Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        canvas.drawCircle(length / 2, length / 2, length / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }

    /**
     * 获取视频第一帧图片
     */
    public static Bitmap getVideoFirstBitmap(String path){
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(path);
        Bitmap bitmap = media.getFrameAtTime();
        return  bitmap;
    }

    //获取小的图片，意思是防止内存溢出
    public static Bitmap getSmallBitmap(String filePath, int reqWidth, int reqHeight) {
        final Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Config.ARGB_8888;
        options.inDither = true;
        return BitmapFactory.decodeFile(filePath, options);
    }

    //计算比例大小
    public static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            //计算图片高度和我们需要高度的最接近比例值
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            //宽度比例值
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            //取比例值中的较大值作为inSampleSize
            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 将Bitmap转换成Base64编码的字符串
     */
    public static String bitmapToBase64(Context context, Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    Bitmap convert(Bitmap a, int width, int height)
    {
        int w = a.getWidth();
        int h = a.getHeight();
        Bitmap newb = Bitmap.createBitmap(w, h, Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        Canvas cv = new Canvas(newb);
        Matrix m = new Matrix();
        m.postScale(1, -1);   //镜像垂直翻转
        m.postScale(-1, 1);   //镜像水平翻转
        m.postRotate(-90);  //旋转-90度
        Bitmap new2 = Bitmap.createBitmap(a, 0, 0, w, h, m, true);
        cv.drawBitmap(new2, new Rect(0, 0, new2.getWidth(), new2.getHeight()),new Rect(0, 0, w, h), null);
        return newb;
    }

    public static Bitmap yuvImage2Bitmap(byte[] nv21, int width, int height) {
        if (nv21 == null || width <= 0 || height <= 0) {
            return null;
        }
        YuvImage yuvimage = new YuvImage(
                nv21,
                ImageFormat.NV21,
                width,
                height,
                null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            try {
                yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, baos);
            } catch (IllegalArgumentException e) {
                int newWidth = height;
                int newHeight = width;
                Rect newArea = new Rect(0, 0, newWidth, newHeight);
                yuvimage.compressToJpeg(newArea, 70, baos);
            }
            byte[] rawImage = baos.toByteArray();
            Options options = new Options();
            options.inPreferredConfig = Config.RGB_565;
            Bitmap bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
            return bitmap;
        } catch (IllegalArgumentException e) {
            return null;
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
