package nss.mobile.video.bean;

import nss.mobile.video.ui.adapter.ICardDetails;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2019/1/7
 *
 * @author ql
 */
public class CardDetails implements ICardDetails {

    private String label;
    private String value;
    private int tag;

    public CardDetails(String label, String value, int tag) {
        this.label = label;
        this.value = value;
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getLabel() {
        return label == null ? "" : label;
    }

    @Override
    public String getValue() {
        return value == null ? "" : value;
    }
}
