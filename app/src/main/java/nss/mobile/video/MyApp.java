package nss.mobile.video;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.qiniu.pili.droid.streaming.StreamingEnv;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePalApplication;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nss.mobile.video.bean.AliFileBean;
import nss.mobile.video.bean.MemoryBean;
import nss.mobile.video.bean.MobileKeyBean;
import nss.mobile.video.event.FileMemoryEvent;
import nss.mobile.video.http.OkHttpHelper;
import nss.mobile.video.http.ali.AliOssToken;
import nss.mobile.video.info.UrlApi;
import nss.mobile.video.receiver.NetworkStatus;
import nss.mobile.video.service.ali.AliUploadFileService;
import nss.mobile.video.utils.FileMeoryUtils;
import nss.mobile.video.utils.LocationUtils;
import nss.mobile.video.utils.UnitHelper;
import nss.mobile.video.video.VideoFile;
import okhttp3.Call;


/**
 * 文件下载地址 HttpDownloadHelper ----Utils
 * http://www.wanandroid.com/blog/show/2080
 * Created by mrqiu on 2017/10/15.
 */
public class MyApp extends LitePalApplication implements ServiceConnection {
    private static MyApp instance;
    private static boolean backCamera;
    private CaseFileMemorySizeThread caseFileMemorySizeThread = new CaseFileMemorySizeThread();
    private AliUploadFileService.UploadFileBinder mUploadFileBinder;
    public static boolean isVideo = false;

    public static MyApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        instance = this;
        StreamingEnv.init(getApplicationContext());
        //OkHttpInfo.initOkHttpCard(this);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        C.SCREEN_WIDTH = displayMetrics.widthPixels;
        C.SCREEN_HEIGHT = displayMetrics.heightPixels;
        C.SCREEN_HEIGHT_3 = (int) (C.SCREEN_WIDTH * 3.0f / 4);
        C.SCREEN_HEIGHT_9 = (int) (C.SCREEN_WIDTH * 9.0f / 16);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                C.sTHandler = new Handler();

                Looper.loop();

            }
        }).start();
        Intent intent = new Intent(this, AliUploadFileService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        EventBus.getDefault().register(this);
    }

    private long lastTime;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void uploadFile(NetworkStatus networkStatus) {

        long time = networkStatus.getTime();
        if (time - lastTime < 1000) {
            return;
        }
        lastTime = time;
        if (networkStatus.isConnect()) {
            C.sTHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mUploadFileBinder == null) {
                        C.sTHandler.postDelayed(this, 2_000);
                        return;
                    }
                    List<File> list = loadFile();
                    for (File file : list) {
                        AliFileBean aliBean = AliFileBean.getFile(file.getAbsolutePath());
                        String upStatus = aliBean == null ? null : aliBean.getUpStatus().toString();
                        if (upStatus == null
                                || AliFileBean.STATUS_UPLOADING.equals(upStatus)
                                || AliFileBean.STATUS_WANTING.equals(upStatus)
                                || AliFileBean.STATUS_NORMAL.equals(upStatus)
                                || AliFileBean.STATUS_ERROR.equals(upStatus)) {
                            mUploadFileBinder.addFile(file);
                        }
                    }

                    mUploadFileBinder.startUploadFile();
                    Toast.makeText(MyApp.this, "开始上传文件了", Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

    private List loadFile() {
        File file = VideoFile.baseFile();
        File[] files = file.listFiles();
        return Arrays.asList(files);
    }

    public void startCaseFileMemoryThread() {
        C.sHandler.post(caseFileMemorySizeThread);
    }

    public void stopCaseFileMemoryThread() {
        C.sHandler.removeCallbacks(caseFileMemorySizeThread);
    }

    public static boolean getBackCamera() {
        return backCamera;
    }

    public static void setBackCamera(boolean backCamera) {
        MyApp.backCamera = backCamera;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mUploadFileBinder = (AliUploadFileService.UploadFileBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    public static class CaseFileMemorySizeThread implements Runnable {

        @Override
        public void run() {

            MemoryBean memoryBean = getSDMemoery();
            FileMemoryEvent.getInstance().postMemoryEvent(memoryBean);
            C.sHandler.postDelayed(this, 1_000);
        }


    }

    public static MemoryBean getSDMemoery() {
        String extendedMemoryPath = FileMeoryUtils.getExtendedMemoryPath(getContext());
        MemoryBean memoryBean = new MemoryBean();
        if (extendedMemoryPath == null) {
            long availableInternalMemorySize = FileMeoryUtils.getAvailableInternalMemorySize();
            long totalInternalMemorySize = FileMeoryUtils.getTotalInternalMemorySize();
            memoryBean.setAvailableInternalMemorySize(availableInternalMemorySize);
            memoryBean.setTotalInternalMemorySize(totalInternalMemorySize);
        } else {
            long sdTotalSize = FileMeoryUtils.getSDTotalSize(extendedMemoryPath);
            long sdMomery = FileMeoryUtils.getSDAvailableSize(extendedMemoryPath);
            memoryBean.setAvailableInternalMemorySize(sdMomery);
            memoryBean.setTotalInternalMemorySize(sdTotalSize);
        }
        return memoryBean;
    }

    public static class SendStatusRunnable implements Runnable {
        Map<String, Object> params = new HashMap<>();
        int delayMillis = 5 * 60 * 1000;
        StringCallback callback = new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id, int code) {

            }
        };

        @Override
        public void run() {
            C.sTHandler.postDelayed(this, delayMillis);
            if (!QMUIDisplayHelper.hasInternet(MyApp.getInstance())) {
                return;
            }

            resetParams();
            OkHttpHelper.post(UrlApi.status_submit, params, callback);


        }

        private void resetParams() {
            params.clear();

            MemoryBean memoryBean = getSDMemoery();
            params.put("box-code", MobileKeyBean.getLast().getMobileKey());
            params.put("battery", MyApp.getInstance().getBatteryMemory());
            params.put("total-space", memoryBean.getTotalInternalMemorySize());
            params.put("available", memoryBean.getAvailableInternalMemorySize());
            params.put("status", MyApp.isVideo ? "录制中" : "未录制");
            params.put("time", System.currentTimeMillis());
            Location location = MyApp.getInstance().getLocation();
            if (location != null) {
                params.put("longitude", String.valueOf(location.getLongitude()));
                params.put("latitude", String.valueOf(location.getLatitude()));
            }

        }
    }

    public int getBatteryMemory() {
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = registerReceiver(null, filter2);
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int batteryPct = level * 100 / scale;
        return batteryPct;
    }


    public Location getLocation() {
        return LocationUtils.getLngAndLatWithNetwork(this);
    }

}
