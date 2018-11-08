package nss.mobile.video.ui;

import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.utils.preferences.SettingPreferences;

@BindLayout(layoutRes = R.layout.activity_setting, title = "基本设置")
public class SettingActivity extends BaseActivity {

    @BindView(R.id.setting_auto_time_et)
    EditText autoTimeEt;
    @BindView(R.id.setting_isAuto_switch)
    Switch isAutoSwitch;
    @BindView(R.id.setting_save_tv)
    TextView saveTv;


    @Override
    public void initWidget() {
        super.initWidget();
        boolean auto = SettingPreferences.isAuto();
        isAutoSwitch.setChecked(auto);
        long autoPlayLong = SettingPreferences.getAutoPlayLong();
        autoTimeEt.setText(String.valueOf(autoPlayLong));

        saveTv.setOnClickListener(this);
    }


    @Override
    public void forbidClick(View v) {
        super.forbidClick(v);
        int id = v.getId();
        if (id == saveTv.getId()) {
            long l = Long.parseLong(autoTimeEt.getText().toString());
            if (l < 5) {
                displayMessageDialog("时间并不能小于5s");
                return;
            }
            SettingPreferences.saveAutoPlayLong(l);

            boolean checked = isAutoSwitch.isChecked();
            SettingPreferences.saveIsAuto(checked);
            finish();
        }
    }
}
