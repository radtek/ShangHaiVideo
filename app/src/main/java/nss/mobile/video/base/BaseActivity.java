package nss.mobile.video.base;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.zhy.http.okhttp.OkHttpUtils;

import nss.mobile.video.R;


import java.util.ArrayList;

import me.nereo.multi_image_selector.MultiImageSelector;
import nss.mobile.video.C;
import nss.mobile.video.base.bind.BindViewUtils;
import nss.mobile.video.utils.ActivityUtils;
import nss.mobile.video.zxing.activity.CaptureActivity;


/**
 * Created by mrqiu on 2017/9/19.
 */

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener, IDialog, IAlbum {

    private static final int CHOOSE_PHOTO = 0xffff;
    public static final String BUNDLE = "bundle";
    private static final int REQUEST_CODE_ASK_CAMERA = 22;
    private static final int REQUEST_CODE_DECODE = 23;
    private BaseFragment currentKJFragment;
    public QMUITopBar mTopBar;
    private QMUITipDialog mLoadingDialog;

    public int sSAVE_IMAGE_MAX_SIZE = 3;

    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    public static final int REQUEST_IMAGE = 99;

    private ArrayList<String> mSelectImgs;//当前选中多张图片；
    private QMUITipDialog tipSuccessDialog;
    private QMUITipDialog tipFailDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 避免从桌面启动程序后，会重新实例化入口类的activity
        if (!this.isTaskRoot()) {
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                    finish();
                    return;
                }
            }
        }

        ActivityUtils.addAty(this);
        createView();
        BindViewUtils.find(this);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initBar();

        initStatusBar();
        initData();
        init(savedInstanceState);
        initWidget();

    }

    public void initBar() {


    }

    public void init(Bundle savedInstanceState) {
    }


    public void initStatusBar() {
        QMUIStatusBarHelper.setStatusBarLightMode(this);
    }

    public void toPhone(String phone) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));//跳转到拨号界
        startActivity(dialIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /*********************************************************************/
    /*********************************************************************/
    /********************
     * 用于隐藏软键盘
     **********************************/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus()) {
            /**
             * 点击空白位置 隐藏软键盘
             */
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     *
     * @param token
     */
    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /********************************************************************/

    public void createView() {
        LayoutUtils.bind(this);
    }

    public void initData() {

    }

    public void initWidget() {

    }

    @TargetApi(19)
    public void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            onClickKeyToBack();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 当点击返回键
     */
    public void onClickKeyToBack() {

    }

    /***********************************************************************/

    public void myChangeFragment(int resView, BaseFragment targetFragment) {
        if (targetFragment.equals(currentKJFragment)) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        if (!targetFragment.isAdded()) {
            transaction.add(resView, targetFragment, targetFragment.getClass()
                    .getName());
        }
        if (targetFragment.isHidden()) {
            transaction.show(targetFragment);
            targetFragment.onChange();
        }
        if (currentKJFragment != null && currentKJFragment.isVisible()) {
            transaction.hide(currentKJFragment);
        }
        currentKJFragment = targetFragment;
        transaction.commit();

    }

    private final static int MIN_CLICK_TIME = 300;
    private long lastTime;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.qmui_topbar_item_left_back) {
            clickTopbarLeftImgs();
        }
        if (System.currentTimeMillis() - lastTime > MIN_CLICK_TIME) {
            forbidClick(v);
            lastTime = System.currentTimeMillis();
        }
    }

    public void clickTopbarLeftImgs() {
        finish();
    }

    public void forbidClick(View v) {

    }

    public void toFocusable(View v) {
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        v.requestFocus();
    }

    public <T extends Object> void startActivity(Class<T> t) {
        Intent intent = new Intent(this, t);
        startActivity(intent);
    }

    public <T extends BaseActivity> void startActivity(Class<T> t, int requestCode) {
        Intent intent = new Intent(this, t);
        startActivityForResult(intent, requestCode);
    }

    public <T extends BaseActivity> void startActivity(Class<T> t, Bundle bundle) {
        Intent intent = new Intent(this, t);
        intent.putExtra("bundle", bundle);
        startActivity(intent);
    }

    public <T extends BaseActivity> void startActivity(Class<T> t, Bundle bundle, int requestCode) {
        Intent intent = new Intent(this, t);
        intent.putExtra("bundle", bundle);
        startActivityForResult(intent, requestCode);
    }

    public Bundle getBundle() {
        Intent intent = getIntent();
        if (intent == null) {
            return new Bundle();
        }
        if (intent.getBundleExtra("bundle") == null) {
            return new Bundle();
        }
        return intent.getBundleExtra("bundle");
    }

    @Override
    public void displayLoadingDialog(CharSequence msg) {
        cancelLoadingDialog();
        mLoadingDialog = new QMUITipDialog.Builder(this)
                .setTipWord(msg)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .create();
        if (isFinishing()) {
            return;
        }
        mLoadingDialog.show();
    }

    @Override
    public void cancelLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag(this);
        ActivityUtils.remove(this);
    }


    /**********************************************************************************/
    /******************************
     * 打开相册
     *********************************/
    @Override
    public void toOpenAlbum() {
        //判断是否添加权限 读取照片
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //当前没有权限,去申请权限
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        } else {
            //已经获取权限
            openAlbum();
        }
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);//打开相册
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果是document类型的Uri ，则通过document id处理；
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];//解析出数字格式的id;
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads")
                        , Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的Uri ,则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是file类型的Uri,直接获取图片路径即可
            imagePath = uri.getPath();
        }
        onResultSingleImgs(imagePath);//根据图片路径显示图片
    }

    /**
     * 选择单张图片
     *
     * @param imagePath
     */
    public void onResultSingleImgs(String imagePath) {

    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        onResultSingleImgs(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_DECODE:
                if (resultCode != Activity.RESULT_OK) {
                    onResultCode(null, false);
                    return;
                }
                String resultContent = CaptureActivity.getResultContent(data);
                onResultCode(resultContent, true);
                break;
            case REQUEST_IMAGE:
                if (resultCode != Activity.RESULT_OK) {
                    return;
                }
                ArrayList<String> mSelectPath = data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);
                onResultMoreImgs(mSelectPath);
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        //4.4及以上系统使用这个方法处理图片
                        handleImageOnKitKat(data);
                    } else {
                        //4.4以下系统使用这个方法处理图片
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public void onResultCode(String resultContent, boolean b) {
    }


    /**
     * 选择多张图片回调
     *
     * @param mSelectPath
     */
    public void onResultMoreImgs(ArrayList<String> mSelectPath) {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] granResults) {
        switch (requestCode) {
            case 1:
                if (granResults.length > 0 && granResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "you denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_STORAGE_READ_ACCESS_PERMISSION:
                if (granResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImage(mSelectImgs);
                }
                break;
            case REQUEST_CODE_ASK_CAMERA:
                if (granResults[0] == PackageManager.PERMISSION_GRANTED) {
                    toScanningActivity();
                } else {
                    displayMessageDialog("请到系统设置中，给予当前APP权限");
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, granResults);
        }

    }

    public BaseActivity setTextView(CharSequence s, TextView tv) {
        setTextView(s, "", tv);
        return this;
    }

    public BaseActivity setTextView(CharSequence s, String normal, TextView tv) {
        if (TextUtils.isEmpty(s)) {
            s = normal;
        }
        tv.setText(s);
        return this;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }


    @Override
    public void openAlbumMore(int sSAVE_IMAGE_MAX_SIZE, ArrayList<String> srcList) {
        this.sSAVE_IMAGE_MAX_SIZE = sSAVE_IMAGE_MAX_SIZE;
        this.mSelectImgs = srcList;
        pickImage(srcList);
    }

    @Override
    public void pickImage(ArrayList<String> selectPath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.mis_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            int maxNum = sSAVE_IMAGE_MAX_SIZE;


            MultiImageSelector selector = MultiImageSelector.create(this);
            selector.showCamera(false);
            selector.count(maxNum);
            // selector.single();
            selector.multi();
            selector.origin(selectPath);
            selector.start(this, REQUEST_IMAGE);
        }
    }

    private void requestPermission(final String permission, String rationale, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.mis_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.mis_permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(BaseActivity.this, new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.mis_permission_dialog_cancel, null)
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void displayMessageDialog(CharSequence msg) {
        if (isFinishing()) {
            return;
        }
        new QMUIDialog.MessageDialogBuilder(this)
                .setTitle("提示")
                .setMessage(msg)
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public void displayMessageDialog(CharSequence msg, boolean isCancel) {
        if (isFinishing()) {
            return;
        }
        QMUIDialog show = new QMUIDialog.MessageDialogBuilder(this)
                .setTitle("提示")
                .setMessage(msg)
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.cancel();
                    }
                })
                .show();
        show.setCancelable(isCancel);
    }

    @Override
    public void displayMessageDialog(CharSequence msg, String action, QMUIDialogAction.ActionListener l) {
        if (isFinishing()) {
            return;
        }
        new QMUIDialog.MessageDialogBuilder(this)
                .setTitle("提示")
                .setMessage(msg)
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.cancel();
                    }
                })
                .addAction(action, l)
                .show();
    }


    @Override
    public void displayTipDialogSuccess(CharSequence msg, long l) {
        if (isFinishing()) {
            return;
        }
        if (tipSuccessDialog != null && tipSuccessDialog.isShowing()) {
            tipSuccessDialog.dismiss();
        }
        QMUITipDialog.Builder builder = new QMUITipDialog.Builder(this).setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS);
        if (!TextUtils.isEmpty(msg)) {
            builder.setTipWord(msg);
        }
        tipSuccessDialog = builder.create();
        tipSuccessDialog.show();
        C.sHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cancelTipDialogSuccess();
            }
        }, l);
    }

    @Override
    public void displayTipDialogSuccess(CharSequence msg) {
        displayTipDialogSuccess(msg, 1_000);
    }

    @Override
    public void cancelTipDialogSuccess() {
        if (tipSuccessDialog == null) {
            return;
        }
        tipSuccessDialog.dismiss();
        tipSuccessDialog = null;
    }


    @Override
    public void displayTipDialogFail(CharSequence msg, long l) {
        if (tipFailDialog != null && tipFailDialog.isShowing()) {
            tipFailDialog.dismiss();
        }
        QMUITipDialog.Builder builder = new QMUITipDialog.Builder(this).setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL);
        if (!TextUtils.isEmpty(msg)) {
            builder.setTipWord(msg);
        }
        tipFailDialog = builder.create();
        tipFailDialog.show();
        C.sHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cancelTipDialogFail();
            }
        }, l);
    }

    @Override
    public void displayTipDialogFail(CharSequence msg) {
        displayTipDialogFail(msg, 1_000);
    }

    @Override
    public void cancelTipDialogFail() {
        if (tipFailDialog == null) {
            return;
        }
        tipFailDialog.dismiss();
        tipFailDialog = null;
    }

    /**
     * 跳转到扫描页面
     */
    public void toScanningActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    /**
                     * 这个API主要用于给用户一个申请权限的解释，
                     * 该方法只有在用户在上一次已经拒绝过你的这个权限申请。
                     * 也就是说，用户已经拒绝一次了，
                     * 你又弹个授权框，你需要给用户一个解释，为什么要授权，则使用该方法。
                     */
                    //ToastUtils.show(this, "没有权限");
                    displayMessageDialog("请前往应用设置，中赋予摄像头权限");
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_ASK_CAMERA);
                }
                return;
            } else {
                startActivity(CaptureActivity.class, REQUEST_CODE_DECODE);
            }
        } else {
            startActivity(CaptureActivity.class, REQUEST_CODE_DECODE);
        }
    }

    @Override
    public void toast(CharSequence msg) {

    }
}


