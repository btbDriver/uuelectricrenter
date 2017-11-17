package com.youyou.uuelectric.renter.Utils.update;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.rey.material.widget.ProgressView;
import com.rey.material.widget.TextView;
import com.youyou.uuelectric.renter.R;

/**
 * Created by aaron on 16/5/4.
 * 提示更新弹窗
 */
public class DownLoadDialog {

    public static DownLoadDialog instance;
    public static Activity mActivity;
    public static MaterialDialog materialDialog;
    public static RelativeLayout updateRela;
    public static ProgressView progressBar;
    public static TextView updatePercent;

    /**
     * 返回一个弹窗实例
     *
     * @param activity
     * @return
     */
    public synchronized static DownLoadDialog getInstance(Activity activity) {
        if (mActivity != activity) {
            mActivity = activity;
            instance = new DownLoadDialog();
        }
        return instance;
    }

    /**
     * 显示更新弹窗(两个按钮),非阻塞式弹窗，点击按钮之后弹窗消失
     * @param title
     * @param content
     * @param leftBtnText
     * @param rightBtnText
     * @param onBtnLeftClickL
     * @param onBtnRightClickL
     * @return
     */
    public Dialog showMaterialDialog(String title, String content, String leftBtnText, String rightBtnText, final View.OnClickListener onBtnLeftClickL, final View.OnClickListener onBtnRightClickL) {
        materialDialog = new MaterialDialog.Builder(mActivity)
                .title(title)
                .content(content)
                .positiveText(rightBtnText)
                .negativeText(leftBtnText)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        dialog.dismiss();
                        if (onBtnRightClickL != null) {
                            onBtnRightClickL.onClick(null);
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                        if (onBtnLeftClickL != null) {
                            onBtnLeftClickL.onClick(null);
                        }
                    }
                })
                .positiveColorRes(R.color.dialog_btn_color)
                .negativeColorRes(R.color.dialog_btn_color)
                .theme(Theme.LIGHT)
                .show();

        materialDialog.setCancelable(false);
        materialDialog.setCanceledOnTouchOutside(false);
        materialDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                DownLoadDialog.instance = null;
                DownLoadDialog.mActivity = null;
                DownLoadDialog.materialDialog = null;
            }
        });
        return materialDialog;
    }


    /**
     * 显示一个提示弹窗，包含，标题、内容、一个按钮及按钮的处理事件
     * (阻塞式弹窗，点击下载之后弹窗不消失)
     * @param title
     * @param content
     * @param btnText
     * @param onBtnClickL
     */
    public Dialog showMaterialTipDialog(String title, String content, String btnText, final View.OnClickListener onBtnClickL) {

        View view = LayoutInflater.from(mActivity).inflate(R.layout.update_dialog_process, null);
        TextView updateMsg = (TextView) view.findViewById(R.id.update_dialog_msg);
        updateRela = (RelativeLayout) view.findViewById(R.id.update_dialog_rela);
        progressBar = (ProgressView) view.findViewById(R.id.update_dialog_progress);
        updatePercent = (TextView) view.findViewById(R.id.update_dialog_content);
        updateMsg.setText(content);
        updatePercent.setText("0%");

        materialDialog = new MaterialDialog.Builder(mActivity)
                .autoDismiss(false)
                .title(title)
                .customView(view, false)
                .positiveText(btnText)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (onBtnClickL != null) {
                            onBtnClickL.onClick(null);
                        }
                    }

                })
                .positiveColorRes(R.color.dialog_btn_color)
                .theme(Theme.LIGHT)
                .show();
        materialDialog.getPositiveButton().setEnabled(true);
        materialDialog.setCancelable(false);
        materialDialog.setCanceledOnTouchOutside(false);
        materialDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                DownLoadDialog.instance = null;
                DownLoadDialog.mActivity = null;
                DownLoadDialog.materialDialog = null;
                DownLoadDialog.updateRela = null;
                DownLoadDialog.progressBar = null;
                DownLoadDialog.updatePercent = null;
            }
        });
        return materialDialog;
    }
}
