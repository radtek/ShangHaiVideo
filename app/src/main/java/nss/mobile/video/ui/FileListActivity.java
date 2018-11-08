package nss.mobile.video.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import nss.mobile.video.R;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.ui.adapter.QLViewHolder;
import nss.mobile.video.utils.LogUtils;
import nss.mobile.video.utils.UnitHelper;
import nss.mobile.video.utils.div.DividerItemDecoration;
import nss.mobile.video.video.VideoFile;

@BindLayout(layoutRes = R.layout.activity_file_list, title = "视文件")
public class FileListActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.file_list_rv)
    RecyclerView rv;
    @BindView(R.id.file_list_bottom_group)
    ViewGroup optionGroup;
    @BindView(R.id.file_list_delete_tv)
    TextView deleteBtn;
    @BindView(R.id.file_list_null_group)
    ViewGroup nullGroup;
    @BindView(R.id.file_list_upload_tv)
    TextView uploadTv;
    private boolean isSetting = false;
    private BaseQuickAdapter<FileBean, QLViewHolder> adapter = new BaseQuickAdapter<FileBean, QLViewHolder>(R.layout.item_file_video) {
        @Override
        protected void convert(QLViewHolder helper, FileBean item) {
            ImageView view1 = (ImageView) helper.getView(R.id.itemFileVideo_iv);
            if (!item.filePath().equals(view1.getTag(R.id.top_bar_right_btn))) {
                Glide.with(helper.itemView.getContext())
                        .load(item.filePath())
                        .into(view1);
            }

            view1.setTag(R.id.top_bar_right_btn, item.filePath);
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
    };
    private Button topbarOption;
    private List<FileBean> data;

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void initWidget() {
        super.initWidget();
        uploadTv.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL, 10, Color.GRAY));
        rv.setAdapter(adapter);
//        new Thread(new FileReaderThread()).start();
        changeData();
        adapter.addFooterView(getLayoutInflater().inflate(R.layout.foot_null, null));
        adapter.setOnItemClickListener(this);
        topbarOption = mTopBar.addRightTextButton("操作", R.id.top_bar_right_btn);
        topbarOption.setOnClickListener(this);
    }

    @NonNull
    private List<FileBean> createDate() {
        File file = VideoFile.baseFile();
        File[] files = file.listFiles();
        List<FileBean> l = new ArrayList<>();
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (File file1 : files) {
            FileBean f = new FileBean();
            f.setFileName(file1.getName());
            long l1 = file1.lastModified();
            f.setStartTime(s.format(new Date(l1)));
            f.setFilePath(file1.getAbsolutePath());
            f.setFileSize(UnitHelper.formatterFileSize(file1.length()));
            l.add(f);
        }
        return l;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        if (isSetting) {
            FileBean fileBean = data.get(position);
            fileBean.select = !fileBean.select;
            adapter.notifyDataSetChanged();
            return;
        }
        Bundle bundle = new Bundle();
        List<FileBean> data = adapter.getData();
        VideoPlayActivity.put(data.get(position).filePath(), bundle);
        startActivity(VideoPlayActivity.class, bundle);
    }


    @Override
    public void forbidClick(View v) {
        super.forbidClick(v);
        switch (v.getId()) {
            case R.id.file_list_upload_tv://上传视频;
                toast("测试--->上传视频中");
                return;
            case R.id.top_bar_right_btn:
                if (isSetting) {
                    //取消
                    topbarOption.setText("操作");
                    isSetting = !isSetting;
                    adapter.notifyDataSetChanged();
                    List<FileBean> data = adapter.getData();
                    for (FileBean datum : data) {
                        datum.select = false;
                    }
                    optionGroup.setVisibility(View.GONE);
                    return;
                }
                topbarOption.setText("完成");

                isSetting = !isSetting;
                adapter.notifyDataSetChanged();
                optionGroup.setVisibility(View.VISIBLE);
                break;
            case R.id.file_list_delete_tv:

                QMUIDialog dialog = new QMUIDialog.MessageDialogBuilder(this)
                        .setMessage("请确定删除指定视频吗？")
                        .addAction("确定", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                List<FileBean> data = adapter.getData();
                                for (FileBean datum : data) {
                                    if (datum.isSelect()) {
                                        File file = new File(datum.filePath);
                                        file.delete();
                                    }
                                }
                                changeData();
                                dialog.cancel();
                            }
                        }).addAction("取消", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.cancel();
                            }
                        }).show();

                break;
        }
    }

    private void changeData() {
        data = createDate();
        adapter.setNewData(data);
        adapter.notifyDataSetChanged();
        int i = data.size() == 0 ? View.VISIBLE : View.GONE;
        nullGroup.setVisibility(i);
    }

    public static class FileReaderThread implements Runnable {

        @Override
        public void run() {
            List<FileBean> fileBeans = new ArrayList<>();
            File file = VideoFile.baseFile();
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                if (!f.isFile()) {
                    continue;
                }
                if (!f.getName().endsWith(".mp4")) {
                    continue;
                }
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                LogUtils.i(getClass().getName(), f.getAbsolutePath());
                try {
                    mediaMetadataRetriever.setDataSource(f.getAbsolutePath());
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    continue;
                }
                Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(f.getAbsolutePath(), 0);
                LogUtils.i(getClass().getName(), f.getName());
                String[] split = f.getName().split("\\.");

                String path = file.getAbsolutePath() + "/" + split[0] + ".jpg";
                File imgFile = new File(path);
                if (!imgFile.exists()) {
                    try {
                        imgFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //创建时间
                String startDate = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
                //播放时长
                String playLong = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                mediaMetadataRetriever.release();
                FileBean fb = new FileBean();
                fb.setFileName(f.getName());
                fb.setStartTime(startDate);
                long time = Long.parseLong(playLong);
                time = time / 1000;
                long miao = time % 60;
                long fen = time / 60;
                fb.setPlayTime(fen + "分" + miao + "秒");
                long l = f.length() / 1024;
                fb.setFileSize(l / 1024 + "M" + l % 1024 + "KB");
                fb.setUpStatus("");
                fb.setCode("");
                fileBeans.add(fb);
            }
            EventBus.getDefault().post(fileBeans);
        }
    }


    public static class FileBean {
        //单元内容:预览图、⽂文件名、开始时间、时⻓长、⽂文件⼤大⼩小、上传状态、校验码;
        private String icon;
        private String fileName;
        private String startTime;
        private String playTime;
        private String fileSize;
        private String upStatus;
        private String code;
        private String filePath;
        private boolean select;

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getPlayTime() {
            return playTime;
        }

        public void setPlayTime(String playTime) {
            this.playTime = playTime;
        }

        public String getFileSize() {
            return fileSize;
        }

        public void setFileSize(String fileSize) {
            this.fileSize = fileSize;
        }

        public String getUpStatus() {
            if (TextUtils.isEmpty(upStatus)) {
                return "未上传";
            }
            return upStatus;
        }

        public void setUpStatus(String upStatus) {
            this.upStatus = upStatus;
        }

        public String getCode() {
            if (TextUtils.isEmpty(code)) {
                return "校验码";
            }
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String filePath() {
            return filePath;
        }

        public boolean isSelect() {
            return select;
        }
    }
}
