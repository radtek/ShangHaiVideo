package nss.mobile.video.ui.adapter;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import nss.mobile.video.R;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/12/14
 *
 * @author ql
 */
public class AliFileAdapter extends BaseQuickAdapter<IFile, BaseViewHolder> {
    private boolean isSetting = false;

    public AliFileAdapter() {
        super(R.layout.item_file_video);
    }

    @Override
    protected void convert(BaseViewHolder helper, IFile item) {
        ImageView view1 = (ImageView) helper.getView(R.id.itemFileVideo_iv);
        if (!item.getFilePath().equals(view1.getTag(R.id.top_bar_right_btn))) {
            Glide.with(helper.itemView.getContext())
                    .load(item.getFilePath())
                    .into(view1);
        }

        view1.setTag(R.id.top_bar_right_btn, item.getFilePath());
        helper.setText(R.id.itemFileVideo_code_tv, item.getCode())
                .setText(R.id.itemFileVideo_createDate_tv, item.getStartTime() + "")
                .setText(R.id.itemFileVideo_fileName_tv, item.getFileName())
                .setText(R.id.itemFileVideo_fileSize_tv, item.getFileSize() + "")
                .setText(R.id.itemFileVideo_uploadStatus_tv, item.getUpStatus())
                .setText(R.id.itemFileVideo_videoDum_tv, item.getPlayTime() + "");
        View view = helper.getView(R.id.itemFileVideo_check_iv);
        if (view.isSelected() != item.isSelect()) {
            view.setSelected(item.isSelect());
        }
        int i = isSetting ? View.VISIBLE : View.GONE;
        if (view.getVisibility() != i) {
            view.setVisibility(i);
        }
    }

    public boolean isSetting() {
        return isSetting;
    }

    public void setSetting(boolean setting) {
        isSetting = setting;
    }
}
