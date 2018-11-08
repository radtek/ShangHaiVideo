package nss.mobile.video.video;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/4
 *
 * @author ql
 */
public class QualityBean {

    private String labelName;
    private Object tag;


    public QualityBean(String labelName, Object tag) {
        this.labelName = labelName;
        this.tag = tag;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }
}
