package nss.mobile.video.service.ali;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nss.mobile.video.C;
import nss.mobile.video.bean.AliFileBean;
import nss.mobile.video.bean.MobileKeyBean;
import nss.mobile.video.http.OkHttpHelper;
import nss.mobile.video.http.ali.AliApiHelper;
import nss.mobile.video.http.ali.AliOssToken;
import nss.mobile.video.http.ali.AliOssUploadFileHelper;
import nss.mobile.video.http.ali.AliPlayInfo;
import nss.mobile.video.http.ali.AliPlayInfoResult;
import nss.mobile.video.http.ali.AliVideoBean;
import nss.mobile.video.http.ali.AliVideoDetails;
import nss.mobile.video.http.ali.OnAliVideoPlayInfoListener;
import nss.mobile.video.http.ali.UploadAddress;
import nss.mobile.video.http.ali.UploadAuth;
import nss.mobile.video.info.UrlApi;
import okhttp3.Call;

public class AliUploadFileService extends Service {
    UploadFileBinder uploadFileBinder = new UploadFileBinder();
    private List<File> files = new ArrayList<>();
    private List<File> mSuccessFiles = new ArrayList<>();
    private List<File> mFailedFiles = new ArrayList<>();
    private Map<File, OSSAsyncTask> mTasks = new HashMap<>();
    private Map<String, Map<String, Object>> mParams = new HashMap<>();

    private List<OnUploadFileListener> mOnUploadFileListener = new ArrayList<>();
    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private boolean start = false;

