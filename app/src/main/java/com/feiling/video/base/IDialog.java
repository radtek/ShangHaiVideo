package com.feiling.video.base;/**
 * Created by mrqiu on 2017/11/13.
 */

import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

/**
 * @author ql email:strive_bug@yeah.net
 */
public interface IDialog {
    void displayLoadingDialog(CharSequence msg);

    void cancelLoadingDialog();

    void displayTipDialogFail(CharSequence msg, long l);

    void displayTipDialogFail(CharSequence msg);

    void cancelTipDialogFail();

    void cancelTipDialogSuccess();

    void displayTipDialogSuccess(CharSequence msg, long l);

    void displayTipDialogSuccess(CharSequence msg);

    void displayMessageDialog(CharSequence msg);

    void displayMessageDialog(CharSequence msg, boolean isCancel);

    void displayMessageDialog(CharSequence msg, String action, QMUIDialogAction.ActionListener l);

    void toast(CharSequence msg);
}
