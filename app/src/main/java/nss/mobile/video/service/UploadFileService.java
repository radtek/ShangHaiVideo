package nss.mobile.video.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.TelephonyManager;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nss.mobile.video.C;
import nss.mobile.video.bean.db.UploadFile;
import nss.mobile.video.http.ftp.FTPConfig;
import nss.mobile.video.http.ftp.FTPHelper;

public class UploadFileService extends Service {

    private Thread runThread;
    private FileUploadBinder binder;
    private FTPHelper mFTPHelper;
    private Handler mHandler;
    private FTPConfig ftpConfig;
    private FileBox fileBox;
    private boolean isConnecting;
    private boolean isUploadFile;
    private IFileUploadListener uploadListener = new IFileUploadListener() {
        @Override
        public void uploadFileStart(File lastFile) {
            C.sHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IFileUploadListener uploadListener : binder.uploadListeners) {
                        uploadListener.uploadFileStart(lastFile);
                    }
                }
            });

        }

        @Override
        public void uploadFileError(File lastFile, Exception e) {
            C.sHandler.post(new Runnable() {
                @Override
                public void run() {
                    UploadFile uploadFile = UploadFile.selectByFilePath(lastFile.getAbsolutePath());
                    if (uploadFile == null) {
                        uploadFile = new UploadFile();
                        uploadFile.setFilePath(lastFile.getAbsolutePath());
                        uploadFile.setStatus(UploadFile.UPLOAD_FAILED);
                        uploadFile.save();
                    } else {
                        uploadFile.setStatus(UploadFile.UPLOAD_FAILED);
                        uploadFile.update(uploadFile.getId());
                    }
                    for (IFileUploadListener uploadListener : binder.uploadListeners) {
                        uploadListener.uploadFileError(lastFile, e);
                    }
                }
            });

        }

        @Override
        public void uploadFileSuccess(File lastFile) {
            C.sHandler.post(new Runnable() {
                @Override
                public void run() {
                    UploadFile uploadFile = UploadFile.selectByFilePath(lastFile.getAbsolutePath());
                    if (uploadFile == null) {
                        uploadFile = new UploadFile();
                        uploadFile.setFilePath(lastFile.getAbsolutePath());
                        uploadFile.setStatus(UploadFile.UPLOAD_END);
                        uploadFile.save();
                    } else {
                        uploadFile.setStatus(UploadFile.UPLOAD_END);
                        uploadFile.update(uploadFile.getId());
                    }
                    for (IFileUploadListener uploadListener : binder.uploadListeners) {
                        uploadListener.uploadFileSuccess(lastFile);
                    }
                }
            });


        }

        @Override
        public void uploadingFile(float progress, long total, File lastFile) {
            C.sHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IFileUploadListener uploadListener : binder.uploadListeners) {
                        uploadListener.uploadingFile(progress, total, lastFile);
                    }
                }
            });

        }
    };

    public UploadFileService() {
        mFTPHelper = new FTPHelper();
        binder = new FileUploadBinder();
        ftpConfig = new FTPConfig();
        fileBox = new FileBox();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mFTPHelper.isConnect() && !isConnecting) {
            runThread = new Thread(new FTPConnectRunnable());
            runThread.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private String getSaveAbsPath(String fileName) {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String imei = telephonyManager.getDeviceId();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String format = simpleDateFormat.format(new Date());
        String s = "/" + imei + "/" + format + "/" + fileName;
        return s;
    }


    private void uploadNext() {
        if (!mFTPHelper.isConnect()) {
            isUploadFile = false;
            for (IFtpCallback callback : binder.callbacks) {
                callback.ftpFailed("ftp已经断开连接请重新连接");
            }
            return;
        }
        UpFile next = fileBox.next();
        if (next == null) {
            isUploadFile = false;
            return;
        }
        isUploadFile = true;
        mHandler.post(new FTPUploadFileRunnable(next.getFile(), getSaveAbsPath(next.getFile().getName()), uploadListener));
    }


    public class FileUploadBinder extends Binder {
        boolean uploading = false;
        private List<IFtpCallback> callbacks = new ArrayList<>();
        private List<IFileUploadListener> uploadListeners = new ArrayList<>();

        public void addFileUploadListener(IFileUploadListener listener) {
            uploadListeners.add(listener);
        }

        public void addFtpCallback(IFtpCallback callback) {
            callbacks.add(callback);
        }

        public void removeFtpCallback(IFtpCallback callback) {
            callbacks.remove(callback);
        }

        public void addUploadFile(UpFile localFile) {
            fileBox.addFile(localFile);
            if (!isUploadFile) {
                uploadNext();
            }
            UploadFile fileDb = new UploadFile();
            fileDb.setFilePath(localFile.getFile().getAbsolutePath());
            fileDb.setStatus(UploadFile.UPLOAD_READY);
            UploadFile uploadFile = UploadFile.selectByFilePath(localFile.getFile().getAbsolutePath());
            if (uploadFile != null) {
                fileDb.update(uploadFile.getId());
                return;
            }
            fileDb.save();

        }

        public void stopUpload() {
            isUploadFile = false;
            mFTPHelper.setStopUpload(true);
        }

        public void removeFile(File localFile) {
            fileBox.remove(localFile);
        }

        public boolean isServiceHasFile(File file) throws IOException {
            if (!mFTPHelper.isConnect()) {
                return false;
            }
            return mFTPHelper.isHasFile(file);
        }

        public boolean isFtpConnect() {
            return mFTPHelper.isConnect();
        }

        public void startUpload() {
            mFTPHelper.setStopUpload(false);
            if (!isFtpConnect()) {
                runThread.start();
                return;
            }
            uploadNext();
        }

        public void stopUploadByFile(String filePath) {
            mFTPHelper.setStopFile(filePath);

        }

        public boolean isUploading() {
            return isUploadFile;
        }
    }

    class FTPConnectRunnable implements Runnable {

        @Override
        public void run() {
            try {
                isConnecting = true;
                mFTPHelper.connectFtp(ftpConfig.getUrl(), ftpConfig.getPort());
                boolean login = mFTPHelper.login(ftpConfig.getAccount(), ftpConfig.getPw());
                if (!login) {
                    handlerLoginFailed();
                    return;
                }
                handlerFtpConnectSuccess();
                if (mHandler != null) {
                    mHandler.removeCallbacks(null);
                    mHandler = null;
                }
                Looper.prepare();
                mHandler = new Handler(Looper.myLooper());
                Looper.loop();
                if (binder.uploading) {
                    uploadNext();
                }
            } catch (IOException e) {
                e.printStackTrace();
                handlerFtpFailed(e);
            } finally {
                isConnecting = false;
            }
        }
    }

    private void handlerFtpFailed(IOException e) {
        isUploadFile = false;
        C.sHandler.post(new Runnable() {
            @Override
            public void run() {
                for (IFtpCallback callback : binder.callbacks) {
                    callback.ftpFailed(e.getMessage());
                }
            }
        });

    }

    private void handlerFtpConnectSuccess() {
        C.sHandler.post(new Runnable() {
            @Override
            public void run() {
                for (IFtpCallback callback : binder.callbacks) {
                    callback.ftpConnectSuccess();
                }
            }
        });

    }

    /**
     * ftp登录失败
     */
    private void handlerLoginFailed() {
        isUploadFile = false;
        C.sHandler.post(new Runnable() {
            @Override
            public void run() {

                for (IFtpCallback callback : binder.callbacks) {
                    callback.ftpLoginFailed("账号密码错误");
                }
            }
        });


    }

    class FTPUploadFileRunnable implements Runnable {
        File uploadFile;
        String serviceFile;
        IFileUploadListener listener;

        public FTPUploadFileRunnable(File uploadFile, String serviceFile, IFileUploadListener listener) {
            this.uploadFile = uploadFile;
            this.serviceFile = serviceFile;
            this.listener = listener;
        }

        @Override
        public void run() {
            try {
                listener.uploadFileStart(uploadFile);
                boolean b = mFTPHelper.uploadFile(uploadFile, serviceFile, new FTPHelper.IFileUploadPressListener() {
                    @Override
                    public void onProcess(long currentSize, long localSize, File localPath) {
                        listener.uploadingFile(currentSize * 1.0f / localSize, localSize, localPath);
                    }
                });
                if (b) {
                    listener.uploadFileSuccess(uploadFile);
                    return;
                }
                listener.uploadFileError(uploadFile, new RuntimeException("上传失败"));

            } catch (IOException e) {
                e.printStackTrace();
                listener.uploadFileError(uploadFile, e);
            } finally {
                uploadNext();
            }
        }

    }
}
