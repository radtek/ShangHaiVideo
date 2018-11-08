package nss.mobile.video.video;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.utils.div.DividerItemDecoration;
import nss.mobile.video.utils.preferences.QualityPreferences;
import nss.mobile.video.video.configuration.CaptureConfiguration;
import nss.mobile.video.video.configuration.PredefinedCaptureConfigurations;

@BindLayout(layoutRes = R.layout.activity_quality_select, title = "录像质量")
public class QualitySelectActivity extends BaseActivity {

    public static final String CAPTURE_CONNFIG = "keyCapture";

    @BindView(R.id.quality_quality_rv)
    RecyclerView mQualityRv;
    @BindView(R.id.quality_Resolution_rv)
    RecyclerView mResolutionRv;


    private QualityAdapter mQualityAdapter = new QualityAdapter();
    private QualityAdapter mResolutionAdapter = new QualityAdapter();

    private List<QualityBean> mQualityList = new ArrayList<>();
    private List<QualityBean> mResolutionList = new ArrayList<>();

    public static void put(CaptureConfiguration captureConfiguration, Bundle bundle) {
        bundle.putParcelable(CAPTURE_CONNFIG, captureConfiguration);
    }


    @Override
    public void initData() {
        super.initData();
        mQualityList.add(new QualityBean("HIGH", PredefinedCaptureConfigurations.CaptureQuality.HIGH));
        mQualityList.add(new QualityBean("MEDIUM", PredefinedCaptureConfigurations.CaptureQuality.MEDIUM));
        mQualityList.add(new QualityBean("LOW", PredefinedCaptureConfigurations.CaptureQuality.LOW));

        mResolutionList.add(new QualityBean("360P", PredefinedCaptureConfigurations.CaptureResolution.RES_360P));
        mResolutionList.add(new QualityBean("480P", PredefinedCaptureConfigurations.CaptureResolution.RES_480P));
        mResolutionList.add(new QualityBean("720P", PredefinedCaptureConfigurations.CaptureResolution.RES_720P));
        mResolutionList.add(new QualityBean("1080P", PredefinedCaptureConfigurations.CaptureResolution.RES_1080P));
        mResolutionList.add(new QualityBean("1440P", PredefinedCaptureConfigurations.CaptureResolution.RES_1440P));
        mResolutionList.add(new QualityBean("2160P", PredefinedCaptureConfigurations.CaptureResolution.RES_2160P));

        mQualityAdapter.setNewData(mQualityList);
        mResolutionAdapter.setNewData(mResolutionList);

        Bundle bundle = getBundle();
        CaptureConfiguration p = bundle.getParcelable(CAPTURE_CONNFIG);
        for (int i = 0; i < mQualityList.size(); i++) {
            QualityBean qualityBean = mQualityList.get(i);
            PredefinedCaptureConfigurations.CaptureQuality cq = (PredefinedCaptureConfigurations.CaptureQuality) qualityBean.getTag();
            if (cq == p.getQuality()) {
                mQualityAdapter.setSelectPosition(i);
                break;
            }
        }
        for (int i = 0; i < mResolutionList.size(); i++) {
            QualityBean qualityBean = mResolutionList.get(i);
            PredefinedCaptureConfigurations.CaptureResolution cr = (PredefinedCaptureConfigurations.CaptureResolution) qualityBean.getTag();
            if (cr == p.getResolution()) {
                mResolutionAdapter.setSelectPosition(i);
                break;
            }
        }

    }

    @Override
    public void initWidget() {
        super.initWidget();
        mQualityRv.setLayoutManager(new LinearLayoutManager(this));
        mQualityRv.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL, 10, Color.GRAY));
        mQualityRv.setAdapter(mQualityAdapter);

        mResolutionRv.setLayoutManager(new LinearLayoutManager(this));
        mResolutionRv.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL, 10, Color.GRAY));
        mResolutionRv.setAdapter(mResolutionAdapter);

        mQualityAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mQualityAdapter.setSelectPosition(position);
                adapter.notifyDataSetChanged();
            }
        });

        mResolutionAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mResolutionAdapter.setSelectPosition(position);
                mResolutionAdapter.notifyDataSetChanged();
            }
        });

        mTopBar.addRightTextButton("保存", R.id.top_bar_right_btn).setOnClickListener(this);
    }

    @Override
    public void forbidClick(View v) {
        super.forbidClick(v);
        switch (v.getId()) {
            case R.id.top_bar_right_btn:
                int selectPosition = mQualityAdapter.getSelectPosition();
                QualityBean qualityBean = mQualityList.get(selectPosition);
                QualityPreferences.saveQuality((PredefinedCaptureConfigurations.CaptureQuality) qualityBean.getTag());

                int rPosition = mResolutionAdapter.getSelectPosition();
                QualityBean rQ = mResolutionList.get(rPosition);
                QualityPreferences.saveResolution((PredefinedCaptureConfigurations.CaptureResolution) rQ.getTag());
                finish();
                break;
        }
    }
}
