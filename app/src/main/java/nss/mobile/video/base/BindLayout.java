package nss.mobile.video.base;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nss.mobile.video.R;

/**
 * @author ql
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2017/11/25
 */
@Documented
@Inherited
//该注解可以作用于方法,类与接口
@Target({ElementType.METHOD, ElementType.TYPE})
//JVM会读取注解,所以利用反射可以获得注解
@Retention(RetentionPolicy.RUNTIME)
public @interface BindLayout {

    String title() default "";

    @StringRes int titleRes() default -1;

    @LayoutRes int layoutRes() default -1;

    /**
     * 如设置为 0 则不显示
     *
     * @return
     */
    @DrawableRes int backRes() default R.drawable.qmui_icon_topbar_back;

    @IdRes int topbarId() default R.id.topbar;

    boolean bindTopBar() default true;

    boolean addStatusBar() default false;
}