    public AliUploadFileService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return uploadFileBinder;
    }

    private void startUpload() {
        if (start) {
            return;
        }
        for (File file : files) {
            AliFileBean f = AliFileBean.getFile(file.getAbsolutePath());
            if (f == null) {
                f = new AliFileBean();
                f.setFilePath(file.getAbsolutePath());
                f.setFileName(file.getName());
                long l1 = file.lastModified();
                f.setStartTime(mSimpleDateFormat.format(new Date(l1)));
            }
            f.setUpstatus(AliFileBean.STATUS_WANTING);
            f.saveSingle();
        }
        for (OnUploadFileListener onUploadFileListener : mOnUploadFileListener) {
            onUploadFileListener.onStart(files);
        }

        if (files.size() == 0) {

            for (OnUploadFileListener onUploadFileListener : mOnUploadFileListener) {
                onUploadFileListener.onUploadEnd();
            }
            return;
        }
        start = true;
        File file = files.get(0);
        for (OnUploadFileListener onUploadFileListener : mOnUploadFileListener) {
            onUploadFileListener.onStartUploadFile(file);
        }
        uploadFile(file);
    }

    private void uploadFile(File file) {
        final File uploadFile = file;
        AliFileBean f = AliFileBean.getFile(file.getAbsolutePath());
        f.setUpstatus(AliFileBean.STATUS_UPLOADING);
        f.saveSingle();
        AliApiHelper.getToken(new AliApiHelper.OnLoadAliOssTokenListener() {
            @Override
            public void onAliOssLoad(AliOssToken aliOssToken, UploadAddress uploadAddress, UploadAuth uploadAuth) {
                AliOssUploadFileHelper ossHelper =
                        new AliOssUploadFileHelper(uploadAuth.getAccessKeyId()
                                , uploadAddress.getBucket()
                                , uploadAuth.getSecurityToken()
                                , uploadAuth.getAccessKeySecret()
                                , uploadAuth.getRegion()
                                , uploadAddress.getFileName()
                                , uploadAddress.getEndpoint());
                AliFileBean f = AliFileBean.getFile(uploadFile.getAbsolutePath());
                f.setAliVideoId(aliOssToken.getVideoId());
                f.saveSingle();

                bindVideoIdToActionServer(aliOssToken, uploadFile);

                for (OnUploadFileListener onUploadFileListener : mOnUploadFileListener) {
                    onUploadFileListener.bindVideoId(uploadFile, aliOssToken);
                }
                C.sTHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ossHelper.resetInit(AliUploadFileService.this);
                        OSSAsyncTask ossAsyncTask = ossHelper.uploadFile(uploadFile, new OSSProgressCallback<PutObjectRequest>() {
                            @Override
                            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                                for (OnUploadFileListener onUploadFileListener : mOnUploadFileListener) {
                                    onUploadFileListener.onProgress(request, currentSize, totalSize, uploadFile, files);
                                }

                            }
                        }, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                            @Override
                            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                                AliFileBean f = AliFileBean.getFile(uploadFile.getAbsolutePath());
                                f.setUpstatus(AliFileBean.STATUS_OVER);
                                f.saveSingle();

                                uploadFileSuccessToServer(uploadFile);

                                for (OnUploadFileListener onUploadFileListener : mOnUploadFileListener) {
                                    onUploadFileListener.onSuccess(request, result, uploadFile, files);
                                }
                                uploadFileSuccess(uploadFile);
                                nextUploadFile();
                            }

                            @Override
                            public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                                AliFileBean f = AliFileBean.getFile(uploadFile.getAbsolutePath());
                                f.setUpstatus(AliFileBean.STATUS_ERROR);
                                f.saveSingle();
                                uploadFileFailedToActionServer(uploadFile);

                                for (OnUploadFileListener onUploadFileListener : mOnUploadFileListener) {
                                    onUploadFileListener.onFailedFile(uploadFile);
                                }
                                uploadFileFailed(uploadFile);
                                nextUploadFile();
                            }
                        });
                        mTasks.put(file, ossAsyncTask);
                    }
                });

            }

            @Override
            public void onAliOssFailed(Exception e, String hint) {
                for (File file1 : files) {
                    AliFileBean f = AliFileBean.getFile(file1.getAbsolutePath());
                    f.setUpstatus(AliFileBean.STATUS_ERROR);
                    f.saveSingle();
                }
                start = false;
                for (OnUploadFileListener onUploadFileListener : mOnUploadFileListener) {
                    onUploadFileListener.onFailedStop(e, hint);
                }
            }
        });

    }

    private void uploadFileFailedToActionServer(File uploadFile) {
        Map<String, Object> params = new HashMap<>();
        MobileKeyBean last = MobileKeyBean.getLast();
        params.put("box-code", last.getMobileKey());
        AliFileBean file = AliFileBean.getFile(uploadFile.getAbsolutePath());
        long value = 0;
        if (file != null) {
            value = file.geteDate();
        } else {
            value = System.currentTimeMillis() / 1000;
        }
        params.put("end-at", value);
        params.put("filename", uploadFile.getName());
        params.put("done", "失败");
        Map<String, Object> map = mParams.get(uploadFile.getAbsolutePath());
        if (map == null) {
            map = params;
        } else {
            map.putAll(params);
        }
        OkHttpHelper.post(UrlApi.file_status_submit, map, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id, int code) {

            }
        });
    }


    /**
     * 获得到的 视频信息，上传到服务，通知绑定成功
     *
     * @param aliOssToken
     * @param uploadFile
     */
    private void bindVideoIdToActionServer(AliOssToken aliOssToken, File uploadFile) {
        Map<String, Object> params = new HashMap<>();
        MobileKeyBean last = MobileKeyBean.getLast();
        params.put("box-code", last.getMobileKey());
        params.put("filename", uploadFile.getName());
        AliFileBean file = AliFileBean.getFile(uploadFile.getAbsolutePath());
        long value = 0;
        if (file != null) {
            value = file.getsDate();
        } else {
            value = System.currentTimeMillis() / 1000;
        }
        params.put("begin-at", value);
        params.put("size", uploadFile.length());
        params.put("oss-guid", aliOssToken.getVideoId());
        Map<String, Object> map = mParams.get(uploadFile.getAbsolutePath());
        if (map == null) {
            map = params;
        } else {
            map.putAll(params);
        }
        mParams.put(uploadFile.getAbsolutePath(), map);
    }

    /**
     * 上传成功后，上传到服务器
     *
     * @param uploadFile
     */
    private void uploadFileSuccessToServer(File uploadFile) {
        AliFileBean mFile = AliFileBean.getFile(uploadFile.getAbsolutePath());
        AliApiHelper.getVideoId(mFile.getAliVideoId(), new AliApiHelper.OnLoadAliVideoDetailsListener() {
            @Override
            public void onAliVideoDetails(AliVideoDetails videoDetails) {
                AliVideoBean video = videoDetails.getVideo();

                final Map<String, Object> params = new HashMap<>();
                MobileKeyBean last = MobileKeyBean.getLast();
                params.put("box-code", last.getMobileKey());
                AliFileBean file = AliFileBean.getFile(uploadFile.getAbsolutePath());
                long value = 0;
                if (file != null) {
                    value = file.geteDate();
                } else {
                    value = System.currentTimeMillis() / 1000;
                }
                params.put("end-at", value);
                params.put("duration", (int) video.getDuration());
                params.put("view-preview", video.getCoverURL());
                //                            params.put("viewVod",);//点播地址
                params.put("filename", mFile.getFileName().toString());//文件名称
                Map<String, Object> map = mParams.get(uploadFile.getAbsolutePath());
                if (map == null) {
                    map = params;
                } else {
                    map.putAll(params);
                }
                mParams.put(uploadFile.getAbsolutePath(), map);
                AliApiHelper.getVideoPlayUrl(mFile.getAliVideoId(), new OnAliVideoPlayInfoListener() {
                    @Override
                    public void onAliVideoPlaySuccess(AliPlayInfoResult aliPlayInfoResult) {
                        AliPlayInfoResult.PlayInfoListBean playInfoList = aliPlayInfoResult.getPlayInfoList();
                        if (playInfoList != null) {
                            List<AliPlayInfo> playInfo = playInfoList.getPlayInfo();
                            if (playInfo != null && playInfo.size() > 0) {
                                AliPlayInfo aliPlayInfo = playInfo.get(0);
                                final Map<String, Object> params = new HashMap<>();
                                MobileKeyBean last = MobileKeyBean.getLast();
                                params.put("box-code", last.getMobileKey());
                                params.put("done", "完成");
                                params.put("viewVod", aliPlayInfo.getPlayURL());//点播地址
                                params.put("filename", uploadFile.getName());//点播地址

                                Map<String, Object> map = mParams.get(uploadFile.getAbsolutePath());
                                if (map == null) {
                                    map = params;
                                } else {
                                    map.putAll(params);
                                }

                                mFile.setVideVod(aliPlayInfo.getPlayURL());
                                mFile.setSubmitServerStatus(1);
                                mFile.saveSingle();
                                //                                    params.put("viewDownload", );//下载地址
                                OkHttpHelper.post(UrlApi.file_status_submit, map, new StringCallback() {
                                    @Override
                                    public void onError(Call call, Exception e, int id) {

                                    }

                                    @Override
                                    public void onResponse(String response, int id, int code) {

                                    }
                                });
                                mParams.remove(uploadFile.getAbsolutePath());
                            }
                        }

                    }

                    @Override
                    public void onAliVideoPlayFailed(Exception e, String hint) {

                    }
                });
            }

            @Override
            public void onAliVideoFailed(Exception e, String hint) {

            }
        });


    }

    private void uploadFileFailed(File uploadFile) {
        AliFileBean f = AliFileBean.getFile(uploadFile.getAbsolutePath());
        f.setUpstatus(AliFileBean.STATUS_ERROR);
        f.saveSingle();

        mTasks.remove(uploadFile);
        mFailedFiles.add(uploadFile);
        for (File file : files) {
            if (file.getAbsolutePath().equals(uploadFile)) {
                files.remove(file);
                return;
            }
        }
    }

    private void uploadFileSuccess(File uploadFile) {
        AliFileBean f = AliFileBean.getFile(uploadFile.getAbsolutePath());
        f.setUpstatus(AliFileBean.STATUS_OVER);
        f.saveSingle();

        mTasks.remove(uploadFile);
        mSuccessFiles.add(uploadFile);
        for (File file : files) {
            if (file.getAbsolutePath().equals(uploadFile.getAbsolutePath())) {
                files.remove(file);
                return;
            }
        }
    }

    private void nextUploadFile() {
        start = false;
        if (files.size() == 0) {
            for (OnUploadFileListener onUploadFileListener : mOnUploadFileListener) {
                onUploadFileListener.onUploadEnd();
            }
            return;
        }
        File file = files.get(0);
        for (OnUploadFileListener onUploadFileListener : mOnUploadFileListener) {
            onUploadFileListener.onStartUploadFile(file);
        }
        uploadFile(file);
    }


    public class UploadFileBinder extends Binder implements IUploadFileService {


        @Override
        public void startUploadFile() {
            startUpload();
        }

        @Override
        public void removeFile(File file) {
            files.remove(file);
        }

        @Override
        public void addFile(File file) {
            if (files.indexOf(file) != -1) {
                return;
            }
            files.add(file);
        }

        @Override
        public void addFiles(List<File> files) {
            for (File file : files) {
                if (AliUploadFileService.this.files.indexOf(file) != -1) {
                    continue;
                }
                AliUploadFileService.this.files.add(file);
            }
        }

        @Override
        public void stopUpLoadFile() {
            start = false;
            Set<File> files = mTasks.keySet();
            for (File file : files) {
                OSSAsyncTask ossAsyncTask = mTasks.get(file);
                if (!ossAsyncTask.isCanceled()) {
                    ossAsyncTask.cancel();
                }
            }
        }

        @Override
        public void addListener(OnUploadFileListener l) {
            mOnUploadFileListener.add(l);
        }

        @Override
        public void removeListener(OnUploadFileListener l) {
            mOnUploadFileListener.remove(l);
        }

    }

    public interface OnUploadFileListener {

        void onStart(List<File> list);

        void onProgress(PutObjectRequest request, long currentSize, long totalSize, File uploadFile, List<File> files);

        void onSuccess(PutObjectRequest request, PutObjectResult result, File uploadFile, List<File> files);

        void onUploadEnd();

        void onStartUploadFile(File file);

        void bindVideoId(File uploadFile, AliOssToken aliOssToken);

        void onFailedFile(File uploadFile);

        void onFailedStop(Exception e, String hint);


    }

}
