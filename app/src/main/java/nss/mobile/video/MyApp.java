package nss.mobile.video;

import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;

import com.qiniu.pili.droid.streaming.StreamingEnv;

import org.litepal.LitePalApplication;

import nss.mobile.video.bean.MemoryBean;
import nss.mobile.video.event.FileMemoryEvent;
import nss.mobile.video.utils.FileMeoryUtils;


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
        MultiDex.install(this);
        instance = this;
//        StreamingEnv.init(getApplicationContext());
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
        private MemoryBean memoryBean = new MemoryBean();

        @Override
        public void run() {
            String extendedMemoryPath = FileMeoryUtils.getExtendedMemoryPath(getContext());
            if (extendedMemoryPath == null) {
                long availableInternalMemorySize = FileMeoryUtils.getAvailableInternalMemorySize();
                long totalInternalMemorySize = FileMeoryUtils.getTotalInternalMemorySize();
                memoryBean.setAvailableInternalMemorySize(availableInternalMemorySize);
                memoryBean.setTotalInternalMemorySize(totalInternalMemorySize);
                FileMemoryEvent.getInstance().postMemoryEvent(memoryBean);
                C.sHandler.postDelayed(this, 1_000);
                return;
            }


            long sdTotalSize = FileMeoryUtils.getSDTotalSize(extendedMemoryPath);
            long sdMomery = FileMeoryUtils.getSDAvailableSize(extendedMemoryPath);
            memoryBean.setAvailableInternalMemorySize(sdMomery);
            memoryBean.setTotalInternalMemorySize(sdTotalSize);
            FileMemoryEvent.getInstance().postMemoryEvent(memoryBean);
            C.sHandler.postDelayed(this, 1_000);
        }


    }


}
