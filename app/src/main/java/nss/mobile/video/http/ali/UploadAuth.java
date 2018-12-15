package nss.mobile.video.http.ali;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/12/15
 *
 * @author ql
 */
public class UploadAuth {


    /**
     * SecurityToken : CAIS0AR1q6Ft5B2yfSjIr4nSIuPsv7RV7vq/Y3/kvnQmavxtqJbjkzz2IHhJeXNvBO0etf4+mWBY7PcYlrUqGsIdGhacMZoqs8UGrF3/JpfZv8u84YADi5CjQZMIkPVomJ28Wf7waf+AUBXGCTmd5MMYo9bTcTGlQCZuW//toJV7b9MRcxClZD5dfrl/LRdjr8lo1xGzUPG2KUzSn3b3BkhlsRYe72Rk8vaHxdaAzRDcgVbmqJcSvJ+jC4C8Ys9gG519XtypvopxbbGT8CNZ5z9A9qp9kM49/izc7P6QH35b4RiNL8/Z7tQNXwhiffobHa9YrfHgmNhlvvDSj43t1ytVOeZcX0akQ5u7ku7ZHP+oLt8jaYvjP3PE3rLpMYLu4T48ZXUSODtDYcZDUHhrEk4RUjXdI6Of8UrWSQC7Wsr217otg7Fyyk3s8MaHAkWLX7SB2DwEB4c4aEokVW4RxnezW6UBaRBpbld7Bq6cV5lOdBRZoK+KzQrJTX9Ez2pLmuD6e/LOs7oDVJ37WZtKyuh4Y49d4U8rVEjPQqiykT0pFgpfTK1RzbPmNLKm9baB25/zW+PdDe0dsVgoIFKOpiGWG3RLNn+ztJ9xbkeE+sKUlPeTqMJqHFp+ttpVVFiIIYZh9w0+u/LstBnK+bG+WC7t5XR/uPugptUQsxQ8I6372bbC5m6P4kb9O/dpxJ3lP0R0WgmydnBDx/Sfu2kKvRhpkRvvY0tCsQvMiD7rJpxGgqzRlylefo5XmPXFTQmn8l5pAMmy/60xXudvbCjH10p6WKcDGoABCxr7jLanUo/ZfxBx80MSubrf+otuqjOarSbcNT4ux8yPzyBTmebT+pY/wjIDS8SHN49DkS2wVxXEdS/8YMVMh9MmqHqtXp9rY8MBTLy/MuutOvCXCBJm1V7tkyQpbWxQQW0bSRh/tpG5IRCxBzfRreEBs0qEw+d8cSnckf3sdvg=
     * AccessKeyId : STS.NJgiYXRktY8TaYUZtsfsAGSHq
     * ExpireUTCTime : 2018-12-15T06:10:39Z
     * AccessKeySecret : BGxaNVUPX1QtbpgycJbeXjqgv7CzArtfJ4RxXJ3Z4pv6
     * Expiration : 3596
     * Region : cn-shanghai
     */

    private String SecurityToken;
    private String AccessKeyId;
    private String ExpireUTCTime;
    private String AccessKeySecret;
    private String Expiration;
    private String Region;

    public String getSecurityToken() {
        return SecurityToken;
    }

    public void setSecurityToken(String SecurityToken) {
        this.SecurityToken = SecurityToken;
    }

    public String getAccessKeyId() {
        return AccessKeyId;
    }

    public void setAccessKeyId(String AccessKeyId) {
        this.AccessKeyId = AccessKeyId;
    }

    public String getExpireUTCTime() {
        return ExpireUTCTime;
    }

    public void setExpireUTCTime(String ExpireUTCTime) {
        this.ExpireUTCTime = ExpireUTCTime;
    }

    public String getAccessKeySecret() {
        return AccessKeySecret;
    }

    public void setAccessKeySecret(String AccessKeySecret) {
        this.AccessKeySecret = AccessKeySecret;
    }

    public String getExpiration() {
        return Expiration;
    }

    public void setExpiration(String Expiration) {
        this.Expiration = Expiration;
    }

    public String getRegion() {
        return Region;
    }

    public void setRegion(String Region) {
        this.Region = Region;
    }
}
