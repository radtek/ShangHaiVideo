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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.bean.db.UploadFile;
import nss.mobile.video.event.FileUploadChangeEvent;
import nss.mobile.video.service.IFileUploadListener;
import nss.mobile.video.service.IFtpCallback;
import nss.mobile.video.service.UploadFileService;
import nss.mobile.video.ui.adapter.FileUploadingAdapter;
import nss.mobile.video.utils.div.DividerItemDecoration;

@BindLayout(layoutRes = R.layout.activity_file_uploading, title = "上传文件")
public class FileUploadingActivity extends BaseActivity implements BaseQuickAdapter.OnItemChildClickListener, ServiceConnection, IFileUploadListener, IFtpCallback {

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
    private UploadFileService.FileUploadBinder fileUploadBinder;
    private List<FileProgressBean> data = new ArrayList<>();


    @Override
    public void initData() {
        super.initData();
        Intent intent = new Intent(this, UploadFileService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        openTv.setOnClickListener(this);
        deleteTv.setOnClickListener(this);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL, 10, Color.GRAY));
        rv.setAdapter(uploadingAdapter);


        uploadingAdapter.setOnItemChildClickListener(this);
    }

    @Override
    public void forbidClick(View v) {
        super.forbidClick(v);
        if (v.getId() == deleteTv.getId()) {
            UploadFile.deleteAll();
            FileUploadChangeEvent.getInstance().postMemoryEvent(2);
        } else if (v.getId() == openTv.getId()) {
            if (fileUploadBinder.isUploading()) {
                fileUploadBinder.stopUpload();
                openTv.setText("全部开启");
            } else {
                fileUploadBinder.startUpload();
                openTv.setText("全部关闭");
            }
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
                            fileUploadBinder.stopUploadByFile(filePath);
                            UploadFile.deleteByFilePath(filePath);
                            FileUploadChangeEvent.getInstance().postMemoryEvent(1);
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

    private void restartService() {
        Intent intent = new Intent(this, UploadFileService.class);
        startService(intent);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        fileUploadBinder = (UploadFileService.FileUploadBinder) service;
        fileUploadBinder.addFileUploadListener(this);
        fileUploadBinder.addFtpCallback(this);

        if (fileUploadBinder.isFtpConnect()) {
            errorHintGroup.setVisibility(View.VISIBLE);
            hintTv.setText("启动ftp服务中");
            restartService();
            return;
        }
        errorHintGroup.setVisibility(View.GONE);

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void uploadFileStart(File lastFile) {

    }

    @Override
    public void uploadFileError(File lastFile, Exception e) {
        for (FileProgressBean datum : data) {
            if (datum.getFilePath().equals(lastFile.getAbsolutePath())) {
                datum.setUploadErrorHint(e.getMessage());
                uploadingAdapter.setNewData(data);
                uploadingAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void uploadFileSuccess(File lastFile) {
        for (FileProgressBean datum : data) {
            if (datum.getFilePath().equals(lastFile.getAbsolutePath())) {
                data.remove(datum);
                uploadingAdapter.setNewData(data);
                uploadingAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void uploadingFile(float progress, long total, File lastFile) {
        for (FileProgressBean datum : data) {
            if (datum.getFilePath().equals(lastFile.getAbsolutePath())) {
                datum.setMax(100);
                datum.setProgress((int) (progress * 100));
                uploadingAdapter.setNewData(data);
                uploadingAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void ftpFailed(String error) {
        errorHintGroup.setVisibility(View.VISIBLE);
        hintTv.setText(error);
    }

    @Override
    public void ftpConnectSuccess() {
        errorHintGroup.setVisibility(View.GONE);
        if (fileUploadBinder.isUploading()) {
            openTv.setText("全部关闭");
        } else {
            openTv.setText("全部开启");
        }
    }

    @Override
    public void ftpLoginFailed(String msg) {
        errorHintGroup.setVisibility(View.VISIBLE);
        hintTv.setText(msg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }
}
