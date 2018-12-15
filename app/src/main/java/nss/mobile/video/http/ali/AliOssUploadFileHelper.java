package nss.mobile.video.http.ali;

import android.content.Context;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.io.File;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/12/12
 *
 * @author ql
 */
public class AliOssUploadFileHelper {

    //"<StsToken.AccessKeyId>"
//    private static String AccessKeyId = "LTAIhdAHy8685IVU";
//    private static String SecretKeyId = "d17aQE1OrvTaSvhirTqmr3us0cdxqe";
//    private static String bucketName = "";
    private OSSClient oss;
    private String accessKeyId;
    private String bucket;
    private String securityToken;
    private String accessKeySecret;
    private String region;
    private String fileName;
    private String endpoint;

    public AliOssUploadFileHelper() {
    }

    public AliOssUploadFileHelper(String accessKeyId, String bucket, String securityToken, String accessKeySecret, String region, String fileName, String endpoint) {
        this.accessKeyId = accessKeyId;
        this.bucket = bucket;
        this.securityToken = securityToken;
        this.accessKeySecret = accessKeySecret;
        this.region = region;
        this.fileName = fileName;
        this.endpoint = endpoint;
    }

    public void resetInit(Context context) {
        // TODO: 2018/12/13 需要他们进行提供
//推荐使用OSSAuthCredentialsProvider。token过期可以及时更新
        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(accessKeyId, accessKeySecret, securityToken);
//        OSSCredentialProvider credentialProvider = new OSSAuthCredentialsProvider(stsServer);

//该配置类如果不设置，会有默认配置，具体可看该类
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求数，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次

        oss = new OSSClient(context, endpoint, credentialProvider, conf);


//        task.cancel(); // 可以取消任务
//        task.waitUntilFinished(); // 可以等待任务完成
    }

    public OSSAsyncTask uploadFile(File file, OSSProgressCallback<PutObjectRequest> putObject, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback) {
        // 构造上传请求
        PutObjectRequest put = new PutObjectRequest(bucket, fileName, file.getAbsolutePath());

        // 异步上传时可以设置进度回调
//        OSSProgressCallback<PutObjectRequest> putObject = new OSSProgressCallback<PutObjectRequest>() {
//            @Override
//            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
//                Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
//            }
//        };
        put.setProgressCallback(putObject);

//        OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback = new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
//            @Override
//            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
//                Log.d("PutObject", "UploadSuccess");
//
//                Log.d("ETag", result.getETag());
//                Log.d("RequestId", result.getRequestId());
//            }
//
//            @Override
//            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
//                // 请求异常
//                if (clientExcepion != null) {
//                    // 本地异常如网络异常等
//                    clientExcepion.printStackTrace();
//                }
//                if (serviceException != null) {
//                    // 服务异常
//                    Log.e("ErrorCode", serviceException.getErrorCode());
//                    Log.e("RequestId", serviceException.getRequestId());
//                    Log.e("HostId", serviceException.getHostId());
//                    Log.e("RawMessage", serviceException.getRawMessage());
//                }
//            }
//        };
        OSSAsyncTask task = oss.asyncPutObject(put, completedCallback);

        return task;
    }
}
