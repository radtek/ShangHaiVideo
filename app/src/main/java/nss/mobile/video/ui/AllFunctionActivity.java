package nss.mobile.video.ui;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.ArrayList;
import java.util.List;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.ui.adapter.FunctionAdapter;
import nss.mobile.video.ui.adapter.ITag;

@BindLayout(layoutRes = R.layout.activity_all_function, title = "功能选择")
public class AllFunctionActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener {


    @BindView(R.id.allFunction_rv)
    RecyclerView rv;

    private List<MultiItemEntity> list = new ArrayList<>();

    private FunctionAdapter functionAdapter = new FunctionAdapter();

    @Override
    public void initData() {
        super.initData();
        createMenu();
    }

    @Override
    public void initWidget() {
        super.initWidget();
        rv.setLayoutManager(new LinearLayoutManager(this));

        functionAdapter.setNewData(list);
        rv.setAdapter(functionAdapter);

        functionAdapter.setOnItemClickListener(this);

    }

    private void createMenu() {
        list.add(new FunctionAdapter.NormalFunction("录像基本设置", R.drawable.icon_setting, 0));
        list.add(new FunctionAdapter.NormalFunction("文件查看", R.drawable.icon_video_file, 1));
        list.add(new FunctionAdapter.NormalFunction("视频直播", R.drawable.icon_video_file, 2));

    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        int tag = ((ITag) list.get(position)).getTag();
        switch (tag) {
            case 0:
                startActivity(SettingActivity.class);
                break;
            case 1:
                startActivity(FileListActivity.class);
                break;
            case 2:

                startActivity(SimLiveVideActivity.class);
                break;
        }
    }


}
