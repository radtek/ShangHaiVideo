package nss.mobile.video.video;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;

import nss.mobile.video.R;
import nss.mobile.video.ui.adapter.QLViewHolder;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/4
 *
 * @author ql
 */
public class QualityAdapter extends BaseQuickAdapter<QualityBean, QLViewHolder> {

    private int selectPosition = -1;

    public QualityAdapter() {
        super(R.layout.item_quality_select);
    }

    @Override
    protected void convert(QLViewHolder helper, QualityBean item) {
        helper.setText(R.id.itemQuality_tv, item.getLabelName());
        View view = helper.getView(R.id.itemQuality_iv);
        boolean b = helper.getAdapterPosition() == selectPosition;
        int i = b ? View.VISIBLE : View.INVISIBLE;
        if (view.getVisibility() != i) {
            view.setVisibility(i);
        }
    }

    public int getSelectPosition() {
        return selectPosition;
    }

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
    }
}
