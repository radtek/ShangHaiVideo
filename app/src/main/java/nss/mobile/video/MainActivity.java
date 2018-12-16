package nss.mobile.video;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.qmuiteam.qmui.util.QMUIPackageHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.bean.MobileKeyBean;
import nss.mobile.video.service.UploadFileUtils;
import nss.mobile.video.ui.HomeActivity;
import nss.mobile.video.utils.FileMeoryUtils;
import nss.mobile.video.utils.JsonUtils;
import nss.mobile.video.video.VideoCaptureActivity;
import nss.mobile.video.video.VideoFile;
import nss.mobile.video.video.configuration.CaptureConfiguration;
import nss.mobile.video.video.configuration.PredefinedCaptureConfigurations;
import okhttp3.Call;


@BindLayout(layoutRes = R.layout.activity_main, bindTopBar = false)
public class MainActivity extends BaseActivity {

    //申请两个权限，录音和文件读写
    //1、首先声明一个数组permissions，将需要的权限都放在里面
    String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //2、创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
    List<String> mPermissionList = new ArrayList<>();
    /**
     * 不再提示权限时的展示对话框
     */
    AlertDialog mPermissionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initPermission();
    }


    private final int mRequestCode = 100;//权限请求码

    //权限判断和申请
    private void initPermission() {

        mPermissionList.clear();//清空没有通过的权限

        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//添加还未授予的权限
            }
        }

        //申请权限
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        } else {


            //说明权限都已经通过，可以做你想做的事情去
            openVideo(null);
            finish();
        }
    }


    //请求权限后回调的方法
    //参数： requestCode  是我们自己定义的权限请求码
    //参数： permissions  是我们请求的权限名称数组
    //参数： grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                showPermissionDialog();//跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
            } else {
                //全部权限通过，可以进行下一步操作。。。
                openVideo(null);
                finish();
            }
        }

    }

    private void showPermissionDialog() {
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(this)
                    .setMessage("已禁用权限，请手动授予")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();

                            Uri packageURI = Uri.parse("package:" + getPackageName());
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //关闭页面或者做其他操作
                            cancelPermissionDialog();
                            finish();

                        }
                    })
                    .create();
        }
        mPermissionDialog.show();
    }

    //关闭对话框
    private void cancelPermissionDialog() {
        mPermissionDialog.cancel();
    }


    public void openVideo(View v) {

        OkHttpUtils.get()
                .addParams("mbox-code","0021-0000DF-YDQZZD-000001")
                .url("http://nss.justice.org.cn/notary_test/api/get-setting")
                .build()
                .execute(new StringCallback() {

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        toAty();
                    }

                    @Override
                    public void onResponse(String response, int id, int code) {
                        Map<String, Object> map = JsonUtils.fromTypeJson(response, Map.class);
                        String version = (String) map.get("version");
                        final String uploadUrl = (String) map.get("update-url");
                        String appVersion = QMUIPackageHelper.getAppVersion(MyApp.getInstance());
                        if (appVersion.equals(version)) {
                            toAty();
                            return;
                        }
                        QMUIDialog dialog = new QMUIDialog.MessageDialogBuilder(MainActivity.this)
                                .setMessage("当前版本号:" + appVersion + ",最新版本:" + version + ",是否需要更新?")
                                .setTitle("更新")
                                .addAction("暂不更新", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.cancel();
                                        toAty();
                                    }
                                })
                                .addAction("更新", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        displayLoadingDialog("加载数据中");
                                        File sdPath = FileMeoryUtils.getSDPath();
                                        File dirPath = new File(sdPath, "download");
                                        dirPath.mkdirs();
                                        File appPath = new File(dirPath, "app" + System.currentTimeMillis() + ".apk");
                                        if (appPath.exists()) {
                                            appPath.delete();
                                        }
                                        try {
                                            if (appPath.createNewFile()) {
                                                OkHttpUtils.get().url(uploadUrl)
                                                        .build().execute(new FileCallBack(sdPath.getAbsolutePath(), appPath.getName()) {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        toast("下载失败");
                                                        toAty();
                                                    }

                                                    @Override
                                                    public void onResponse(File response, int id, int code) {
                                                        install(response.getAbsolutePath());
                                                    }

                                                    @Override
                                                    public void onAfter(int id) {
                                                        super.onAfter(id);
                                                        cancelLoadingDialog();
                                                    }
                                                });
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            cancelLoadingDialog();
                                            toast("创建文件失败，无法下载");
                                            toAty();
                                        }

                                        dialog.cancel();
                                    }
                                }).show();
                        dialog.setCancelable(false);
                    }
                });
//        if (true) {
//            startActivity(HomeActivity.class);
//            return;
//        }
        toAty();
    }

    private void install(String filePath) {
        File apkFile = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(
                    this
                    , "你的包名.fileprovider"
                    , apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        startActivity(intent);
    }

    private void toAty() {
        UploadFileUtils.setmobileId(MobileKeyBean.getLast().getMobileKey() + "_");
        // TODO: 2018/11/4
        final CaptureConfiguration config = createCaptureConfiguration();

        final Intent intent = new Intent(this, VideoCaptureActivity.class);
        intent.putExtra(VideoCaptureActivity.EXTRA_CAPTURE_CONFIGURATION, config);
        startActivityForResult(intent, 101);
    }

    private CaptureConfiguration createCaptureConfiguration() {
        final PredefinedCaptureConfigurations.CaptureResolution resolution = getResolution(1);
        final PredefinedCaptureConfigurations.CaptureQuality quality = getQuality(1);

        CaptureConfiguration.Builder builder = new CaptureConfiguration.Builder(resolution, quality);
        builder.showRecordingTime();

        return builder.build();
    }

    /**
     * 品质
     *
     * @param position
     * @return
     */
    private PredefinedCaptureConfigurations.CaptureQuality getQuality(int position) {
        final PredefinedCaptureConfigurations.CaptureQuality[] quality = new PredefinedCaptureConfigurations.CaptureQuality[]{PredefinedCaptureConfigurations.CaptureQuality.HIGH, PredefinedCaptureConfigurations.CaptureQuality.MEDIUM,
                PredefinedCaptureConfigurations.CaptureQuality.LOW};
        return quality[position];
    }

    //分辨率
    private PredefinedCaptureConfigurations.CaptureResolution getResolution(int position) {
        final PredefinedCaptureConfigurations.CaptureResolution[] resolution = new PredefinedCaptureConfigurations.CaptureResolution[]{PredefinedCaptureConfigurations.CaptureResolution.RES_1080P,
                PredefinedCaptureConfigurations.CaptureResolution.RES_720P, PredefinedCaptureConfigurations.CaptureResolution.RES_480P};
        return resolution[position];
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

}
