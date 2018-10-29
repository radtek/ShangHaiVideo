package com.feiling.video.ui;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feiling.video.R;
import com.feiling.video.base.BaseActivity;
import com.feiling.video.base.BindLayout;
import com.feiling.video.base.bind.BindView;
import com.feiling.video.ui.adapter.QLViewHolder;
import com.feiling.video.utils.LogUtils;
import com.feiling.video.video.VideoFile;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@BindLayout(layoutRes = R.layout.activity_file_list)
public class FileListActivity extends BaseActivity {

    @BindView(R.id.file_list_rv)
    RecyclerView rv;

    private BaseQuickAdapter<FileBean, QLViewHolder> adapter = new BaseQuickAdapter<FileBean, QLViewHolder>(R.layout.item_file_video) {
        @Override
        protected void convert(QLViewHolder helper, FileBean item) {
            Glide.with(helper.itemView.getContext())
                    .load(item.getIcon())
                    .into((ImageView) helper.getView(R.id.itemFileVideo_iv));
            helper.setText(R.id.itemFileVideo_code_tv, item.getCode())
                    .setText(R.id.itemFileVideo_createDate_tv, item.getStartTime() + "")
                    .setText(R.id.itemFileVideo_fileName_tv, item.getFileName())
                    .setText(R.id.itemFileVideo_fileSize_tv, item.getFileSize() + "")
                    .setText(R.id.itemFileVideo_uploadStatus_tv, item.getUpStatus())
                    .setText(R.id.itemFileVideo_videoDum_tv, item.getPlayTime() + "");
        }
    };

    @Override
    public void initData() {
        super.initData();
        EventBus.getDefault().register(this);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        new Thread(new FileReaderThread()).start();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setResDada(ArrayList<FileBean> list) {
        adapter.setNewData(list);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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
                mediaMetadataRetriever.setDataSource(f.getAbsolutePath());
                Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(f.getAbsolutePath(),0);
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
                String imgPath = saveBitmap(path, videoThumbnail);
                //创建时间
                String startDate = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
                //播放时长
                String playLong = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                mediaMetadataRetriever.release();
                FileBean fb = new FileBean();
                fb.setFileName(f.getName());
                fb.setIcon(imgPath);
                fb.setStartTime(startDate);
                long time = Long.parseLong(playLong);
                time = time / 1000;
                long miao = time % 60;
                long fen = time /60;
                fb.setPlayTime(fen + "分" + miao + "秒");
                long l = f.length() / 1024;
                fb.setFileSize(l / 1024 + "M" + l%1024 +"KB");
                fb.setUpStatus("");
                fb.setCode("");
                fileBeans.add(fb);
            }
            EventBus.getDefault().post(fileBeans);
        }
    }

    /**
     * 保存bitmap到本地
     *
     * @param mBitmap
     * @return
     */
    public static String saveBitmap(String path, Bitmap mBitmap) {
        File filePic;
        try {
            filePic = new File(path);
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return filePic.getAbsolutePath();
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
            return upStatus;
        }

        public void setUpStatus(String upStatus) {
            this.upStatus = upStatus;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
