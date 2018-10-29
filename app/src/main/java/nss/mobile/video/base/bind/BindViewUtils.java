package nss.mobile.video.base.bind;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2017-3-10.
 */
public class BindViewUtils {

    public static void find(Activity aty) {
        Class clazz = aty.getClass();
        View sourceView = aty.getWindow().getDecorView();
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (field.isAnnotationPresent(BindView.class)) {
                    BindView f = field.getAnnotation(BindView.class);
                    int viewId = f.value();
                    field.setAccessible(true);
                    field.set(aty,sourceView.findViewById(viewId));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    public static void find(Fragment frag) {
        find(frag,frag.getActivity().getWindow().getDecorView());
    }

    public static void find(Object ojb,View v) {
        Class clazz = ojb.getClass();

        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (field.isAnnotationPresent(BindView.class)) {
                    BindView f = field.getAnnotation(BindView.class);
                    int viewId = f.value();
                    field.setAccessible(true);
                    field.set(ojb,v.findViewById(viewId));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
