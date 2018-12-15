package nss.mobile.video.ui.video;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.bean.db.UploadFile;
import nss.mobile.video.event.FileUploadChangeEvent;
import nss.mobile.video.http.ftp.UploadRemoveException;
import nss.mobile.video.service.IFtpStatusListener;
import nss.mobile.video.service.UploadFileService;
import nss.mobile.video.ui.adapter.QLViewHolder;
import nss.mobile.video.utils.LogUtils;
import nss.mobile.video.utils.UnitHelper;
import nss.mobile.video.utils.div.DividerItemDecoration;
import nss.mobile.video.video.VideoFile;

@BindLayout(layoutRes = R.layout.activity_file_list, title = "视文件")
public class FileListActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener, ServiceConnection, IFtpStatusListener, UploadFileService.IFileUploadListener {

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
    @BindView(R.id.file_list_hint_tv)
    TextView hintTv;
    @BindView(R.id.file_list_error_hint_group)
    ViewGroup errorHintGroup;
    @BindView(R.id.file_list_ftp_pb)
    ProgressBar pb;
    @BindView(R.id.file_list_load_data_group)
    ViewGroup loadDataGroup;
    @BindView(R.id.file_list_check_file_tv)
    TextView checkFileTv;

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
    };
    private Button topbarOption;
    private List<FileBean> data = new ArrayList<>();
    private UploadFileService.FileBinder fileBind;


    @Override
    public void initData() {
        super.initData();

    }

    private void restartService() {
        Intent intent = new Intent(this, UploadFileService.class);
        UploadFileService.reconnect(intent);
        startService(intent);
        hintTv.setText("正在连接ftp服务器");
        pb.setVisibility(View.VISIBLE);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        uploadTv.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        errorHintGroup.setOnClickListener(this);
        checkFileTv.setOnClickListener(this);
        pb.setVisibility(View.GONE);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL, 10, Color.GRAY));
        rv.setAdapter(adapter);
