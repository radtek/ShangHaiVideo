package nss.mobile.video.ui.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseQItemAdapter;
import nss.mobile.video.base.QBaseViewHolder;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/29
 *
 * @author ql
 */
public class DevAdapter extends BaseQItemAdapter<IDev,QBaseViewHolder>{
    public DevAdapter() {
        super(R.layout.item_dev);
    }

    @Override
    protected void convert(QBaseViewHolder helper, IDev item) {

    }
}
