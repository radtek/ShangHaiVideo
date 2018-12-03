package nss.mobile.video.ui.fragment;

import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseFragment;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.ui.adapter.DevAdapter;
import nss.mobile.video.ui.adapter.IDev;
import nss.mobile.video.utils.div.DividerItemDecoration;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/29
 *
 * @author ql
 */
@BindLayout(layoutRes = R.layout.frag_dev,bindTopBar = false)
public class DevFragment extends BaseFragment {

    @BindView(R.id.frag_dev_rv)
    RecyclerView rv;

    private DevAdapter devAdapter = new DevAdapter();

    private List<IDev> list = new ArrayList<>();

    @Override
    protected void initData() {
        super.initData();
        for (int i = 0; i < 20; i++) {
            list.add(new IDev() {
            });
        }
        devAdapter.setNewData(list);
    }

    @Override
    protected void initWidget(View view) {
        super.initWidget(view);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL, 20, Color.GRAY));

        rv.setAdapter(devAdapter);
    }
}


