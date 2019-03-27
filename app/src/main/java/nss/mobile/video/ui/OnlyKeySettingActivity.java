package nss.mobile.video.ui;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.function.BiFunction;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.bean.MobileKeyBean;
import nss.mobile.video.service.UploadFileUtils;
import nss.mobile.video.utils.SnCheckHelper;

@BindLayout(layoutRes = R.layout.activity_only_key_setting, title = "设备id设置")
public class OnlyKeySettingActivity extends BaseActivity {

    @BindView(R.id.key_setting_key_et)
    EditText mKeyEt;
    @BindView(R.id.key_setting_pw_et)
    EditText mPwEt;
    @BindView(R.id.key_setting_save_action_tv)
    TextView mSaveActionTv;
    @BindView(R.id.key_setting_scanning_action_tv)
    TextView mScanningActionTv;


    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void initWidget() {
        super.initWidget();
        mScanningActionTv.setOnClickListener(this);
        mSaveActionTv.setOnClickListener(this);
        MobileKeyBean last = MobileKeyBean.getLast();
        if (last != null) {
            mKeyEt.setText(last.getMobileKey());
        }

    }

    @Override
    public void forbidClick(View v) {
        super.forbidClick(v);
        if (v.getId() == mScanningActionTv.getId()) {
            toScanningActivity();
        } else if (v.getId() == mSaveActionTv.getId()) {
            String key = mKeyEt.getText().toString().trim();
            String pw = mPwEt.getText().toString().trim();
            if (TextUtils.isEmpty(key)) {
                displayMessageDialog("请输入ID");
                return;
            }
            if (TextUtils.isEmpty(pw)) {
                displayMessageDialog("请输入密码");
                return;
            }
            Calendar calendar = Calendar.getInstance();
//获取系统的日期
//年
            int year = calendar.get(Calendar.YEAR);
//月
            int month = calendar.get(Calendar.MONTH) + 1;
//日
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            calendar.clear();
            calendar.set(year, month, day);
            long timeInMillis = calendar.getTimeInMillis();
            String s = md5(String.valueOf(timeInMillis));
            String substring = s.substring(s.length() - 6, s.length());
            if (!substring.equals(pw)) {
                displayMessageDialog("密码不正确");
                return;
            }
            displayLoadingDialog("检测设备号中...");
            SnCheckHelper.checkSn(key, new SnCheckHelper.OnSnCheckListener() {
                @Override
                public void onSnCheckSuccess(String sn) {
                    MobileKeyBean k = new MobileKeyBean();
                    k.setMobileKey(key);
                    k.setCreateDate(System.currentTimeMillis());
                    k.save();
                    UploadFileUtils.setmobileId(key + "_");
                    displayMessageDialog("设置成功");
                    cancelLoadingDialog();
                }

                @Override
                public void onSnCheckFailed(String error) {
                    cancelLoadingDialog();
                    displayMessageDialog(error);
                }
            });


        }
    }

    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onResultCode(String resultContent, boolean b) {
        if (!b) {
            return;
        }
        new QMUIDialog.MessageDialogBuilder(this)
                .setTitle("提示")
                .setMessage("扫描结果为:" + resultContent + "\r\n确定使用吗?")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.cancel();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        mKeyEt.setText(resultContent);
                        dialog.cancel();
                    }
                })
                .show();
    }
}
