package nss.mobile.video.card.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import nss.mobile.video.card.provider.Config;
import nss.mobile.video.card.utils.LogUtil;

/**
 * Created by leo on 2018/8/26.
 */
public class FaceRectView extends View {

    private static Rect rect;
    private Paint paint;
    private static int width, height;

    public FaceRectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        rect = new Rect();
        paint = new Paint(Paint.DITHER_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.GREEN);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        height = getMeasuredHeight();
        width = getMeasuredWidth();
        Log.i("liwei","width:"+width);
        Log.i("liwei","height:"+height);
    }

    public static void setRect(Rect _rect) {
        if (_rect == null) {
            rect.set(0, 0, 0, 0);
            return;
        }
        switch (Config.CameraDirection) {
            case 0:
                LogUtil.i("liwei","0");
                int left = width - (_rect.right * width / Config.CameraHight);
                int right = width - (_rect.left * width / Config.CameraHight);
                rect.left = left;
                rect.top = _rect.top * height / Config.CameraWidth;
                rect.right = right;
                rect.bottom = _rect.bottom * height / Config.CameraWidth;
                break;
            case 1:
                LogUtil.i("liwei","1");

                rect.left = _rect.left * width / Config.CameraHight;
                rect.top = _rect.top * height / Config.CameraWidth;
                rect.right = _rect.right * width / Config.CameraHight;
                rect.bottom = _rect.bottom * height / Config.CameraWidth;

                LogUtil.i("rect.left2:" + rect.left);
                LogUtil.i("rect.top2:" + rect.top);
                LogUtil.i("rect.right2:" + rect.right);
                LogUtil.i("rect.bottom2:" + rect.bottom);
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, paint);
    }
}
