package nss.mobile.video.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nss.mobile.video.C;
import nss.mobile.video.bean.db.FtpIp;
import nss.mobile.video.bean.db.UploadFile;
import nss.mobile.video.event.FileUploadChangeEvent;
import nss.mobile.video.http.ftp.FTPConfig;
import nss.mobile.video.http.ftp.FTPHelper;
import nss.mobile.video.http.ftp.UploadRemoveException;

public class UploadFileService extends Service {

    private FTPHelper ftpHelper = new FTPHelper();
    private FileBinder fileBinder = new FileBinder();
    private boolean ftpConnect;//是否正在连接
    private IFtpStatusListener iFtpStatusListener = new IFtpStatusListener() {
        @Override
        public void ftpConnectSuccess() {
            C.sHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IFtpStatusListener ftpStatusListener : fileBinder.ftpStatusListeners) {
                        ftpStatusListener.ftpConnectSuccess();
                    }
                }
            });
        }

        @Override
        public void ftpConnectFailed(Exception e) {
            C.sHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IFtpStatusListener ftpStatusListener : fileBinder.ftpStatusListeners) {
                        ftpStatusListener.ftpConnectFailed(e);
                    }
                }
            });
        }

        @Override
        public void ftpOff() {
            C.sHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IFtpStatusListener ftpStatusListener : fileBinder.ftpStatusListeners) {
                        ftpStatusListener.ftpOff();
                    }
                }
            });
        }

        @Override
        public void ftpDisconnect() {
            C.sHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IFtpStatusListener ftpStatusListener : fileBinder.ftpStatusListeners) {
                        ftpStatusListener.ftpDisconnect();
                    }
                }
            });
        }
    };
    private Handler mHandler;
    private boolean uploadFile;//是否在上传文件

    public static void reconnect(Intent intent) {
        intent.putExtra("REC", true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return fileBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mHandler == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mHandler == null) {
                        Looper.prepare();
                        mHandler = new Handler(Looper.myLooper());
                        mHandler.post(new FtpConnectRunnable());
                        Looper.loop();
                    }
                }
            }).start();
        } else if (intent != null) {
            if (intent.getBooleanExtra("REC", false) && !ftpConnect) {
                mHandler.post(new FtpConnectRunnable());
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public boolean isUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(boolean uploadFile) {
        this.uploadFile = uploadFile;
    }

    public class FileBinder extends Binder implements IUploadAction {
        private List<IFtpStatusListener> ftpStatusListeners = new ArrayList<>();
        private List<IFileUploadListener> fileUploadListeners = new ArrayList<>();

        private IFileUploadListener l = new IFileUploadListener() {
            @Override
            public void startUploadFile(File file) {
                UploadFile uploadFile = UploadFile.selectByFilePath(file.getAbsolutePath());
                uploadFile.setStatus(UploadFile.UPLOAD_ING);
                uploadFile.update(uploadFile.getId());
                C.sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (IFileUploadListener fileUploadListener : fileUploadListeners) {
                            fileUploadListener.startUploadFile(file);
                        }
                    }
                });
            }

            @Override
            public void uploadFailed(File file, Exception e) {
                UploadFile uploadFile = UploadFile.selectByFilePath(file.getAbsolutePath());
                uploadFile.setStatus(UploadFile.UPLOAD_FAILED);
                uploadFile.update(uploadFile.getId());
                C.sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (IFileUploadListener fileUploadListener : fileUploadListeners) {
                            fileUploadListener.uploadFailed(file, e);
                        }
                    }
                });
            }

            @Override
            public void uploadEnd(File file) {
                UploadFile uploadFile = UploadFile.selectByFilePath(file.getAbsolutePath());
                uploadFile.setStatus(UploadFile.UPLOAD_END);
                uploadFile.update(uploadFile.getId());
                C.sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (IFileUploadListener fileUploadListener : fileUploadListeners) {
                            fileUploadListener.uploadEnd(file);
                        }
                    }
                });
            }

            @Override
            public void uploadRemove(File localPath, UploadRemoveException e) {
                C.sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (IFileUploadListener fileUploadListener : fileUploadListeners) {
                            fileUploadListener.uploadRemove(localPath, e);
                        }
                    }
                });
            }

            @Override
            public void onAfter() {
                if (ftpHelper.isStopUpload()) {
                    return;
                }
                startUpload();
            }


            @Override
            public void onProcess(long currentSize, long localSize, File localPath) {
                C.sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (IFileUploadListener fileUploadListener : fileUploadListeners) {
                            fileUploadListener.onProcess(currentSize, localSize, localPath);
                        }
                    }
                });
            }
        };
        private boolean uploadFile;

        @Override
        public void startUpload() {
            if (!ftpHelper.isConnect()) {
                iFtpStatusListener.ftpDisconnect();
                return;
            }
            if (isUploadFile()) {
                ftpHelper.setStopUpload(false);
                return;
            }
            UploadFile uploadFile = UploadFile.firstUploadFile();
            if (uploadFile == null) {
                return;
            }
            String name = new File(uploadFile.getFilePath()).getName();
            String saveServiceFile = UploadFileUtils.getSaveServiceFilePath(name);
            ftpHelper.setStopUpload(false);
            uploadFile(uploadFile.getFilePath(), saveServiceFile, l);
        }

        @Override
        public void stopUpload() {
            List<UploadFile> uploadFiles = LitePal.where("status = ?", UploadFile.UPLOAD_ING).find(UploadFile.class);
            for (UploadFile file : uploadFiles) {
                file.setStatus(UploadFile.UPLOAD_READY);
                file.update(file.getId());
            }
            mHandler.removeCallbacks(null);
            ftpHelper.setStopUpload(true);
        }

        @Override
        public void removeUpload(String absPath) {
            UploadFile uploadFile = UploadFile.selectByFilePath(absPath);
            if (uploadFile != null) {
                uploadFile.delete();
                ftpHelper.setStopFile(uploadFile.getFilePath());
            }

            FileUploadChangeEvent.getInstance().postUploadStatusChange(1);
        }

        @Override
        public void removeAll() {
            List<UploadFile> uploadFiles = LitePal.where("status = ?", UploadFile.UPLOAD_ING).find(UploadFile.class);
            for (UploadFile file : uploadFiles) {
                file.delete();
            }
            FileUploadChangeEvent.getInstance().postUploadStatusChange(1);
        }

        public void addFtpListener(IFtpStatusListener l) {
            ftpStatusListeners.add(l);
        }

        public void addUploadListener(IFileUploadListener l) {
            fileUploadListeners.add(l);
        }

        public void removeFtpListener(IFileUploadListener l) {
            ftpStatusListeners.remove(l);
        }

        public void removeUploadListener(IFileUploadListener l) {
            fileUploadListeners.remove(l);
        }

        public boolean isFtpConnect() {
            return ftpHelper.isConnect();
        }

        public boolean isServiceHasFile(String filePath) throws IOException {
            return ftpHelper.isHasFile(new File(filePath));

        }

        public boolean isUploading() {
            return uploadFile;
        }
    }

    private void uploadFile(String localFile, String serviceFile, IFileUploadListener listener) {
        mHandler.post(new FtpUploadFileRunnable(new File(localFile), serviceFile, listener));
    }

    public class FtpConnectRunnable implements Runnable {

        @Override
        public void run() {
            try {
                ftpConnect = true;
                List<FtpIp> all = LitePal.findAll(FtpIp.class);
                if (all.size() == 0) {
                    iFtpStatusListener.ftpConnectFailed(new RuntimeException("未设置ip"));
                    return;
                }
                FtpIp ftpIp = all.get(0);
                ftpHelper.connectFtp(ftpIp.getIp(), Integer.parseInt(ftpIp.getPort()));
                boolean login = ftpHelper.login(ftpIp.getAccount(), ftpIp.getPw());
                if (!login) {
                    iFtpStatusListener.ftpConnectFailed(new RuntimeException("账号或密码错误"));
                    return;
                }

                iFtpStatusListener.ftpConnectSuccess();


            } catch (IOException e) {
                e.printStackTrace();
                iFtpStatusListener.ftpConnectFailed(e);
            } finally {
                ftpConnect = false;
            }
        }
    }

    public class FtpUploadFileRunnable implements Runnable {

        private File localPath;
        private String servicePath;
        private IFileUploadListener fileUploadListener;

        public FtpUploadFileRunnable(File localPath, String servicePath, IFileUploadListener fileUploadListener) {
            this.localPath = localPath;
            this.servicePath = servicePath;
            this.fileUploadListener = fileUploadListener;
        }

        @Override
        public void run() {
            try {
                if (!ftpHelper.isConnect()) {
                    throw new IOException("ftp服务未连接");
                }
                uploadFile = true;
                fileUploadListener.startUploadFile(localPath);
                ftpHelper.uploadFile(localPath, servicePath, fileUploadListener);
                fileUploadListener.uploadEnd(localPath);
            } catch (IOException e) {
                e.printStackTrace();
                iFtpStatusListener.ftpConnectFailed(e);
                fileUploadListener.uploadFailed(localPath, e);
            } catch (UploadRemoveException e) {
                e.printStackTrace();
                fileUploadListener.uploadRemove(localPath, e);
            } catch (NullPointerException e) {
                e.printStackTrace();
                iFtpStatusListener.ftpConnectFailed(e);
            } finally {
                uploadFile = false;
                fileUploadListener.onAfter();
            }
        }
    }

    public interface IFileUploadListener extends FTPHelper.IFileUploadPressListener {
        void startUploadFile(File file);

        void uploadFailed(File file, Exception e);

        void uploadEnd(File file);

        void uploadRemove(File localPath, UploadRemoveException e);

        void onAfter();
    }

}
