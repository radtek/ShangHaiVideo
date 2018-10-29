package nss.mobile.video.bean;

import nss.mobile.video.bean.base.Page;
import nss.mobile.video.utils.DataUtils;

import java.util.List;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/8/18
 *
 * @author ql
 */
public class BaseResult {
    public static final int CODE_SUCCESS = 200;
    public static final int CODE_RELOGIN = 101;

    private int code;
    private String message;
    private List list;
    private Object data;
    private long total;
    private long pageSizes;

    public <T> T getDataO(Class clazz) {
        return DataUtils.getResultObj(data, clazz);
    }

    public List getListO(Class clazz) {
        return DataUtils.getArrayResult(list, clazz);
    }

    public Long getTotal() {
        return total;
    }

    public boolean checkLoadEnd(Page page) {
        return page.getPageNum() == pageSizes;

    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public long getPageSizes() {
        return pageSizes;
    }

    public void setPageSizes(Integer pageSizes) {
        this.pageSizes = pageSizes;
    }

    public boolean isSuccess() {
        return code == CODE_SUCCESS;

    }

    public boolean isRelogin() {
        return (code == CODE_RELOGIN);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
