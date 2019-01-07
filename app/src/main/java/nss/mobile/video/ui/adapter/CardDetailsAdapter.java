package nss.mobile.video.ui.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import nss.mobile.video.R;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2019/1/7
 *
 * @author ql
 */
public class CardDetailsAdapter extends BaseQuickAdapter<ICardDetails,BaseViewHolder>{
    public CardDetailsAdapter() {
        super(R.layout.item_card_details);
    }

    @Override
    protected void convert(BaseViewHolder helper, ICardDetails item) {
        helper.setText(R.id.item_card_details_label_tv,item.getLabel())
                .setText(R.id.item_card_details_tv,item.getValue());
    }
}
