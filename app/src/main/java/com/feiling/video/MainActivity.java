package com.feiling.video;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.feiling.video.base.BaseActivity;
import com.feiling.video.base.BindLayout;
import com.feiling.video.ui.VideoPlayActivity;
import com.feiling.video.utils.LogUtils;
import com.feiling.video.video.VideoCaptureActivity;
import com.feiling.video.video.configuration.CaptureConfiguration;
import com.feiling.video.video.configuration.PredefinedCaptureConfigurations;

import java.util.ArrayList;
import java.util.List;


@BindLayout(layoutRes = R.layout.activity_main)
public class MainActivity extends BaseActivity {
    public static final int REQUEST_CODE_ASK_CAMERA = 123;

    //申请两个权限，录音和文件读写
    //1、首先声明一个数组permissions，将需要的权限都放在里面
    String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //2、创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
    List<String> mPermissionList = new ArrayList<>();
    /**
     * 不再提示权限时的展示对话框
     */
    AlertDialog mPermissionDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        }else{
            //说明权限都已经通过，可以做你想做的事情去
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
            }else{
                //全部权限通过，可以进行下一步操作。。。

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

    public void seeVideo(View v){
        startActivity(VideoPlayActivity.class);
    }

    public void openVideo(View v) {
        final CaptureConfiguration config = createCaptureConfiguration();
        final String filename = getFilesDir().getAbsolutePath()+"/"+System.currentTimeMillis() +".mp4";

        final Intent intent = new Intent(this, VideoCaptureActivity.class);
        intent.putExtra(VideoCaptureActivity.EXTRA_CAPTURE_CONFIGURATION, config);
        intent.putExtra(VideoCaptureActivity.EXTRA_OUTPUT_FILENAME, filename);
        startActivityForResult(intent, 101);
    }

    private CaptureConfiguration createCaptureConfiguration() {
        final PredefinedCaptureConfigurations.CaptureResolution resolution = getResolution(1);
        final PredefinedCaptureConfigurations.CaptureQuality quality = getQuality(1);

        CaptureConfiguration.Builder builder = new CaptureConfiguration.Builder(resolution, quality);
        //maxDurationSecs为录制的最长时间，单位为秒。
        // maxFilesizeMb为录制的最大大小，单位为M。
        // showTimer为是否显示录制计时器。
        // allowFrontFacingCamera为是否允许前置摄像头切换。
//        try {
//            int maxDuration = Integer.valueOf(maxDurationEt.getEditableText().toString());
//            builder.maxDuration(maxDuration);
//        } catch (final Exception e) {
//            //NOP
//        }
//        try {
//            int maxFileSize = Integer.valueOf(maxFilesizeEt.getEditableText().toString());
//            builder.maxFileSize(maxFileSize);
//        } catch (final Exception e) {
//            //NOP
//        }
//        try {
//            int fps = Integer.valueOf(fpsEt.getEditableText().toString());
//            builder.frameRate(fps);
//        } catch (final Exception e) {
//            //NOP
//        }
        builder.showRecordingTime();
//        builder.noCameraToggle();

        return builder.build();
    }

    private PredefinedCaptureConfigurations.CaptureQuality getQuality(int position) {
        final PredefinedCaptureConfigurations.CaptureQuality[] quality = new PredefinedCaptureConfigurations.CaptureQuality[]{PredefinedCaptureConfigurations.CaptureQuality.HIGH, PredefinedCaptureConfigurations.CaptureQuality.MEDIUM,
                PredefinedCaptureConfigurations.CaptureQuality.LOW};
        return quality[position];
    }

    private PredefinedCaptureConfigurations.CaptureResolution getResolution(int position) {
        final PredefinedCaptureConfigurations.CaptureResolution[] resolution = new PredefinedCaptureConfigurations.CaptureResolution[]{PredefinedCaptureConfigurations.CaptureResolution.RES_1080P,
                PredefinedCaptureConfigurations.CaptureResolution.RES_720P, PredefinedCaptureConfigurations.CaptureResolution.RES_480P};
        return resolution[position];
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        String stringExtra = data.getStringExtra(VideoCaptureActivity.EXTRA_OUTPUT_FILENAME);
        LogUtils.i(getClass().getName(),stringExtra);
    }
}
