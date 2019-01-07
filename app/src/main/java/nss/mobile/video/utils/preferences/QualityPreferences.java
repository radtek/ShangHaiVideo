package nss.mobile.video.utils.preferences;

import nss.mobile.video.MyApp;
import nss.mobile.video.utils.PreferencesUtils;
import nss.mobile.video.video.configuration.CaptureConfiguration;
import nss.mobile.video.video.configuration.PredefinedCaptureConfigurations;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/10/26
 *
 * @author ql
 */
public class QualityPreferences {
    private static final String KEY_QUALITY = "keyQuality";
    private static final String KEY_RESOLUTION = "keyResolution";

    public static CaptureConfiguration getConfiguration() {
        int i = PreferencesUtils.queryInt(MyApp.getInstance().getApplicationContext(), KEY_QUALITY, 1);
        PredefinedCaptureConfigurations.CaptureQuality quality;
        switch (i) {
            case 0:
                quality = PredefinedCaptureConfigurations.CaptureQuality.LOW;
                break;
            case 1:
                quality = PredefinedCaptureConfigurations.CaptureQuality.MEDIUM;
                break;
            case 2:
                quality = PredefinedCaptureConfigurations.CaptureQuality.HIGH;
                break;
            default:
                quality = PredefinedCaptureConfigurations.CaptureQuality.LOW;
        }

        int r = PreferencesUtils.queryInt(MyApp.getInstance().getApplicationContext(), KEY_RESOLUTION, 2);
        PredefinedCaptureConfigurations.CaptureResolution resolution;
        switch (r) {
            case 0:
                resolution = PredefinedCaptureConfigurations.CaptureResolution.RES_360P;
                break;
            case 1:
                resolution = PredefinedCaptureConfigurations.CaptureResolution.RES_480P;
                break;
            case 2:
                resolution = PredefinedCaptureConfigurations.CaptureResolution.RES_720P;
                break;
            case 3:
                resolution = PredefinedCaptureConfigurations.CaptureResolution.RES_1080P;
                break;
            case 4:
                resolution = PredefinedCaptureConfigurations.CaptureResolution.RES_1440P;
                break;
            case 5:
                resolution = PredefinedCaptureConfigurations.CaptureResolution.RES_2160P;
                break;
            default:
                resolution = PredefinedCaptureConfigurations.CaptureResolution.RES_360P;
        }

        return new CaptureConfiguration.Builder(resolution, quality).build();
    }

    public static void saveResolution(PredefinedCaptureConfigurations.CaptureResolution c) {
        int tag;
        if (c == PredefinedCaptureConfigurations.CaptureResolution.RES_360P) {
            tag = 0;
        } else if (c == PredefinedCaptureConfigurations.CaptureResolution.RES_480P) {
            tag = 1;
        } else if (c == PredefinedCaptureConfigurations.CaptureResolution.RES_720P) {
            tag = 2;

        } else if (c == PredefinedCaptureConfigurations.CaptureResolution.RES_1080P) {
            tag = 3;

        } else if (c == PredefinedCaptureConfigurations.CaptureResolution.RES_1440P) {
            tag = 4;

        } else if (c == PredefinedCaptureConfigurations.CaptureResolution.RES_2160P) {
            tag = 5;
        } else {
            tag = 0;
        }
        PreferencesUtils.saveInt(MyApp.getInstance().getApplicationContext(), KEY_RESOLUTION, tag);
    }

    public static void saveQuality(PredefinedCaptureConfigurations.CaptureQuality c) {
        int tag;
        if (c == PredefinedCaptureConfigurations.CaptureQuality.LOW) {
            tag = 0;
        } else if (c == PredefinedCaptureConfigurations.CaptureQuality.MEDIUM) {
            tag = 1;
        } else if (c == PredefinedCaptureConfigurations.CaptureQuality.HIGH) {
            tag = 2;
        } else {
            tag = 0;
        }
        PreferencesUtils.saveInt(MyApp.getInstance().getApplicationContext(), KEY_QUALITY, tag);
    }

}
