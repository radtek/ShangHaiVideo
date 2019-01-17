package nss.mobile.video.card;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.card.android.graphics.ImageConverter;
import nss.mobile.video.card.liveface.CameraDevice;
import nss.mobile.video.card.liveface.ZKLiveFaceAnalyzer;
import nss.mobile.video.card.provider.Config;
import nss.mobile.video.card.provider.MySurfaceTextrureListener;
import nss.mobile.video.card.provider.SurfaceTextureDetailListener;
import nss.mobile.video.card.utils.BitmapUtil;
import nss.mobile.video.card.utils.LogUtil;
import nss.mobile.video.card.utils.ToastUtil;
import nss.mobile.video.card.view.FaceRectView;

/*现在的摄像头宽高是固定的，图片的也是固定的。所以可以根据比例来*/
public class VerifyActivity extends BaseActivity implements SurfaceTextureDetailListener {

    private FaceRectView mFaceRectView;
    private TextView mTvVerifyState;
    private ImageView mImageHead;
    private LinearLayout mLinear;

    //摄像头
    TextureView mTextureView;
    private Camera mCamera;
    private SurfaceTexture mSurfaceTexture;
    private Rect mRect;
    private byte[] mCardTemp = null;
    private byte[] mCardPhoto=null;
    private static int VERIFY_COUNT = 0;
    private boolean mIsShouldDetectRun = true;
    private Thread mThreadRenZheng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        initView();
        getCardTemp();
    }

    private void initView() {
        mTextureView=(TextureView) findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(new MySurfaceTextrureListener(this));
        mFaceRectView=(FaceRectView) findViewById(R.id.faceRectView);
        mImageHead=(ImageView) findViewById(R.id.imageHead);
        mTvVerifyState=(TextView) findViewById(R.id.tvVerifyState);
        mLinear=(LinearLayout) findViewById(R.id.linear);
        mLinear.setVisibility(View.GONE);
    }

    private void getCardTemp() {
        Intent intent=getIntent();
        mCardPhoto=intent.getByteArrayExtra("mPersonHead");
        Bitmap idphotoBm = BitmapFactory.decodeByteArray(mCardPhoto, 0, mCardPhoto.length);
        if (idphotoBm == null) {
            return;
        }
        mCardTemp = ZKLiveFaceAnalyzer.getInstance(VerifyActivity.this).detectFace(idphotoBm);
        idphotoBm.recycle();
        idphotoBm = null;
        LogUtil.i("mCardTemp:" + mCardTemp);
        if (mCardTemp == null) {
            ToastUtil.showShort(VerifyActivity.this,"身份证照片模板提取失败!");
            return;
        }else {
            startRenZheng();
        }
    }

    private void startRenZheng(){
        mThreadRenZheng = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (null != Config.VerifyFrameBuf) {
                        myThread(Config.VerifyFrameBuf);
                    }
                }
            }
        });
        mThreadRenZheng.start();
    }

    private Object myThread(byte[] data) {
        if (mIsShouldDetectRun == false) {
            return null;
        }
        final byte[] rotatedNv21Data = new byte[data.length];

        //0代表后置摄像头
        if (Config.CameraDirection==0){
            ImageConverter.rotateNV21(data, rotatedNv21Data, Config.CameraWidth, Config.CameraHight, 270, true);
        }else {
            ImageConverter.rotateNV21(data, rotatedNv21Data, Config.CameraWidth, Config.CameraHight, 90, true);
        }
        long faceContext = ZKLiveFaceAnalyzer.getInstance(VerifyActivity.this).detectFace(rotatedNv21Data, Config.CameraHight, Config.CameraWidth);
        byte[] current;
        if (faceContext != -1) {
            mRect = ZKLiveFaceAnalyzer.getInstance(VerifyActivity.this).getRect();
            if (mRect != null) {
                mFaceRectView.setRect(mRect);
                mFaceRectView.postInvalidate();
            } else {
                mFaceRectView.setRect(null);
                mFaceRectView.postInvalidate();
                return null;
            }
            // 活体检测
            if (Config.LiveFace==true){
                boolean isLiveness = ZKLiveFaceAnalyzer.getInstance(VerifyActivity.this).isLiveness(faceContext);
                if (!isLiveness) {
                    return null;
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            current = ZKLiveFaceAnalyzer.getInstance(VerifyActivity.this).extracTemplate(faceContext);
            if (current == null) {
                return null;
            }
        } else {
                mFaceRectView.setRect(null);
                mFaceRectView.postInvalidate();
            return null;
        }
        boolean isSuccess = ZKLiveFaceAnalyzer.getInstance(VerifyActivity.this).verify(mCardTemp, current);
        if (isSuccess) {
            verifySuccess(rotatedNv21Data);
        } else {
            VERIFY_COUNT++;
            if (VERIFY_COUNT == 30) {
                verifyFail();
            }
        }
        return null;
    }

    public void verifySuccess(final byte[] rotatedNv21Data) {
        mIsShouldDetectRun = false;
        VERIFY_COUNT = 0;
        mCardTemp = null;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLinear.setVisibility(View.VISIBLE);
                mFaceRectView.setVisibility(View.GONE);
                mTvVerifyState.setVisibility(View.VISIBLE);
                mTvVerifyState.setText("比对成功!");
                Config.VerifyFrameBuf = null;
                mThreadRenZheng.interrupt();
                Bitmap bitmaprotatedNv21 = BitmapUtil.yuvImage2Bitmap(rotatedNv21Data, Config.CameraHight, Config.CameraWidth);
                final Bitmap bitmapHead = Bitmap.createBitmap(bitmaprotatedNv21, mRect.left, mRect.top, mRect.width(), mRect.height());
                mImageHead.setImageBitmap(bitmapHead);
            }
        });
    }

    public void verifyFail() {
        mIsShouldDetectRun = false;
        VERIFY_COUNT = 0;
        mCardTemp = null;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLinear.setVisibility(View.VISIBLE);
                mFaceRectView.setVisibility(View.GONE);
                mTvVerifyState.setVisibility(View.VISIBLE);
                mTvVerifyState.setText("比对失败!");
                mImageHead.setVisibility(View.GONE);
                mIsShouldDetectRun = false;
                Config.VerifyFrameBuf = null;
                mThreadRenZheng.interrupt();
            }
        });
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurfaceTexture = surface;
        mCamera = CameraDevice.initCamera(mTextureView, mSurfaceTexture);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface, int width, int height) {
        releaseCamera();
        return true;
    }

    public void releaseCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void cancelAllTask() {
        mIsShouldDetectRun = false;
        Config.VerifyFrameBuf = null;
        if (mThreadRenZheng != null) {
            mThreadRenZheng.interrupt();
        }
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelAllTask();
    }
}
