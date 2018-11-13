package nss.mobile.video.http.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/12
 *
 * @author ql
 */
public class FTPHelper {
    private FTPClient mFtpClient;


    private String stopFile;
    private boolean stopUpload;

    public void connectFtp(String url, int port) throws IOException {
        mFtpClient = new FTPClient();
        mFtpClient.connect(url, port);
        mFtpClient.setControlEncoding("UTF-8");
    }

    public boolean login(String account, String pw) throws IOException {
        mFtpClient.login(account, pw);
        int replyCode = mFtpClient.getReplyCode();
        return FTPReply.isPositiveCompletion(replyCode);
    }

    public boolean isConnect() {
        if (mFtpClient == null) {
            return false;
        }
        return mFtpClient.isConnected();
    }

    public boolean isHasFile(File file) throws IOException {
        String name = file.getName();
        FTPFile[] files = mFtpClient.listFiles(name);
        if (files.length == 0) {
            return false;
        }
        if (files[0].getSize() != file.length()) {
            return false;
        }
        return true;
    }


    public boolean uploadFile(File localPath, String serverPath, IFileUploadPressListener listener) throws IOException {
        if (!localPath.exists()) {
            return false;
        }

        createDirectory(serverPath);
        String fileName = localPath.getName();
        long localSize = localPath.length();
        FTPFile[] files = mFtpClient.listFiles(fileName);
        long serverSize = 0;
        if (files.length != 0) {
            mFtpClient.deleteFile(fileName);
        }
        RandomAccessFile raf = new RandomAccessFile(localPath, "r");
        //进度
        long step = localSize / 100;
        long process = 0;
        long currentSize = 0;
        mFtpClient.enterLocalPassiveMode();
        mFtpClient.setFileType(FTP.BINARY_FILE_TYPE);
        mFtpClient.setRestartOffset(serverSize);
        raf.seek(serverSize);
        OutputStream outputStream = mFtpClient.appendFileStream(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(outputStream);
        byte[] b = new byte[1024];
        int length;
        while ((length = raf.read(b)) != -1) {
            if (isStopUpload(localPath)) {
                break;
            }
            bos.write(b, 0, length);
            currentSize = currentSize + length;
            if (currentSize / step != process) {
                process = currentSize / step;
                //当前进度 process
                listener.onProcess(currentSize, localSize, localPath);
            }
        }
        bos.flush();
        //结束
        bos.close();
        raf.close();
        if (mFtpClient.completePendingCommand()) {
            //文件上传成功
            return true;
        } else {
            //文件上传失败
            return false;
        }
    }


    public String getStopFile() {
        return stopFile;
    }

    public void setStopFile(String stopFile) {
        this.stopFile = stopFile;
    }

    public boolean isStopUpload() {
        return stopUpload;
    }

    public void setStopUpload(boolean stopUpload) {
        this.stopUpload = stopUpload;
    }

    private boolean isStopUpload(File localPath) {
        if (localPath.getAbsolutePath().equals(stopFile)) {
            stopFile = null;
            return true;
        }
        return stopUpload;
    }


    public interface IFileUploadPressListener {

        void onProcess(long currentSize, long localSize, File localPath);
    }

    public void close() throws IOException {
        mFtpClient.logout();
        mFtpClient.disconnect();

    }


    /**
     * 在ftp服务器上创建文件夹
     *
     * @param path
     * @return
     * @throws IOException
     */
    private boolean createDirectory(String path) throws IOException {
        mFtpClient.changeWorkingDirectory("/");
        boolean bool = false;
        String directory = path.substring(0, path.lastIndexOf("/") + 1);
        int start = 0;
        int end = 0;
        if (directory.startsWith("/")) {
            start = 1;
        }
        end = directory.indexOf("/", start);
        while (true) {
            String subDirectory = directory.substring(start, end);
            if (!mFtpClient.changeWorkingDirectory(subDirectory)) {
                mFtpClient.makeDirectory(subDirectory);
                mFtpClient.changeWorkingDirectory(subDirectory);
                bool = true;
            }
            start = end + 1;
            end = directory.indexOf("/", start);
            if (end == -1) {
                break;
            }
        }

        return bool;
    }


}
