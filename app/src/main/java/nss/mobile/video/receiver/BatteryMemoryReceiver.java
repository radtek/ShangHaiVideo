package nss.mobile.video.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/12/16
 *
 * @author ql
 */
public class BatteryMemoryReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        intent.getIntExtra("level", 0);    ///电池剩余电量
        intent.getIntExtra("scale", 0);  ///获取电池满电量数值
        intent.getStringExtra("technology");  ///获取电池技术支持
        intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN); ///获取电池状态
        intent.getIntExtra("plugged", 0);  ///获取电源信息
        intent.getIntExtra("health",BatteryManager.BATTERY_HEALTH_UNKNOWN);  ///获取电池健康度
        intent.getIntExtra("voltage", 0);  ///获取电池电压
        intent.getIntExtra("temperature", 0);  ///获取电池温度
    }
}
