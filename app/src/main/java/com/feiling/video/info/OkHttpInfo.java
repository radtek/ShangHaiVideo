package com.feiling.video.info;

import android.content.Context;
import android.support.annotation.NonNull;

import com.zhy.http.okhttp.OkHttpUtils;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;

/**
 * Created by mrqiu on 2017/10/15.
 */

public class OkHttpInfo {
    private static final long TIME_OUT = 30_000;

    /**
     * 初始化 请求
     *
     * @param context
     */
    public static void initOkHttpCard(Context context) {

        OkHttpClient okHttpClient = new OkHttpClient();
        OkHttpClient okHttpClientCard = getOkHttpClientCard(okHttpClient.newBuilder(), context);

        OkHttpClient client = okHttpClientCard.newBuilder()
                .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .build();

        OkHttpUtils.initClient(client);

    }

    public static void initClient(){
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient build = builder.connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(build);
    }

    /**
     * 添加证书
     *
     * @param builder
     * @param context
     * @return
     */
    @NonNull
    private static OkHttpClient getOkHttpClientCard(OkHttpClient.Builder builder, Context context) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            String certificateAlias = Integer.toString(0);
            keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(context.getAssets().open("5odj.com.cer")));//拷贝好的证书
            SSLContext sslContext = SSLContext.getInstance("TLS");
            final TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init
                    (
                            null,
                            trustManagerFactory.getTrustManagers(),
                            new SecureRandom()
                    );
            builder.sslSocketFactory(sslContext.getSocketFactory());
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }
}
