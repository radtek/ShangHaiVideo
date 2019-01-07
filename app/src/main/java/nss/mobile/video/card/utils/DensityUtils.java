package nss.mobile.video.card.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

/**
 * @description 常用单位转换的辅助类
 * @author liwei
 * @date 2016年10月11日
 * @version icon_1.0
 */
public class DensityUtils {
	
	private DensityUtils() {
		throw new UnsupportedOperationException("cannot be instantiated");
	}

	/**
	 * 获得屏幕宽度
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenWidth(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return outMetrics.widthPixels;
	}

	/**
	 * 获得屏幕高度
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenHeight(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return outMetrics.heightPixels;
	}

	/**
	 * 获得控件的宽、高
	 * @param context
	 * @return
	 */
	public static int[] getViewWidthHigh(final View view, Context context) {
        final int[] chiCun = new int[2];
        view.measure( 0,  0);
        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();
        chiCun[0]=measuredWidth;
        chiCun[1]=measuredHeight;
		return chiCun;
	}

/*public static int[] getViewWidthHigh(final View view,Context context){
	final int[] chiCun = new int[2];
	ViewTreeObserver vto2 = view.getViewTreeObserver();
	vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			//textView.append("\n\n"+imageView.getHeight()+","+imageView.getWidth());
			chiCun[0]=view.getWidth();
			chiCun[1]=view.getHeight();
		}
	});
	return chiCun;
}*/


	/**
	 * dp转px
	 * 
	 * @param context
	 * @return
	 */
	public static int dp2px(Context context, float dpVal) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dpVal, context.getResources().getDisplayMetrics());
	}

	/**
	 * sp转px
	 * 
	 * @param context
	 * @return
	 */
	public static int sp2px(Context context, float spVal) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
				spVal, context.getResources().getDisplayMetrics());
	}

	/**
	 * px转dp
	 * 
	 * @param context
	 * @param pxVal
	 * @return
	 */
	public static float px2dp(Context context, float pxVal) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (pxVal / scale);
	}

	/**
	 * px转sp
	 * @param pxVal
	 * @return
	 */
	public static float px2sp(Context context, float pxVal) {
		return (pxVal / context.getResources().getDisplayMetrics().scaledDensity);
	}

}
