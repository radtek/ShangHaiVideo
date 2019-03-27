package nss.mobile.video.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.ViewGroup;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

import java.util.ArrayList;
import java.util.List;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BaseFragment;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.ui.fragment.CardFragment;
import nss.mobile.video.ui.video.AllFunctionActivity;
import nss.mobile.video.video.VideoCaptureActivity;
import nss.mobile.video.video.configuration.CaptureConfiguration;
import nss.mobile.video.video.configuration.PredefinedCaptureConfigurations;

@BindLayout(layoutRes = R.layout.activity_home, bindTopBar = false, addStatusBar = true)
public class HomeActivity extends BaseActivity {
    public static final int R_SELECT_WAREHOUSE = 2;
    @BindView(R.id.activity_home_nav_card)
    ViewGroup navCard;//首页
    @BindView(R.id.activity_home_nav_get)
    ViewGroup navGet;//视频功能
    @BindView(R.id.activity_home_nav_all)
    ViewGroup navAll;//全部功能
    @BindView(R.id.v_bar)
    View vBar;


    private List<BaseFragment> fragments = new ArrayList<>();
    private ViewGroup lastNav;

    @Override
    public void initStatusBar() {
        QMUIStatusBarHelper.translucent(this);
        QMUIStatusBarHelper.setStatusBarDarkMode(this);
    }

    @Override
    public void initData() {
        super.initData();
//        fragments.add(new DevFragment());
        fragments.add(new CardFragment());
    }

    @Override
    public void initWidget() {
        super.initWidget();
//        navDev.setTag(0);
//        navDev.setOnClickListener(this);
        navCard.setTag(0);
        navCard.setOnClickListener(this);
        navGet.setTag(1);
        navGet.setOnClickListener(this);
        navAll.setTag(2);
        navAll.setOnClickListener(this);
        myChangeFragment(R.id.function_fl_content, fragments.get(0));
        setNavSelect(navCard);
        lastNav = navCard;
        ViewGroup.LayoutParams layoutParams = vBar.getLayoutParams();
        layoutParams.height = QMUIDisplayHelper.getStatusBarHeight(this);


    }

    @Override
    public void forbidClick(View v) {
        super.forbidClick(v);
        switch (v.getId()) {
            case R.id.activity_home_nav_index:
            case R.id.activity_home_nav_get:
            case R.id.activity_home_nav_card:
            case R.id.activity_home_nav_all:
                if (v.equals(lastNav)) {
                    return;
                }
                setNavNormal(lastNav);
                int tag = (int) v.getTag();
                if (tag == 1) {
                    final CaptureConfiguration config = createCaptureConfiguration();

                    final Intent intent = new Intent(this, VideoCaptureActivity.class);
                    intent.putExtra(VideoCaptureActivity.EXTRA_CAPTURE_CONFIGURATION, config);
                    startActivityForResult(intent, 101);
                    return;
                } else if (tag == 2) {
                    startActivity(AllFunctionActivity.class);
                    return;
                }
                lastNav = (ViewGroup) v;
                setNavSelect(lastNav);
                myChangeFragment(R.id.function_fl_content, fragments.get(tag));

                break;
        }
    }

    private CaptureConfiguration createCaptureConfiguration() {
        final PredefinedCaptureConfigurations.CaptureResolution resolution = getResolution(1);
        final PredefinedCaptureConfigurations.CaptureQuality quality = getQuality(1);

        CaptureConfiguration.Builder builder = new CaptureConfiguration.Builder(resolution, quality);
        builder.showRecordingTime();

        return builder.build();
    }

    /**
     * 品质
     *
     * @param position
     * @return
     */
    private PredefinedCaptureConfigurations.CaptureQuality getQuality(int position) {
        final PredefinedCaptureConfigurations.CaptureQuality[] quality = new PredefinedCaptureConfigurations.CaptureQuality[]{PredefinedCaptureConfigurations.CaptureQuality.HIGH, PredefinedCaptureConfigurations.CaptureQuality.MEDIUM,
                PredefinedCaptureConfigurations.CaptureQuality.LOW};
        return quality[position];
    }

    //分辨率
    private PredefinedCaptureConfigurations.CaptureResolution getResolution(int position) {
        final PredefinedCaptureConfigurations.CaptureResolution[] resolution = new PredefinedCaptureConfigurations.CaptureResolution[]{PredefinedCaptureConfigurations.CaptureResolution.RES_1080P,
                PredefinedCaptureConfigurations.CaptureResolution.RES_720P, PredefinedCaptureConfigurations.CaptureResolution.RES_480P};
        return resolution[position];
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void setNavNormal(ViewGroup vp) {
        vp.setSelected(false);
    }

    private void setNavSelect(ViewGroup vp) {
        vp.setSelected(true);
    }


}
