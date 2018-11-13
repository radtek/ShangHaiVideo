package nss.mobile.video.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nss.mobile.video.R;
import nss.mobile.video.UploadService;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.bean.db.UploadFile;
import nss.mobile.video.event.FileUploadChangeEvent;
import nss.mobile.video.service.IFileUploadListener;
import nss.mobile.video.service.IFtpCallback;
import nss.mobile.video.service.UpFile;
import nss.mobile.video.service.UploadFileService;
import nss.mobile.video.ui.adapter.QLViewHolder;
import nss.mobile.video.utils.LogUtils;
import nss.mobile.video.utils.UnitHelper;
import nss.mobile.video.utils.div.DividerItemDecoration;
import nss.mobile.video.video.VideoFile;

@BindLayout(layoutRes = R.layout.activity_file_list, title = "视文件")
public class FileListActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener, ServiceConnection, IFileUploadListener, IFtpCallback {

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
    private List<FileBean> data;
    private UploadFileService.FileUploadBinder fileUploadBinder;


    @Override
    public void initData() {
        super.initData();
        Intent intent = new Intent(this, UploadFileService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    private void restartService() {
        Intent intent = new Intent(this, UploadFileService.class);
        startService(intent);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        uploadTv.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        errorHintGroup.setOnClickListener(this);

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
            f.setUpStatus("未知");
            f.setFileSize(UnitHelper.formatterFileSize(file1.length()));
            l.add(f);
        }
        return l;
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
                for (FileBean datum : data) {
                    boolean isServiceHas = false;
                    try {
                        isServiceHas = fileUploadBinder.isServiceHasFile(new File(datum.getFilePath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (isServiceHas) {
                        datum.setUpStatus("已上传");
                        continue;
                    }
                    UploadFile uploadFile = UploadFile.selectByFilePath(datum.getFilePath());
                    if (uploadFile != null && UploadFile.UPLOAD_READY.equals(uploadFile.getStatus())) {
                        datum.setUpStatus("正在上传");
                        continue;
                    }

                    datum.setUpStatus("未上传");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setNewData(data);
                        adapter.notifyDataSetChanged();
                    }
                });
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
                        UpFile upFile = new UpFile();
                        upFile.setFile(new File(datum.getFilePath()));
                        fileUploadBinder.addUploadFile(upFile);
                    }
                }
                checkFiles();
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
        }
    }

    private void changeData() {
        data = createDate();
        adapter.setNewData(data);
        adapter.notifyDataSetChanged();
        int i = data.size() == 0 ? View.VISIBLE : View.GONE;
        nullGroup.setVisibility(i);
    }
    /****************************************/
    /****************启动文件服务*****************/
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        fileUploadBinder = (UploadFileService.FileUploadBinder) service;
        fileUploadBinder.addFileUploadListener(this);
        fileUploadBinder.addFtpCallback(this);

        if (!fileUploadBinder.isFtpConnect()) {
            errorHintGroup.setVisibility(View.VISIBLE);
            hintTv.setText("启动ftp服务中");
            restartService();
            return;
        }
        errorHintGroup.setVisibility(View.GONE);
        checkFiles();

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    /****************************************/
    /****************文件长传回调Start******************/
    @Override
    public void uploadFileStart(File lastFile) {

    }

    @Override
    public void uploadFileError(File lastFile, Exception e) {

    }

    @Override
    public void uploadFileSuccess(File lastFile) {
        checkFiles();
    }

    @Override
    public void uploadingFile(float progress, long total, File lastFile) {
        LogUtils.i(getClass().getName(), progress + "----" + total + "---" + lastFile.getAbsolutePath());
    }

    /***************文件上传回调end*************/
    /****************************************/

    @Override
    public void ftpFailed(String error) {
        errorHintGroup.setVisibility(View.VISIBLE);
        hintTv.setText(error);
    }

    @Override
    public void ftpConnectSuccess() {
        errorHintGroup.setVisibility(View.GONE);
        checkFiles();
    }

    @Override
    public void ftpLoginFailed(String msg) {
        errorHintGroup.setVisibility(View.VISIBLE);
        hintTv.setText(msg);
    }

}
