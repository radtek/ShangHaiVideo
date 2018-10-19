package com.feiling.video.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/6/7
 *
 * @author ql
 */

public class ScrollWebView extends WebView {

    private OnScrollWebViewListener onScrollWebViewListener;

    public ScrollWebView(Context context) {
        super(context);
    }

    public ScrollWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (this.onScrollWebViewListener == null) {
            return;
        }
        this.onScrollWebViewListener.onScrollChanged(l, t, oldl, oldt);
    }

    public OnScrollWebViewListener getOnScrollWebViewListener() {
        return onScrollWebViewListener;
    }

    public void setOnScrollWebViewListener(OnScrollWebViewListener onScrollWebViewListener) {
        this.onScrollWebViewListener = onScrollWebViewListener;
    }

    public interface OnScrollWebViewListener {
        void onScrollChanged(int l, int t, int oldl, int oldt);
    }
}
