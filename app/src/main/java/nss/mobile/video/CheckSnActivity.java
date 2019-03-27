package nss.mobile.video;

import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.bean.MobileKeyBean;
import nss.mobile.video.receiver.NetworkStatus;
import nss.mobile.video.ui.wifi.WifiManagerActivity;
import nss.mobile.video.utils.SnCheckHelper;
import nss.mobile.video.utils.preferences.CameraRotationCorrectionPreferences;
import nss.mobile.video.utils.preferences.SettingPreferences;

@BindLayout(layoutRes = R.layout.activity_check_sn, title = "设备id", backRes = 0)
public class CheckSnActivity extends BaseActivity {


    @BindView(R.id.check_sn_input_et)
    EditText mSnInputEt;
    @BindView(R.id.check_sn_action_tv)
    View mSnActionTv;//保存
    @BindView(R.id.check_sn_scanning_action_tv)
    View mSnScanningActionTv;
    @BindView(R.id.check_sn_hint_tv)
    TextView mHintTv;


    @Override
    public void initWidget() {
        super.initWidget();


        s();
        mSnActionTv.setOnClickListener(this);
        mSnScanningActionTv.setOnClickListener(this);
        if (QMUIDisplayHelper.hasInternet(this)) {
            mHintTv.setVisibility(View.GONE);
        }
        mHintTv.setOnClickListener(this);
        EventBus.getDefault().register(this);

        MobileKeyBean last = MobileKeyBean.getLast();
        if (last != null) {

            actionCheckSn(last.getMobileKey());
        }

//        C.sTHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                checkCard();
//            }
//        });
    }

    private void actionCheckSn(String sn) {
        displayLoadingDialog("检测序列号中...");
        SnCheckHelper.checkSn(sn, new SnCheckHelper.OnSnCheckListener() {
            @Override
            public void onSnCheckSuccess(String sn) {
                cancelLoadingDialog();
                startActivity(MainActivity.class);
                finish();
            }

            @Override
            public void onSnCheckFailed(String error) {
                cancelLoadingDialog();
                displayMessageDialog(error);
            }
        });
    }

    @Override
    public void forbidClick(View v) {
        super.forbidClick(v);
        int id = v.getId();
        if (id == mSnActionTv.getId()) {
            actionSaveSn();
        } else if (id == mSnScanningActionTv.getId()) {
            toScanningActivity();
        } else if (id == mHintTv.getId()) {
            startActivity(WifiManagerActivity.class);
        }
    }

    private void actionSaveSn() {
        String sn = mSnInputEt.getText().toString();
        if (TextUtils.isEmpty(sn)) {
            displayMessageDialog("请填入设备id");
            return;
        }

        displayLoadingDialog("进行验证设备id是否正确");
        SnCheckHelper.checkSn(sn, new SnCheckHelper.OnSnCheckListener() {
            @Override
            public void onSnCheckSuccess(String sn) {
                cancelLoadingDialog();
                MobileKeyBean mobileKeyBean = new MobileKeyBean();
                mobileKeyBean.setCreateDate(System.currentTimeMillis());
                mobileKeyBean.setMobileKey(sn);
                mobileKeyBean.save();
                startActivity(MainActivity.class);

            }

            @Override
            public void onSnCheckFailed(String error) {
                cancelLoadingDialog();
                displayMessageDialog(error);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void internetStatus(NetworkStatus networkStatus) {
        if (networkStatus.isConnect()) {
            mHintTv.setVisibility(View.GONE);
        } else {
            mHintTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResultCode(String resultContent, boolean b) {
        super.onResultCode(resultContent, b);
        if (!b) {
            return;
        }
        mSnInputEt.setText(resultContent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    private void s() {
        StringBuffer sb = new StringBuffer();
        sb.append(Build.MANUFACTURER)
                .append("\r\n")
                .append(Build.PRODUCT).append("\r\n")
                .append(Build.BRAND).append("\r\n")
                .append(Build.MODEL).append("\r\n")
                .append(Build.BOARD).append("\r\n")
                .append(Build.DEVICE);

        if ("cfon640_43".equals(Build.PRODUCT)
                && "QUALCOMM".equals(Build.MANUFACTURER)
                && "cfon640_43".equals(Build.DEVICE)
                && "Android".equals(Build.BRAND)
                && "U3_43".equals(Build.MODEL)) {
            SettingPreferences.saveActivityFirst(SettingPreferences.ACTIVITY_CARD);
            CameraRotationCorrectionPreferences.saveRotation(-1);
        }

    }


}
