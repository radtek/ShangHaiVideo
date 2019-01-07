package nss.mobile.video.ui.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseFragment;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.bean.CardDetails;
import nss.mobile.video.ui.adapter.CardDetailsAdapter;
import nss.mobile.video.ui.adapter.ICardDetails;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/29
 *
 * @author ql
 */
@BindLayout(layoutRes = R.layout.frag_card,title = "身份证",bindTopBar = false)
public class CardFragment extends BaseFragment{
    @BindView(R.id.frag_card_rv)
    RecyclerView mCardDetailsRv;


    private CardDetailsAdapter mCardDetailsAdapter= new CardDetailsAdapter();
    private List<ICardDetails> mCardDetails = new ArrayList<>();

    @Override
    protected void initData() {
        super.initData();
        mCardDetails.add(new CardDetails("姓名:",null,0));
        mCardDetails.add(new CardDetails("性别:",null,1));
        mCardDetails.add(new CardDetails("出生日期:",null,2));
        mCardDetails.add(new CardDetails("地址:",null,3));
        mCardDetails.add(new CardDetails("民族:",null,4));
        mCardDetails.add(new CardDetails("身份证:",null,5));
        mCardDetailsAdapter.setNewData(mCardDetails);
    }


    @Override
    protected void initWidget(View view) {
        super.initWidget(view);
        mCardDetailsRv.setLayoutManager(new LinearLayoutManager(getContext()));
        mCardDetailsRv.setAdapter(mCardDetailsAdapter);


    }
}
