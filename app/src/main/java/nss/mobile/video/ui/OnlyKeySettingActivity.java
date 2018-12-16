package nss.mobile.video.ui;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.bean.MobileKeyBean;
import nss.mobile.video.service.UploadFileUtils;

@BindLayout(layoutRes = R.layout.activity_only_key_setting, title = "设备id设置")
public class OnlyKeySettingActivity extends BaseActivity {

    @BindView(R.id.key_setting_key_et)
    EditText mKeyEt;
    @BindView(R.id.key_setting_pw_et)
    EditText mPwEt;
    @BindView(R.id.key_setting_save_action_tv)
    TextView mSaveActionTv;


    @Override
    public void initWidget() {
        super.initWidget();
        mSaveActionTv.setOnClickListener(this);
        MobileKeyBean last = MobileKeyBean.getLast();
        mKeyEt.setText(last.getMobileKey());

    }

    @Override
    public void forbidClick(View v) {
        super.forbidClick(v);
        if (v.getId() == mSaveActionTv.getId()) {
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

            MobileKeyBean k = new MobileKeyBean();
            k.setMobileKey(key);
            k.setCreateDate(System.currentTimeMillis());
            k.save();
            UploadFileUtils.setmobileId(key + "_");
            toast("保存成功");

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
}
