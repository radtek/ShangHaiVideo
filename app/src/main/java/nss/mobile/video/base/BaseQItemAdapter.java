package nss.mobile.video.base;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.List;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/7/18
 *
 * @author ql
 */
public abstract class BaseQItemAdapter<T, Q extends QBaseViewHolder> extends BaseQuickAdapter<T, Q> {
    public BaseQItemAdapter(int layoutResId, @Nullable List<T> data) {
        super(layoutResId, data);
    }

    public BaseQItemAdapter(@Nullable List<T> data) {
        super(data);
    }

    public BaseQItemAdapter(int layoutResId) {
        super(layoutResId);
    }

}
