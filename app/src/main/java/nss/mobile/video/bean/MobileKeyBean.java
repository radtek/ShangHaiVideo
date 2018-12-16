package nss.mobile.video.bean;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/12/15
 *
 * @author ql
 */
public class MobileKeyBean extends LitePalSupport {

    private int id;
    private String mobileKey;
    private Long createDate;

    public static MobileKeyBean getLast(){
        return LitePal.findLast(MobileKeyBean.class);
    }

    public static void saveNormalKey(Context context){
        MobileKeyBean last = new MobileKeyBean();
        last.setCreateDate(System.currentTimeMillis());
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String imei = telephonyManager.getDeviceId();
        last.setMobileKey(imei);
        last.save();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMobileKey() {
        return mobileKey;
    }

    public void setMobileKey(String mobileKey) {
        this.mobileKey = mobileKey;
    }

    public Long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Long createDate) {
        this.createDate = createDate;
    }
}
