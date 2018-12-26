package com.ormediagroup.youngplus.lau;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ormediagroup.youngplus.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Lau on 2018/12/20.
 */

public class ProcessingDialog {

    private static final int LOADING_LAYOUT = R.layout.loading_dialog;
    private static final int SUCCESS_LAYOUT = R.layout.completed_dialog;
    private static final int FAILED_LAYOUT = R.layout.failed_dialog;
    private static final int WARNING_LAYOUT = R.layout.warning_dialog;

    private static final int TYPE_LOADING = 0;
    private static final int TYPE_SUCCESS = 1;
    private static final int TYPE_FAILED = 2;
    private static final int TYPE_WARNING = 3;

    private int showTime = 1000;
    private boolean cancelOutside = false;

    private Context context;
    private AlertDialog loadingAlertDialog, successAlertDialog, failedAlertDialog, warningAlertDialog;

    public ProcessingDialog(Context context) {
        this.context = context;
    }

    public ProcessingDialog(Context context, int showTime) {
        this.context = context;
        this.showTime = showTime;
    }

    public void loading(String prompt) {
        loadingAlertDialog = createDialog(prompt, LOADING_LAYOUT, cancelOutside);
        loadingAlertDialog.show();
    }

    public AlertDialog success(String prompt) {
        return success(prompt, showTime);
    }

    public AlertDialog success(String prompt, int showTime) {
        hideDialog(TYPE_LOADING);
        successAlertDialog = createDialog(prompt, SUCCESS_LAYOUT, cancelOutside);
        successAlertDialog.show();
        setHideTimer(showTime, TYPE_SUCCESS);
        return successAlertDialog;
    }

    public AlertDialog failed(String prompt) {
        return failed(prompt, showTime);
    }

    public AlertDialog failed(String prompt, int showTime) {
        hideDialog(TYPE_LOADING);
        failedAlertDialog = createDialog(prompt, FAILED_LAYOUT, cancelOutside);
        failedAlertDialog.show();
        setHideTimer(showTime, TYPE_FAILED);
        return failedAlertDialog;
    }

    public AlertDialog warning(String prompt) {
        return warning(prompt, showTime);
    }

    public AlertDialog warning(String prompt, int showTime) {
        warningAlertDialog = createDialog(prompt, WARNING_LAYOUT, cancelOutside);
        warningAlertDialog.show();
        setHideTimer(showTime, TYPE_WARNING);
        return warningAlertDialog;
    }

    public AlertDialog loadingToSuccess(String prompt) {
        return loadingToWhat(R.drawable.success, prompt, showTime);
    }

    public AlertDialog loadingToSuccess(String prompt, int showTime) {
        return loadingToWhat(R.drawable.success, prompt, showTime);
    }

    public AlertDialog loadingToFailed(String prompt) {
        return loadingToWhat(R.drawable.failed, prompt, showTime);
    }

    public AlertDialog loadingToFailed(String prompt, int showTime) {
        return loadingToWhat(R.drawable.failed, prompt, showTime);
    }

    private AlertDialog loadingToWhat(int resID, String prompt, int showTime) {
        LinearLayout replacePart = loadingAlertDialog.findViewById(R.id.replace_part);
        replacePart.removeAllViews();
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setImageResource(resID);
        replacePart.addView(imageView);
        TextView textView = loadingAlertDialog.findViewById(R.id.prompt);
        textView.setText(prompt);
        setHideTimer(showTime, TYPE_LOADING);
        return loadingAlertDialog;
    }

    public AlertDialog getLoading() {
        return getDialog(TYPE_LOADING);
    }

    public AlertDialog getSuccess() {
        return getDialog(TYPE_SUCCESS);
    }

    public AlertDialog getFailed() {
        return getDialog(TYPE_FAILED);
    }

    public AlertDialog getWarning() {
        return getDialog(TYPE_WARNING);
    }

    private AlertDialog getDialog(int type) {
        AlertDialog dialog = null;
        switch (type) {
            case TYPE_LOADING:
                if (loadingAlertDialog != null) dialog = loadingAlertDialog;
                break;
            case TYPE_SUCCESS:
                if (successAlertDialog != null) dialog = successAlertDialog;
                break;
            case TYPE_FAILED:
                if (failedAlertDialog != null) dialog = failedAlertDialog;
                break;
            case TYPE_WARNING:
                if (warningAlertDialog != null) dialog = warningAlertDialog;
                break;
        }
        return dialog;
    }

    public void hideLoading() {
        hideDialog(TYPE_LOADING);
    }

    public void hideSuccess() {
        hideDialog(TYPE_SUCCESS);
    }

    public void hideFailed() {
        hideDialog(TYPE_FAILED);
    }

    public void hideWarning() {
        hideDialog(TYPE_WARNING);
    }

    private void hideDialog(int type) {
        switch (type) {
            case TYPE_LOADING:
                if (loadingAlertDialog != null) loadingAlertDialog.dismiss();
                break;
            case TYPE_SUCCESS:
                if (successAlertDialog != null) successAlertDialog.dismiss();
                break;
            case TYPE_FAILED:
                if (failedAlertDialog != null) failedAlertDialog.dismiss();
                break;
            case TYPE_WARNING:
                if (warningAlertDialog != null) warningAlertDialog.dismiss();
                break;
        }
    }

    private AlertDialog createDialog(String prompt, int layout, boolean cancelOutside) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(layout, null);
        TextView showTips = dialogView.findViewById(R.id.prompt);
        showTips.setText(prompt);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(cancelOutside);
        return dialog;
    }

    private void setHideTimer(int showTime, final int type) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                hideDialog(type);
                timer.cancel();
            }
        }, showTime);
    }
}
