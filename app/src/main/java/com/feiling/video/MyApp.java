package com.feiling.video;

import android.support.multidex.MultiDexApplication;
import android.util.DisplayMetrics;


/**
 * 文件下载地址 HttpDownloadHelper ----Utils
 * http://www.wanandroid.com/blog/show/2080
 * Created by mrqiu on 2017/10/15.
 */
public class MyApp extends MultiDexApplication {
    private static MyApp instance;
    private static boolean backCamera;


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
        C.SCREEN_HEIGHT_3 = (int)(C.SCREEN_WIDTH * 3.0f / 4);
        C.SCREEN_HEIGHT_9 = (int)(C.SCREEN_WIDTH * 9.0f / 16);
    }

    public static boolean getBackCamera() {
        return backCamera;
    }
    public static void setBackCamera(boolean backCamera) {
        MyApp.backCamera = backCamera;
    }


}
