package nss.mobile.video.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("网络状态发生变化");
        //检测API是不是小于23，因为到了API23之后getNetworkInfo(int networkType)方法被弃用
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {

            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            //获取ConnectivityManager对象对应的NetworkInfo对象
            //获取WIFI连接的信息
            NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //获取移动数据连接的信息
            NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiNetworkInfo.isConnected() || dataNetworkInfo.isConnected()) {
//                Toast.makeText(context, "WIFI已连接,移动数据已连接", Toast.LENGTH_SHORT).show();
                connectNetwork(context);
            } else {
                dismissNetwork(context);
            }
//API大于23时使用下面的方式进行网络监听
        } else {

            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            //获取所有网络连接的信息
            Network[] networks = connMgr.getAllNetworks();
            //通过循环将网络信息逐个取出来
            boolean connect = false;
            for (int i = 0; i < networks.length; i++) {
                //获取ConnectivityManager对象对应的NetworkInfo对象
                NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                if (networkInfo.isConnected()) {
                    connectNetwork(context);
                    connect = true;
                    break;
                }
            }

            if (connect) {
                return;
            }
            dismissNetwork(context);
        }


    }
    private NetworkStatus networkStatus = new NetworkStatus();
    private void dismissNetwork(Context context) {
        networkStatus.setConnect(false);
        networkStatus.setTime(System.currentTimeMillis());
        EventBus.getDefault().post(networkStatus);
    }

    private void connectNetwork(Context context) {
        networkStatus.setTime(System.currentTimeMillis());
        networkStatus.setConnect(true);
        EventBus.getDefault().post(networkStatus);
    }
}
