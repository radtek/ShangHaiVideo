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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUIEmptyView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nss.mobile.video.C;
import nss.mobile.video.R;
import nss.mobile.video.base.BaseActivity;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.bean.AliFileBean;
import nss.mobile.video.bean.MobileKeyBean;
import nss.mobile.video.http.OkHttpHelper;
import nss.mobile.video.http.ali.AliApiHelper;
import nss.mobile.video.http.ali.AliOssToken;
import nss.mobile.video.http.ali.AliPlayInfo;
import nss.mobile.video.http.ali.AliPlayInfoResult;
import nss.mobile.video.http.ali.AliVideoBean;
import nss.mobile.video.http.ali.AliVideoDetails;
import nss.mobile.video.http.ali.OnAliVideoPlayInfoListener;
import nss.mobile.video.info.UrlApi;
import nss.mobile.video.service.ali.AliUploadFileService;
import nss.mobile.video.ui.adapter.AliFileAdapter;
import nss.mobile.video.utils.UnitHelper;
import nss.mobile.video.utils.div.DividerItemDecoration;
import nss.mobile.video.video.VideoFile;
import okhttp3.Call;

@BindLayout(layoutRes = R.layout.activity_ali_file, title = "阿里云上传")
public class AliFileActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener, ServiceConnection, AliUploadFileService.OnUploadFileListener {

    @BindView(R.id.ali_file_rv)
    RecyclerView fileRv;
    @BindView(R.id.alifile_list_select_all_tv)
    TextView mSelectAllTv;
    @BindView(R.id.alifile_list_delete_tv)
    TextView mDeleteTv;
    @BindView(R.id.alifile_list_upload_tv)
    TextView mUploadTv;
    @BindView(R.id.alifile_list_check_file_tv)
    TextView mCheckFileTv;
    @BindView(R.id.alifile_operate_group)
    ViewGroup mOperateGroup;


    private AliFileAdapter mFileAdapter = new AliFileAdapter();
    private List<AliFileBean> mFiles;
    private Button topBarRightBtn;
    private AliUploadFileService.UploadFileBinder binder;
    private AliFileBean mNowUploadFile;

