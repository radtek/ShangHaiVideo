package nss.mobile.video.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;

public class UploadFileService extends Service implements IFileUploadListener {
    private final UploadBinder uploadBinder = new UploadBinder();
    private UploadListener mListener;
    private DataRequest mDataRequest;
    private UploadRunnable mUploadRunnable;
    private Thread runThread;

    private String mUploadUrl;

    public UploadFileService() {
        mUploadRunnable = new UploadRunnable(this, mUploadUrl, mDataRequest);
        runThread = new Thread(mUploadRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return uploadBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (runThread.isAlive()) {
            return super.onStartCommand(intent, flags, startId);
        }
        runThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void start() {
        if (mListener == null) {
            return;
        }
        mListener.uploadStart();
    }

    @Override
    public void uploadFileStart(File lastFile) {
        if (mListener == null) {
            return;
        }
        mListener.progress(0, lastFile.length(), lastFile, mDataRequest.getUploadIndex(), mDataRequest.getTotalUploadFile());

    }

    @Override
    public void uploadFileError(File lastFile, Exception e) {
        mDataRequest.putFailedFile(lastFile);
    }

    @Override
    public void uploadFileSuccess(File lastFile) {
        mDataRequest.putSuccessFile(lastFile);
    }

    @Override
    public void uploadingFile(float progress, long total, File lastFile) {
        if (mListener == null) {
            return;
        }
        mListener.progress(progress, total, lastFile, mDataRequest.getUploadIndex(), mDataRequest.getTotalUploadFile());
    }

    @Override
    public void loadEnd() {
        //加载完成
        if (mListener == null) {
            return;
        }
        mListener.uploadEnd(mDataRequest.getSuccessFiles(), mDataRequest.getFailedFiles());
    }


    class UploadBinder extends Binder {

        void addListener(UploadListener uploadListener) {
            mListener = uploadListener;
        }

        void remove() {
            mListener = null;
        }
        
    }


    public interface UploadListener {

        void uploadEnd(List<File> successFiles, List<File> failedFiles);

        void progress(float progress, long total, File nowFile, int uploadIndex, int totalUploadFile);

        void uploadStart();
    }

    public static class UploadRunnable implements Runnable {
        private boolean isStop = false;
        private File lastFile;
        private File next;
        private IFileUploadListener loadListener;
        private String mUploadUrl;
        private DataRequest dataRequest;

        public UploadRunnable(IFileUploadListener loadListener, String mUploadUrl, DataRequest dataRequest) {
            this.loadListener = loadListener;
            this.mUploadUrl = mUploadUrl;
            this.dataRequest = dataRequest;
        }

        @Override
        public void run() {
            loadListener.start();
            while (true) {
                if (isStop) {
                    return;
                }
                if (next == null) {
                    continue;
                }
                lastFile = next;
                next = null;
                dataRequest.setUploadingFile(lastFile);
                OkHttpUtils.postFile().url(mUploadUrl)
                        .file(lastFile)
                        .build()
                        .execute(new StringCallback() {

                            @Override
                            public void onBefore(Request request, int id) {
                                super.onBefore(request, id);
                                loadListener.uploadFileStart(dataRequest.getUploadingFile());
                            }

                            @Override
                            public void inProgress(float progress, long total, int id) {
                                super.inProgress(progress, total, id);
                                loadListener.uploadingFile(progress, total, dataRequest.getUploadingFile());
                            }

                            @Override
                            public void onError(Call call, Exception e, int id) {
                                loadListener.uploadFileError(lastFile, e);
                            }

                            @Override
                            public void onResponse(String response, int id, int code) {
                                loadListener.uploadFileSuccess(dataRequest.getUploadingFile());
                            }

                            @Override
                            public void onAfter(int id) {
                                File next = dataRequest.next();
                                dataRequest.setUploadingFile(next);
                                UploadRunnable.this.next = next;
                                if (next == null) {
                                    loadEnd();
                                }
                            }
                        });
            }
        }

        private void loadEnd() {
            loadListener.loadEnd();
        }
    }

    private class DataRequest {
        private File mUploadingFile;

        private List<File> mNeedUploadFiles = new ArrayList<>();
        private List<File> mFailedFiles = new ArrayList<>();
        private List<File> mSuccessFiles = new ArrayList<>();

        public File getUploadingFile() {
            return mUploadingFile;
        }

        public void setUploadingFile(File mUploadingFile) {
            this.mUploadingFile = mUploadingFile;
        }

        File next() {
            if (mNeedUploadFiles == null) {
                return null;
            }
            if (mNeedUploadFiles.size() == 0) {
                return null;
            }
            File file = mNeedUploadFiles.get(0);
            mNeedUploadFiles.remove(0);
            return file;
        }

        public void put(File file) {
            if (mNeedUploadFiles.contains(file)) {
                return;
            }
            mNeedUploadFiles.add(file);
        }

        public void putFailedFile(File file) {
            if (mFailedFiles.contains(file)) {
                return;
            }
            mFailedFiles.add(file);
        }

        public List<File> getFailedFiles() {
            return mFailedFiles;
        }

        public void remveFailedFile(File file) {
            mFailedFiles.remove(file);
        }


        public void putSuccessFile(File lastFile) {
            if (mSuccessFiles.contains(lastFile)) {
                return;
            }
            mSuccessFiles.add(lastFile);
        }

        public int getTotalUploadFile() {
            return mNeedUploadFiles.size() + mFailedFiles.size() + mSuccessFiles.size();
        }

        public int getUploadIndex() {
            if (mUploadingFile == null) {
                return getTotalUploadFile();
            }
            return mFailedFiles.size() + mSuccessFiles.size() + 1;
        }

        public List<File> getSuccessFiles() {
            return mSuccessFiles;
        }
    }

    private interface ILoadListener {


    }
}
