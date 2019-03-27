package nss.mobile.video.card.authentication.fingerprint_u3;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 作者：李阳
 * 时间：2018/12/29
 * 描述：
 */
public class DefaultFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        TextView textView=new TextView(getContext());

        textView.setText("无USB指纹模块,请检查硬件");

        RelativeLayout.LayoutParams layoutParams= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        textView.setGravity(Gravity.CENTER);

        textView.setLayoutParams(layoutParams);

        return textView;
    }
}
