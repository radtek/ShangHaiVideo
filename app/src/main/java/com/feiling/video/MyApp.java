package com.feiling.video;

import android.support.multidex.MultiDexApplication;
import android.util.DisplayMetrics;

import com.bumptech.glide.load.model.file_descriptor.FileDescriptorModelLoader;
import com.feiling.video.event.FileMemoryEvent;
import com.feiling.video.utils.FileMeoryUtils;
import com.feiling.video.utils.LogUtils;
import com.shuyu.gsyvideoplayer.utils.FileUtils;

import org.litepal.LitePalApplication;

import java.io.Console;


/**
 * 文件下载地址 HttpDownloadHelper ----Utils
 * http://www.wanandroid.com/blog/show/2080
 * Created by mrqiu on 2017/10/15.
 */
public class MyApp extends LitePalApplication {
    private static MyApp instance;
    private static boolean backCamera;
    private CaseFileMemorySizeThread caseFileMemorySizeThread = new CaseFileMemorySizeThread();


    public static MyApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        //OkHttpInfo.initOkHttpCard(this);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        C.SCREEN_WIDTH = displayMetrics.widthPixels;
        C.SCREEN_HEIGHT = displayMetrics.heightPixels;
        C.SCREEN_HEIGHT_3 = (int) (C.SCREEN_WIDTH * 3.0f / 4);
        C.SCREEN_HEIGHT_9 = (int) (C.SCREEN_WIDTH * 9.0f / 16);

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

    public static class CaseFileMemorySizeThread implements Runnable {

        @Override
        public void run() {
            long availableInternalMemorySize = FileMeoryUtils.getAvailableInternalMemorySize();
            long totalInternalMemorySize = FileMeoryUtils.getTotalInternalMemorySize();
            long l = availableInternalMemorySize * 100 / totalInternalMemorySize;
            if (l <= 50) {
                FileMemoryEvent.getInstance().postMemoryEvent(l);
            }
            LogUtils.i(getClass().getName(), "剩余空间--->" + l);
            C.sHandler.postDelayed(this, 1_000);
        }
    }


}
