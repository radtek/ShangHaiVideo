package nss.mobile.video.bean;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/10/30
 *
 * @author ql
 */
public class MemoryBean {
    private long availableInternalMemorySize;//剩余空间
    private long totalInternalMemorySize;//当前全部空间

    /**
     * 剩余空间百分比
     * @return
     */
    public float getPercent(){
        return availableInternalMemorySize * 100.0f / totalInternalMemorySize;
    }

    public long getAvailableInternalMemorySize() {
        return availableInternalMemorySize;
    }

    public void setAvailableInternalMemorySize(long availableInternalMemorySize) {
        this.availableInternalMemorySize = availableInternalMemorySize;
    }

    public long getTotalInternalMemorySize() {
        return totalInternalMemorySize;
    }

    public void setTotalInternalMemorySize(long totalInternalMemorySize) {
        this.totalInternalMemorySize = totalInternalMemorySize;
    }
}
