package nss.mobile.video;

import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.bean.MobileKeyBean;

@BindLayout(layoutRes = R.layout.activity_router, bindTopBar = false)
public class RouterActivity extends BaseActivity {


    @Override
    public void initData() {
        super.initData();
        if (MobileKeyBean.getLast() == null) {
            startActivity(CheckSnActivity.class);
        } else {
            startActivity(MainActivity.class);
        }
        finish();
    }
}
