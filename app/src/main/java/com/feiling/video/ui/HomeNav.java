package com.feiling.video.ui;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/8/6
 *
 * @author ql
 */
public class HomeNav implements IGrid{
    private int tag;
    private int res;
    private String label;

    public HomeNav(int tag, int res, String label) {
        this.tag = tag;
        this.res = res;
        this.label = label;
    }

    @Override
    public int getTag() {
        return tag;
    }

    @Override
    public int getIconRes() {
        return res;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
