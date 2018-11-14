package nss.mobile.video.ui.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.List;

import nss.mobile.video.R;
import nss.mobile.video.ui.FileProgressBean;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/12
 *
 * @author ql
 */
public class FileUploadingAdapter extends BaseQuickAdapter<FileProgressBean, QLViewHolder> {

    public FileUploadingAdapter() {
        super(R.layout.item_upload_ing);
    }

    @Override
    protected void convert(QLViewHolder helper, FileProgressBean item) {
        ImageView view1 = (ImageView) helper.getView(R.id.uploading_iv);
        if (!item.filePath().equals(view1.getTag(R.id.top_bar_right_btn))) {
            Glide.with(helper.itemView.getContext())
                    .load(item.filePath())
                    .into(view1);
        }
        view1.setTag(R.id.top_bar_right_btn, item.getFilePath());

        helper.addOnClickListener(R.id.uploading_delete_tv);

        helper.setText(R.id.uploading_createDate_tv, item.getStartTime() + "")
                .setText(R.id.uploading_error_tv, item.getUploadErrorHint())
                .setText(R.id.uploading_fileName_tv, item.getFileName())
                .setText(R.id.uploading_fileSize_tv, item.getFileSize() + "")
                .setText(R.id.uploading_videoDum_tv, item.getPlayTime() + "")
                .setText(R.id.uploading_progress_tv, String.format("%d", (int)(item.getProgress() * 1.0f * 100 / item.getMax())));
        ProgressBar view = helper.getView(R.id.uploading_pb);
        view.setMax(item.getMax());
        view.setProgress(item.getProgress());
    }
}
