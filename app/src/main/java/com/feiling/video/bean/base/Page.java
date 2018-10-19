package com.feiling.video.bean.base;

import java.util.Map;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/8/19
 *
 * @author ql
 */
public class Page {
    private int pageNum = 1;
    private int pageSize = 20;

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void addPage(Map map) {
        map.put("pageSize", pageSize);
        map.put("pageNum", pageNum);
    }

    public void addPageNum() {
        pageNum++;
    }
}
