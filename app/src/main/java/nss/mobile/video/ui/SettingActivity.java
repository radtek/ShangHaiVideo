package nss.mobile.video.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.utils.PreferencesUtils;
import nss.mobile.video.utils.preferences.SettingPreferences;

@BindLayout(layoutRes = R.layout.activity_setting,title = "基本设置")
public class SettingActivity extends BaseActivity {

    @BindView(R.id.setting_auto_time_et)
    EditText autoTimeEt;
    @BindView(R.id.setting_isAuto_switch)
    Switch isAutoSwitch;


    @Override
    public void initWidget() {
        super.initWidget();
        boolean auto = SettingPreferences.isAuto();
        isAutoSwitch.setChecked(auto);
        long autoPlayLong = SettingPreferences.getAutoPlayLong();
        autoTimeEt.setText(String.valueOf(autoPlayLong));

        isAutoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingPreferences.saveIsAuto(isChecked);
            }
        });

        autoTimeEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                long l = Long.parseLong(s.toString());
                if (l<5){
                    toast("时间并不能小于5s");
                    return;
                }
                SettingPreferences.saveAutoPlayLong(l);
            }
        });
    }


}