    @Override
    public void initBar() {
        super.initBar();
        topBarRightBtn = mTopBar.addRightTextButton("操作", R.id.top_bar_right_btn);
        topBarRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean setting = !mFileAdapter.isSetting();
                mFileAdapter.setSetting(setting);
                int i = setting ? View.VISIBLE : View.GONE;
                mOperateGroup.setVisibility(i);
                String s = setting ? "完成" : "操作";
                topBarRightBtn.setText(s);
                mFileAdapter.notifyDataSetChanged();
            }
        });
        bindUploadService();
    }

    private void bindUploadService() {
        Intent intent = new Intent(this, AliUploadFileService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        mSelectAllTv.setOnClickListener(this);
        mDeleteTv.setOnClickListener(this);
        mCheckFileTv.setOnClickListener(this);
        mUploadTv.setOnClickListener(this);

        fileRv.setLayoutManager(new LinearLayoutManager(this));
        fileRv.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL, 10, Color.GRAY));

        fileRv.setAdapter(mFileAdapter);
        QMUIEmptyView qmuiEmptyView = new QMUIEmptyView(this);
        qmuiEmptyView.show(false, "提示信息", "暂无文件", "重新获得文件", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFiles = loadFile();
                List list = mFiles;
                mFileAdapter.setNewData(list);
                mFileAdapter.notifyDataSetChanged();
            }
        });
        mFileAdapter.setEmptyView(qmuiEmptyView);

        View view = new View(this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, QMUIDisplayHelper.dp2px(this, 32));
        view.setLayoutParams(lp);
        mFileAdapter.addFooterView(view);

        mFiles = loadFile();
        List list = mFiles;
        mFileAdapter.setNewData(list);
        mFileAdapter.notifyDataSetChanged();
        displayLoadingDialog("加载数据中");
        checkFile();

        mFileAdapter.setOnItemClickListener(this);
    }

    @Override
    public void forbidClick(View v) {
        super.forbidClick(v);
        if (v.getId() == mDeleteTv.getId()) {
            actionDelete();
        } else if (v.getId() == mSelectAllTv.getId()) {
            actionSelectStatusChange();
        } else if (v.getId() == mUploadTv.getId()) {
            actionUpload();
        } else if (v.getId() == mCheckFileTv.getId()) {
            displayLoadingDialog("检测文件中");
            checkFile();
        }
    }

    /**
     * 上传文件
     * 文件是否上传;
     */
    private void actionUpload() {
        for (AliFileBean mFile : mFiles) {
            if (mFile.isSelect()) {
                binder.addFile(new File(mFile.getFilePath().toString()));
            }
        }
        binder.startUploadFile();
    }

    private void actionDelete() {
        for (AliFileBean mFile : mFiles) {
            if (mFile.isSelect() && AliFileBean.STATUS_UPLOADING.equals(mFile.getUpStatus().toString())) {
                displayMessageDialog("不能删除正在上传文件");
                return;
            }
        }

        displayLoadingDialog("删除文件中");
        C.sTHandler.post(new Runnable() {
            @Override
            public void run() {
                for (AliFileBean mFile : mFiles) {
                    if (mFile.isSelect()) {
                        if (AliFileBean.STATUS_WANTING.equals(mFile.getUpStatus().toString())) {
                            binder.removeFile(new File(mFile.getFilePath().toString()));
                            mFile.delete();
                        }
                        File file = new File(mFile.getFilePath().toString());
                        file.delete();
                    }
                }
                List list = loadFile();
                mFiles = list;

                C.sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mFileAdapter.setNewData(list);
                        mFileAdapter.notifyDataSetChanged();
                        cancelLoadingDialog();
                    }
                });
            }
        });


    }

    private void actionSelectStatusChange() {
        if (mSelectAllTv.isSelected()) {
            actionCancelSelectAll();
            boolean b = checkSelectStatus();
            String s = b ? "反选" : "全选";
            mSelectAllTv.setText(s);
            mSelectAllTv.setSelected(b);
        } else {
            actionSelectAll();
            boolean b = checkSelectStatus();
            String s = b ? "反选" : "全选";
            mSelectAllTv.setText(s);
            mSelectAllTv.setSelected(b);
        }
        mFileAdapter.notifyDataSetChanged();
    }

    private void actionCancelSelectAll() {
        for (AliFileBean mFile : mFiles) {
            mFile.setSelect(false);
        }
    }

    private void actionSelectAll() {
        for (AliFileBean mFile : mFiles) {
            mFile.setSelect(true);
        }
    }

    private List loadFile() {
        File file = VideoFile.baseFile();
        File[] files = file.listFiles();
        List list = new ArrayList();
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (File file1 : files) {

            AliFileBean f = new AliFileBean();
            f.setFileName(file1.getName());
            long l1 = file1.lastModified();
            f.setStartTime(s.format(new Date(l1)));
            f.setUpstatus(AliFileBean.STATUS_NORMAL);
            f.setFilePath(file1.getAbsolutePath());
            f.setFileSize(UnitHelper.formatterFileSize(file1.length()));
            list.add(f);
        }
        return list;
    }

    private void checkFile() {
        for (AliFileBean mFile : mFiles) {
            AliFileBean status = AliFileBean.getFile(mFile);
            if (status == null) {
                continue;
            }
            mFile.setUpstatus(status.getUpStatus().toString());
        }

        mFileAdapter.notifyDataSetChanged();
        cancelLoadingDialog();
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        AliFileBean aliFileBean1 = mFiles.get(position);
        if (mFileAdapter.isSetting()) {
            AliFileBean aliFileBean = aliFileBean1;
            aliFileBean.setSelect(!aliFileBean.isSelect());
            mFileAdapter.notifyItemChanged(position);
            boolean b = checkSelectStatus();
            String s = b ? "反选" : "全选";
            mSelectAllTv.setSelected(b);

            mSelectAllTv.setText(s);
            mFileAdapter.notifyDataSetChanged();
            return;
        }
        Bundle bundle = new Bundle();
        VideoPlayActivity.put(aliFileBean1.getFilePath().toString(), bundle);
        startActivity(VideoPlayActivity.class, bundle);

    }

    private boolean checkSelectStatus() {
        for (AliFileBean mFile : mFiles) {
            if (!mFile.isSelect()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (AliUploadFileService.UploadFileBinder) service;
        binder.addListener(this);
        bindServerSuccess();
    }

    private void bindServerSuccess() {
        toast("启动服务成功");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    protected void onDestroy() {
        binder.removeListener(this);
        super.onDestroy();
    }

    /*************************************/
    /**************文件下载监听器**************/

    @Override
    public void onStart(List<File> list) {
        checkFile();

    }

    Runnable action = new Runnable() {
        @Override
        public void run() {
            mFileAdapter.notifyDataSetChanged();
        }
    };

    //子线程
    @Override
    public void onProgress(PutObjectRequest request, long currentSize, long totalSize, File uploadFile, List<File> files) {
        long s = currentSize * 100 / totalSize;
        mNowUploadFile.setPlayTime(String.format("进度 %d", s) + "%");

        runOnUiThread(action);

    }

    //子线程
    @Override
    public void onSuccess(PutObjectRequest request, PutObjectResult result, File uploadFile, List<File> files) {
        AliFileBean file1 = AliFileBean.getFile(uploadFile.getAbsolutePath());
        if (file1 != null) {
            for (AliFileBean mFile : mFiles) {
                if (mFile.getFilePath().equals(file1.getFilePath())) {
                    mFile.setUpstatus(file1.getUpStatus().toString());
                    mFile.saveSingle();
                    runOnUiThread(action);
                    AliApiHelper.getVideoId(mFile.getAliVideoId(), new AliApiHelper.OnLoadAliVideoDetailsListener() {
                        @Override
                        public void onAliVideoDetails(AliVideoDetails videoDetails) {

                            AliVideoBean video = videoDetails.getVideo();

                            final Map<String, Object> params = new HashMap<>();
                            MobileKeyBean last = MobileKeyBean.getLast();
                            params.put("box-code", last.getMobileKey());
                            params.put("end-at", System.currentTimeMillis());
                            params.put("duration", (int) video.getDuration());
                            params.put("done", "完成");
                            params.put("view-preview", video.getCoverURL());
                            //                            params.put("viewVod",);//点播地址
                            params.put("filename", mFile.getFileName().toString());//文件名称
//                            params.put("viewDownload",);//下载地址
                            OkHttpHelper.post(UrlApi.file_status_submit, params, new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e, int id) {

                                }

                                @Override
                                public void onResponse(String response, int id, int code) {

                                }
                            });
                        }

                        @Override
                        public void onAliVideoFailed(Exception e, String hint) {

                        }
                    });

                    AliApiHelper.getVideoPlayUrl(mFile.getAliVideoId(), new OnAliVideoPlayInfoListener() {
                        @Override
                        public void onAliVideoPlaySuccess(AliPlayInfoResult aliPlayInfoResult) {
                            AliPlayInfoResult.PlayInfoListBean playInfoList = aliPlayInfoResult.getPlayInfoList();
                            if (playInfoList != null) {
                                List<AliPlayInfo> playInfo = playInfoList.getPlayInfo();
                                if (playInfo != null && playInfo.size() > 0) {
                                    AliPlayInfo aliPlayInfo = playInfo.get(0);
                                    final Map<String, String> params = new HashMap<>();
                                    MobileKeyBean last = MobileKeyBean.getLast();
                                    params.put("box-code", last.getMobileKey());
                                    params.put("done", "完成");
                                    params.put("viewVod", aliPlayInfo.getPlayURL());//点播地址
                                    mFile.setVideVod(aliPlayInfo.getPlayURL());
                                    mFile.setSubmitServerStatus(1);
                                    mFile.saveSingle();
                                    //                                    params.put("viewDownload", );//下载地址
                                    OkHttpUtils.post().url(UrlApi.file_status_submit)
                                            .params(params)
                                            .build()
                                            .execute(new StringCallback() {
                                                @Override
                                                public void onError(Call call, Exception e, int id) {

                                                }

                                                @Override
                                                public void onResponse(String response, int id, int code) {

                                                }
                                            });
                                }
                            }

                        }

                        @Override
                        public void onAliVideoPlayFailed(Exception e, String hint) {

                        }
                    });


                    return;
                }
            }
        }
    }

    @Override
    public void onUploadEnd() {
        toast("上传结束");
        checkFile();
    }

    @Override
    public void onStartUploadFile(File file) {
        AliFileBean file1 = AliFileBean.getFile(file.getAbsolutePath());
        if (file1 != null) {
            for (AliFileBean mFile : mFiles) {
                if (mFile.getFilePath().equals(file1.getFilePath())) {
                    mFile.setUpstatus(file1.getUpStatus().toString());
                    mFile.saveSingle();
                    this.mNowUploadFile = mFile;
                    runOnUiThread(action);
                    return;
                }
            }
        }
    }

    @Override
    public void bindVideoId(File uploadFile, AliOssToken aliOssToken) {
        for (AliFileBean mFile : mFiles) {
            if (uploadFile.getAbsolutePath().equals(mFile.getFilePath())) {
                mFile.setAliVideoId(aliOssToken.getVideoId());
                mFile.saveSingle();
                Map<String, Object> params = new HashMap<>();
                MobileKeyBean last = MobileKeyBean.getLast();
                params.put("box-code", last.getMobileKey());
                params.put("begin-at", System.currentTimeMillis());
                params.put("size", uploadFile.length());
                params.put("oss-guid", aliOssToken.getVideoId());
                OkHttpHelper.post(UrlApi.file_status_submit, params, new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id, int code) {

                    }
                });
                return;
            }
        }
    }

    @Override
    public void onFailedFile(File uploadFile) {
        AliFileBean file1 = AliFileBean.getFile(uploadFile.getAbsolutePath());
        if (file1 != null) {
            for (AliFileBean mFile : mFiles) {
                if (mFile.getFilePath().equals(file1.getFilePath())) {
                    mFile.setUpstatus(file1.getUpStatus().toString());
                    mFile.saveSingle();
                    runOnUiThread(action);

                    Map<String, Object> params = new HashMap<>();
                    MobileKeyBean last = MobileKeyBean.getLast();
                    params.put("box-code", last.getMobileKey());
                    params.put("end-at", System.currentTimeMillis());
                    params.put("done", "失败");
                    OkHttpHelper.post(UrlApi.file_status_submit, params, new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {

                        }

                        @Override
                        public void onResponse(String response, int id, int code) {

                        }
                    });
                    return;
                }
            }
        }
    }

    @Override
    public void onFailedStop(Exception e, String hint) {
        checkFile();

        displayMessageDialog(hint);
    }

    /****************END*****************/
    /*************************************/
}
