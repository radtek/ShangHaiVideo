package nss.mobile.video.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.litepal.LitePal;

import java.io.File;
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
import nss.mobile.video.ui.adapter.FileUploadingAdapter;
import nss.mobile.video.utils.UnitHelper;
import nss.mobile.video.utils.div.DividerItemDecoration;

@BindLayout(layoutRes = R.layout.activity_file_uploading, title = "上传文件")
public class FileUploadingActivity extends BaseActivity implements BaseQuickAdapter.OnItemChildClickListener, ServiceConnection, IFtpStatusListener, UploadFileService.IFileUploadListener {

    @BindView(R.id.fileUploading_rv)
    RecyclerView rv;
    @BindView(R.id.fileUploading_error_hint_group)
    ViewGroup errorHintGroup;
    @BindView(R.id.fileUploading_hint_tv)
    TextView hintTv;
    @BindView(R.id.fileUploading_open_tv)
    TextView openTv;
    @BindView(R.id.fileUploading_delete_tv)
    TextView deleteTv;

    private int deletePosition;

    private FileUploadingAdapter uploadingAdapter = new FileUploadingAdapter();
    private List<FileProgressBean> data = new ArrayList<>();
    private UploadFileService.FileBinder fileBind;


    @Override
    public void initData() {
        super.initData();

    }

    @Override
    public void initWidget() {
        super.initWidget();
        openTv.setOnClickListener(this);
        deleteTv.setOnClickListener(this);
        errorHintGroup.setOnClickListener(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL, 10, Color.GRAY));
        rv.setAdapter(uploadingAdapter);


        uploadingAdapter.setOnItemChildClickListener(this);
        Intent intent = new Intent(this, UploadFileService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        resetData();
    }

    private void resetData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                data.clear();
                SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                List<UploadFile> uploadFiles = LitePal.where("status = ? or status = ?", UploadFile.UPLOAD_ING, UploadFile.UPLOAD_READY).find(UploadFile.class);
                for (UploadFile uploadFile : uploadFiles) {
                    File file = new File(uploadFile.getFilePath());
                    if (!file.exists()) {
                        fileBind.removeUpload(file.getAbsolutePath());
                        continue;
                    }
                    FileProgressBean f = new FileProgressBean();
                    f.setFileName(file.getName());
                    long l1 = file.lastModified();
                    f.setStartTime(s.format(new Date(l1)));
                    f.setFilePath(file.getAbsolutePath());
                    f.setUpStatus("未知");
                    switch (uploadFile.getStatus()) {
                        case UploadFile.UPLOAD_FAILED:
                            f.setUpStatus("上传失败");
                            break;
                        case UploadFile.UPLOAD_ING:
                            f.setUpStatus("上传中");
                            break;
                        case UploadFile.UPLOAD_READY:
                            f.setUpStatus("准备上传");
                            break;
                    }
                    f.setMax(100);
                    f.setFileSize(UnitHelper.formatterFileSize(file.length()));
                    data.add(f);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uploadingAdapter.setNewData(data);
                        uploadingAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();

    }

    private void restartService() {
        Intent intent = new Intent(this, UploadFileService.class);
        UploadFileService.reconnect(intent);
        startService(intent);
    }

    @Override
    public void forbidClick(View v) {
        super.forbidClick(v);
        if (v.getId() == deleteTv.getId()) {
            UploadFile.deleteAll();
            FileUploadChangeEvent.getInstance().postUploadStatusChange(2);
        } else if (v.getId() == openTv.getId()) {
            if (fileBind.isFtpConnect()) {
                if (fileBind.isUploading()) {
                    fileBind.stopUpload();
                    openTv.setText("全部开启");
                } else {
                    fileBind.startUpload();
                }

            } else {

            }
        } else if (v.getId() == errorHintGroup.getId()) {
            restartService();
        }
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        if (view.getId() == R.id.uploading_delete_tv) {
            deletePosition = position;
            new QMUIDialog.MessageDialogBuilder(this)
                    .addAction("确定", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            String filePath = data.get(deletePosition).getFilePath();
                            fileBind.removeUpload(filePath);
                            resetData();
                            dialog.cancel();
                        }
                    })
                    .addAction("取消", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.cancel();
                        }
                    })
                    .setTitle("提示")
                    .setMessage("确定取消上传该视频吗？")
                    .show();
        }
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        fileBind = (UploadFileService.FileBinder) service;
        fileBind.addFtpListener(this);
        fileBind.addUploadListener(this);

        if (!fileBind.isFtpConnect()) {
            errorHintGroup.setVisibility(View.VISIBLE);
            hintTv.setText("启动ftp服务中");
            resetData();
            restartService();
            return;
        }
        restartService();
        errorHintGroup.setVisibility(View.GONE);
        changeButton();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }

    @Override
    public void ftpConnectSuccess() {
        errorHintGroup.setVisibility(View.GONE);
        changeButton();
    }

    private void changeButton() {
        if (fileBind.isUploading()) {
            openTv.setText("全部暂停");
        } else {
            openTv.setText("全部开启");
        }
    }

    @Override
    public void ftpConnectFailed(Exception e) {
        errorHintGroup.setVisibility(View.VISIBLE);
        hintTv.setText(e.getMessage());

    }

    @Override
    public void ftpOff() {
        errorHintGroup.setVisibility(View.VISIBLE);
        hintTv.setText("ftp断开连接");
    }

    @Override
    public void ftpDisconnect() {
        errorHintGroup.setVisibility(View.VISIBLE);
        hintTv.setText("ftp断开连接");
    }

    @Override
    public void startUploadFile(File file) {
        resetData();
        changeButton();
    }

    @Override
    public void uploadFailed(File file, Exception e) {
        resetData();
    }

    @Override
    public void uploadEnd(File file) {
        resetData();
    }

    @Override
    public void uploadRemove(File localPath, UploadRemoveException e) {
        resetData();
    }

    @Override
    public void onAfter() {
        resetData();
    }

    @Override
    public void onProcess(long currentSize, long localSize, File localPath) {
        for (int i = 0; i < data.size(); i++) {
            FileProgressBean datum = data.get(i);
            if (datum.getFilePath().equals(localPath.getAbsolutePath())) {
                datum.setMax((int) localSize);
                datum.setProgress((int) currentSize);
                uploadingAdapter.notifyItemChanged(i);
                break;
            }
        }
    }
}
