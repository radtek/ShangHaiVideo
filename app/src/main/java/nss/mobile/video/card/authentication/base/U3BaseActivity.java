package nss.mobile.video.card.authentication.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

import android_serialport_api.SerialPortManager;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.card.authentication.aratek.SwitchUtil;


/**
 * 时间：2018/12/5
 * 描述：Activity基类，息屏亮屏上下电，集成了ActivityCollector，Home键销毁app进程并下电
 *
 * @author Administrator
 */
public abstract class U3BaseActivity extends BaseActivity {

    private static final String TAG = "BaseActivity";

    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
    private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";
    private HomeReceiver receiver;

    private boolean ISAPPS = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "------onCreate------");

        receiver = new HomeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, intentFilter);
        ActivityCollector.addActivity(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "------onResume------");
        ISAPPS = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "------onPause------");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "------onDestroy------");

        unregisterReceiver(receiver);
        ActivityCollector.removeActivity(this);
    }


    public class HomeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive: action: " + action);
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                // android.intent.action.CLOSE_SYSTEM_DIALOGS
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                Log.i(TAG, "reason: " + reason);

                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    // 短按Home键
                    if (!ISAPPS) {
                        ISAPPS = false;
                        Log.i(TAG, "homekey");

                        try {
                            SerialPortManager.getInstance().setDownGpioSTM32();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //指纹模块切换到正常模式
                        SwitchUtil.getInstance().closeUSB();
                        finishAffinity();
                        ActivityCollector.finishAll();
                        System.exit(0);
                    }


                } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    // 任务键
                    ISAPPS = true;
                    Log.i(TAG, "apps");

                } else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
                    // 锁屏
                    Log.i(TAG, "lock");
                } else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
                    // samsung 长按Home键
                    Log.i(TAG, "assist");
                }

            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                Log.i(TAG, "亮屏了");
                try {
                    SerialPortManager.getInstance().setUpGpioSTM32();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                Log.i(TAG, "息屏了");
                try {
                    SerialPortManager.getInstance().setDownGpioSTM32();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }


}
