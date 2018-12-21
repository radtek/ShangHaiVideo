package nss.mobile.video.ui;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.bean.MobileKeyBean;
import nss.mobile.video.zxing.encoding.EncodingUtils;

@BindLayout(layoutRes = R.layout.activity_sn, title = "序列号")
public class SnActivity extends BaseActivity {


    @BindView(R.id.sn_tv)
    TextView mSnTv;
    @BindView(R.id.sn_iv)
    ImageView mSnIv;


    @Override
    public void initWidget() {
        super.initWidget();
        MobileKeyBean last = MobileKeyBean.getLast();
        mSnTv.setText(last.getMobileKey());
        Bitmap qrCode = EncodingUtils.createQRCode(last.getMobileKey(), 200, 200, null);
        mSnIv.setImageBitmap(qrCode);

    }
}