//        new Thread(new FileReaderThread()).start();
        changeData();
        adapter.addFooterView(getLayoutInflater().inflate(R.layout.foot_null, null));
        adapter.setOnItemClickListener(this);
        topbarOption = mTopBar.addRightTextButton("操作", R.id.top_bar_right_btn);
        topbarOption.setOnClickListener(this);
        mTopBar.addRightTextButton("查看上传", R.id.top_bar_right_look_uploading).setOnClickListener(this);

        FileUploadChangeEvent.getInstance().register(this);

        Intent intent = new Intent(this, UploadFileService.class);
        bindService(intent, this, BIND_AUTO_CREATE);

        FrameLayout frameLayout = new FrameLayout(this);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(0, 200);
        frameLayout.setLayoutParams(layoutParams);
        adapter.addFooterView(frameLayout);

    }

    private void createDate() {
        data.clear();
        File file = VideoFile.baseFile();
        File[] files = file.listFiles();
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (File file1 : files) {
            FileBean f = new FileBean();
            f.setFileName(file1.getName());
            long l1 = file1.lastModified();
            f.setStartTime(s.format(new Date(l1)));
            f.setFilePath(file1.getAbsolutePath());
            f.setUpStatus("未知");
            f.setFileSize(UnitHelper.formatterFileSize(file1.length()));
            data.add(f);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
        EventBus.getDefault().unregister(this);
        FileUploadChangeEvent.getInstance().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void fileStatusChange(Integer i) {
        checkFiles();
    }


    private void checkFiles() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (FileListActivity.class) {


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadDataGroup.setVisibility(View.VISIBLE);
                        }
                    });

                    for (FileBean datum : data) {
                        boolean isServiceHas = false;
                        if (fileBind != null && fileBind.isFtpConnect()) {

                            try {
                                isServiceHas = fileBind.isServiceHasFile(datum.getFilePath());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (isServiceHas) {
                                datum.setUpStatus("已上传");
                                continue;
                            } else {
                                datum.setUpStatus("未上传");
                            }
                        }

                        UploadFile uploadFile = UploadFile.selectByFilePath(datum.getFilePath());
                        if (uploadFile == null) {
                            datum.setUpStatus("未上传");
                            continue;
                        }
                        switch (uploadFile.getStatus()) {
                            case UploadFile.UPLOAD_FAILED:
                                datum.setUpStatus("上传失败");
                                break;
                            case UploadFile.UPLOAD_ING:
                                datum.setUpStatus("上传中");
                                break;
                            case UploadFile.UPLOAD_READY:
                                datum.setUpStatus("准备上传");
                                break;
                        }

                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadDataGroup.setVisibility(View.GONE);
                            adapter.setNewData(data);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }).start();


    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        if (isSetting) {
            FileBean fileBean = data.get(position);
            fileBean.setSelect(!fileBean.isSelect());
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
            case R.id.file_list_error_hint_group:
                restartService();
                break;
            case R.id.file_list_upload_tv://上传视频;
                for (FileBean datum : data) {
                    if (datum.isSelect()) {
                        UploadFile uploadFile = UploadFile.selectByFilePath(datum.getFilePath());
                        if (uploadFile != null) {
                            if (UploadFile.UPLOAD_ING.equals(uploadFile.getStatus())
                                    || UploadFile.UPLOAD_READY.equals(uploadFile.getStatus())) {
                                continue;
                            }
                            uploadFile.setStatus(UploadFile.UPLOAD_READY);
                            uploadFile.update(uploadFile.getId());
                            continue;
                        }
                        uploadFile = new UploadFile();
                        uploadFile.setStatus(UploadFile.UPLOAD_READY);
                        uploadFile.setFilePath(datum.getFilePath());
                        uploadFile.save();
                    }
                }
                changeData();
                fileBind.startUpload();
                return;
            case R.id.top_bar_right_btn:
                if (isSetting) {
                    //取消
                    topbarOption.setText("操作");
                    isSetting = !isSetting;
                    adapter.notifyDataSetChanged();
                    List<FileBean> data = adapter.getData();
                    for (FileBean datum : data) {
                        datum.setSelect(false);
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
                                        File file = new File(datum.getFilePath());
                                        file.delete();
                                        fileBind.removeUpload(file.getAbsolutePath());
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
            case R.id.top_bar_right_look_uploading:
                startActivity(FileUploadingActivity.class);
                break;
            case R.id.file_list_check_file_tv:
                checkFiles();
                break;
        }
    }

    private void changeData() {
        createDate();
        adapter.setNewData(data);
        adapter.notifyDataSetChanged();
        int i = data.size() == 0 ? View.VISIBLE : View.GONE;
        nullGroup.setVisibility(i);
    }
    /****************************************/
    /****************启动文件服务*****************/
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        fileBind = (UploadFileService.FileBinder) service;
        fileBind.addFtpListener(this);
        fileBind.addUploadListener(this);

        if (!fileBind.isFtpConnect()) {
            errorHintGroup.setVisibility(View.VISIBLE);
            hintTv.setText("启动ftp服务中");
            pb.setVisibility(View.VISIBLE);
            restartService();
            return;
        }
        errorHintGroup.setVisibility(View.GONE);
        checkFiles();

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }


    @Override
    public void ftpConnectSuccess() {
        errorHintGroup.setVisibility(View.GONE);
        pb.setVisibility(View.GONE);
    }

    @Override
    public void ftpConnectFailed(Exception e) {
        pb.setVisibility(View.GONE);
        errorHintGroup.setVisibility(View.VISIBLE);
        if (e instanceof SocketTimeoutException) {
            hintTv.setText("连接ftp服务器超时，请检查是否与服务在同一个网关，并联系管理员");
        } else if (e instanceof ConnectException) {
            hintTv.setText("连接ftp服务异常");
        } else {
            hintTv.setText(e.getMessage());
        }
    }

    @Override
    public void ftpOff() {
        pb.setVisibility(View.GONE);
        errorHintGroup.setVisibility(View.VISIBLE);
        hintTv.setText("与ftp服务断开连接");
    }

    @Override
    public void ftpDisconnect() {
        pb.setVisibility(View.GONE);
        errorHintGroup.setVisibility(View.VISIBLE);
        hintTv.setText("与ftp服务断开连接");
    }

    @Override
    public void startUploadFile(File file) {
    }

    @Override
    public void uploadFailed(File file, Exception e) {

    }

    @Override
    public void uploadEnd(File file) {

    }

    @Override
    public void uploadRemove(File localPath, UploadRemoveException e) {

    }

    @Override
    public void onAfter() {
    }

    @Override
    public void onProcess(long currentSize, long localSize, File localPath) {
        LogUtils.i(getClass().getName(), currentSize + "-----" + localSize);
    }

}
