package nss.mobile.video.card.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * @author liwei
 * @date 2016年10月11日
 * @version icon_1.0
 */
public class EdittextUtil {

    Context context;

    public void EditttextUtil(Context context){
        this.context=context;
    }

    public void editTextChangeListener(final EditText day, final EditText hour){
        day.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
