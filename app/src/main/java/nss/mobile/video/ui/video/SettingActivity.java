package nss.mobile.video.ui.video;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.bean.MobileKeyBean;
import nss.mobile.video.ui.SnActivity;
import nss.mobile.video.utils.SnCheckHelper;
import nss.mobile.video.utils.preferences.SettingPreferences;

@BindLayout(layoutRes = R.layout.activity_setting, title = "基本设置")
public class SettingActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.setting_auto_time_et)
    EditText autoTimeEt;
    @BindView(R.id.setting_isAuto_switch)
    Switch isAutoSwitch;
    @BindView(R.id.setting_save_tv)
    TextView saveTv;
    @BindView(R.id.setting_aty_group)
    ViewGroup mAtyGroup;
    @BindView(R.id.setting_aty_first_tv)
    TextView mAtyFirstTV;


    private int mAtyFirstTag;


    @Override
    public void initWidget() {
        super.initWidget();
        boolean auto = SettingPreferences.isAuto();
        isAutoSwitch.setChecked(auto);
        long autoPlayLong = SettingPreferences.getAutoPlayLong();
        autoTimeEt.setText(String.valueOf(autoPlayLong));

        saveTv.setOnClickListener(this);
        mAtyGroup.setOnClickListener(this);

        isAutoSwitch.setOnCheckedChangeListener(this);

        int activityFirst = SettingPreferences.getActivityFirst();
        mAtyFirstTag = activityFirst;
        setFirstAtyName(activityFirst);

        checkSn();
    }

    private void checkSn() {
        displayLoadingDialog("检测设备序列号中");
        MobileKeyBean last = MobileKeyBean.getLast();
        String mobileKey = last.getMobileKey();
        SnCheckHelper.checkSn(mobileKey, new SnCheckHelper.OnSnCheckListener() {
            @Override
            public void onSnCheckSuccess(String sn) {
                cancelLoadingDialog();
            }

            @Override
            public void onSnCheckFailed(String error) {
                cancelLoadingDialog();
                if (isFinishing()) {
                    return;
                }
                QMUIDialog show = new QMUIDialog.MessageDialogBuilder(SettingActivity.this)
                        .setTitle("提示")
                        .setMessage(error)
                        .addAction("确定", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                finish();
                                dialog.cancel();
                            }
                        })
                        .show();
                show.setCancelable(false);
            }
        });
    }

    private void setFirstAtyName(int activityFirst) {
        if (activityFirst == 0) {
            mAtyFirstTV.setText("视频录像");
        } else {
            mAtyFirstTV.setText("身份认证");
        }
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

            SettingPreferences.saveActivityFirst(mAtyFirstTag);
            boolean checked = isAutoSwitch.isChecked();
            SettingPreferences.saveIsAuto(checked);
            finish();
        } else if (id == mAtyGroup.getId()) {
            QMUIBottomSheet bottomSheet = new QMUIBottomSheet.BottomListSheetBuilder(this)
                    .addItem("视频录像", String.valueOf(SettingPreferences.ACTIVITY_VIDEO))
                    .addItem("身份认证", String.valueOf(SettingPreferences.ACTIVITY_CARD))
                    .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                        @Override
                        public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                            mAtyFirstTag = Integer.parseInt(tag);
                            setFirstAtyName(mAtyFirstTag);
                            displayMessageDialog("如切换启动页功能,请重新启动程序");
                            dialog.cancel();
                        }
                    }).build();
            bottomSheet.show();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SettingPreferences.saveIsAuto(isChecked);
    }
}
