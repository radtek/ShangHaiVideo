package nss.mobile.video.ui.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.utils.LogUtils;

@BindLayout(layoutRes = R.layout.activity_wifi_manager, title = "wifi管理")
public class WifiManagerActivity extends BaseActivity {
    private static final String fir = "WIFI:";

    public static final String WIFI = "wifi";
    @BindView(R.id.wifiManager_status_hint_tv)
    TextView mWifiStatusHintTv;
    @BindView(R.id.wifiManager_toScanning_group)
    ViewGroup mToScannningGroup;
    private WifiManager mWifiManager;
    private WifiBroadCastReceiver wifiBroadCastReceiver;
    private ConnectivityManager mConnectivityManager;


    @Override
    public void initWidget() {
        super.initWidget();
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiBroadCastReceiver = new WifiBroadCastReceiver();

        mToScannningGroup.setOnClickListener(this);

        IntentFilter i = new IntentFilter();
        i.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        i.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        i.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(wifiBroadCastReceiver, i);
        String s = isConnectWifi() ? "已开启wifi" : "未开启wifi";
        mWifiStatusHintTv.setText(s);
        toScanningActivity();
    }

    @Override
    public void forbidClick(View v) {
        super.forbidClick(v);
        if (v.getId() == mToScannningGroup.getId()) {
            toScanningActivity();
        }
    }

    @Override
    public void onResultCode(String resultContent, boolean b) {
        if (!b) {
//            finish();
            return;
        }
        connectWifi(resultContent);
    }

    private void connectWifi(String wifi) {
        if (wifi == null) {
            mWifiStatusHintTv.setText("获得信息失败");
            return;
        }
        AccessPoint wifiPoint = createWifiPoint(wifi);
        if (wifiPoint == null) {
            mWifiStatusHintTv.setText("获得信息不正确，请重新扫描");
            return;
        }

        connectWifi(wifiPoint);
    }

    private AccessPoint createWifiPoint(String wifi) {
        //WIFI:T:WPA;S:TP-LINK_A5A2;P:801234567;;
        if (!wifi.startsWith(fir)) {
            return null;
        }
        String substring = wifi.substring(fir.length(), wifi.length());

        String[] split = substring.split(";");
        if (split.length == 0) {
            return null;
        }
        AccessPoint ap = new AccessPoint();
        for (String s : split) {
            String[] split1 = s.split(":");
            if (split1.length == 0) {
                return null;
            }
            String label = split1[0].toLowerCase();
            String value = split1[1];
            if ("t".equals(label)) {
                ap.setEncryptionType(value);
            } else if ("s".equals(label)) {
                ap.setSsid(value);
            } else if ("p".equals(label)) {
                ap.setPassword(value);
            }
        }
        return ap;
    }


    private void connectWifi(AccessPoint wifiPoint) {
        //WIFI:T:WPA;S:TP-LINK_A5A2;P:801234567;;
        WifiConfiguration config = createConfiguration(wifiPoint);
        //如果你设置的wifi是设备已经存储过的，那么这个networkId会返回小于0的值。
        int networkId = mWifiManager.addNetwork(config);
        mWifiManager.enableNetwork(networkId, true);
    }


    private boolean isConnectWifi() {
        NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo.isConnected();
    }


    public WifiConfiguration createConfiguration(AccessPoint ap) {
        String SSID = ap.getSsid();
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + SSID + "\"";

        String encryptionType = ap.getEncryptionType();
        String password = ap.getPassword();
        if (encryptionType.contains("wep")) {
            /**
             * special handling according to password length is a must for wep
             */
            int i = password.length();
            if (((i == 10 || (i == 26) || (i == 58))) && (password.matches("[0-9A-Fa-f]*"))) {
                config.wepKeys[0] = password;
            } else {
                config.wepKeys[0] = "\"" + password + "\"";
            }
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (encryptionType.toLowerCase().contains("wpa")) {
            config.preSharedKey = "\"" + password + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        return config;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(wifiBroadCastReceiver);
        super.onDestroy();
    }

    class WifiBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case WifiManager.WIFI_STATE_CHANGED_ACTION://wifi开关变化通知
                    LogUtils.i(getClass().getName(), "0");
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                            WifiManager.WIFI_STATE_DISABLED);

                    switch (wifiState) {
                        case WifiManager.WIFI_STATE_DISABLING://|wifi正在关闭
                            mWifiStatusHintTv.setText("wifi正在关闭");
                            break;
                        case WifiManager.WIFI_STATE_DISABLED://wifi关闭
                            mWifiStatusHintTv.setText("wifi已关闭");
                            break;
                        case WifiManager.WIFI_STATE_ENABLED://wifi开启
//                            mWifiStatusHintTv.setText(R.string.wifi_connect_loading);
                            mWifiStatusHintTv.setText("wifi已开启");
                            // TODO: 2018/11/4
                            break;
                        case WifiManager.WIFI_STATE_ENABLING://wifi开启中;
                            mWifiStatusHintTv.setText("wifi开启中");
                            break;
                    }
                    break;
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION://wifi扫描结果通知
                    break;
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION://wifi连接结果通知
                    break;
                case ConnectivityManager.CONNECTIVITY_ACTION://
                    // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
                    //获取联网状态的NetworkInfo对象
                    /*NetworkInfo info = intent
                            .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                    if (info != null) {
                        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                            if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                                mWifiStatusHintTv.setText(R.string.wifi_connect_success);
                            } else {
                                if (isConnectWifi()) {
                                    mWifiStatusHintTv.setText("wifi已关闭");
                                } else {
                                    mWifiStatusHintTv.setText(R.string.wifi_connect_failed);
                                }
                            }
                        }
                    } else {
                        mWifiStatusHintTv.setText(R.string.wifi_connect_failed);
                    }*/

                    break;
                case WifiManager.NETWORK_STATE_CHANGED_ACTION://网络状态

                    Parcelable parcelableExtra = intent
                            .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (null != parcelableExtra) {
                        // 获取联网状态的NetWorkInfo对象
                        NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                        //获取的State对象则代表着连接成功与否等状态
                        NetworkInfo.State state = networkInfo.getState();
                        //判断网络是否已经连接
                        boolean isConnected = state == NetworkInfo.State.CONNECTED;
                        mWifiStatusHintTv.setText(isConnected ? "网络已连接" : "网络断开");
                    }

                    break;
            }
        }
    }

    private class AccessPoint {
        private String ssid;
        private String encryptionType;
        private String password;


        public void setSsid(String ssid) {
            this.ssid = ssid;
        }

        public void setEncryptionType(String encryptionType) {
            this.encryptionType = encryptionType;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getSsid() {
            return ssid;
        }

        public String getEncryptionType() {
            return encryptionType;
        }

        public String getPassword() {
            return password;
        }
    }
}
