package nss.mobile.video.ui.video;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import org.litepal.LitePal;

import java.util.List;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.bean.db.FtpIp;

@BindLayout(layoutRes = R.layout.activity_ftp_setting, title = "ftp服务器设置")
public class FtpSettingActivity extends BaseActivity {

    @BindView(R.id.ftpSetting_account_et)
    EditText mAccountEt;
    @BindView(R.id.ftpSetting_ip_et)
    EditText mIpEt;
    @BindView(R.id.ftpSetting_port_et)
    EditText mPortEt;
    @BindView(R.id.ftpSetting_pw_et)
    EditText mPwEt;


    @Override
    public void initWidget() {
        super.initWidget();
        mTopBar.addRightTextButton("保存", R.id.top_bar_right_btn).setOnClickListener(this);
        List<FtpIp> all = LitePal.findAll(FtpIp.class);
        if (all.size() != 0) {
            FtpIp ftpIp = all.get(0);
            mAccountEt.setText(ftpIp.getAccount());
            mIpEt.setText(ftpIp.getIp());
            mPortEt.setText(ftpIp.getPort());
            mPwEt.setText(ftpIp.getPw());
        }

    }

    @Override
    public void forbidClick(View v) {
        super.forbidClick(v);
        if (v.getId() == R.id.top_bar_right_btn) {
            if (TextUtils.isEmpty(mIpEt.getText())) {
                displayMessageDialog("请输入ip");
                return;
            }
            if (TextUtils.isEmpty(mPortEt.getText())) {
                displayMessageDialog("请输入端口");
                return;
            }

            String ip = mIpEt.getText().toString();
            String port = mPortEt.getText().toString();
            String account = mAccountEt.getText().toString();
            String pw = mPwEt.getText().toString();

            FtpIp ftpIp = new FtpIp();
            ftpIp.setAccount(account);
            ftpIp.setIp(ip);
            ftpIp.setPw(pw);
            ftpIp.setPort(port);
            List<FtpIp> all = LitePal.findAll(FtpIp.class);
            if (all.size() == 0) {
                ftpIp.save();
            } else {
                ftpIp.update(all.get(0).getId());
            }



            finish();
        }
    }
}
