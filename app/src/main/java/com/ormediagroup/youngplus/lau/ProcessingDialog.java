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

    public static final int LOADING_LAYOUT = R.layout.loading_dialog;
    public static final int SUCCESS_LAYOUT = R.layout.completed_dialog;
    public static final int FAILED_LAYOUT = R.layout.failed_dialog;
    public static final int WARNING_LAYOUT = R.layout.warning_dialog;

    private int TYPE_LOADING = 0;
    private int TYPE_SUCCESS = 1;
    private int TYPE_FAILED = 2;
    private int TYPE_WARNING = 3;

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

    public ProcessingDialog(Context context, int showTime, boolean cancelOutside) {
        this.context = context;
        this.showTime = showTime;
        this.cancelOutside = cancelOutside;
    }

    public void loading(String prompt) {
        loading(prompt, cancelOutside);
    }

    public void loading(String prompt, boolean cancelOutside) {
        loadingAlertDialog = createDialog(prompt, LOADING_LAYOUT, cancelOutside);
        loadingAlertDialog.show();
    }

    public void success(String prompt) {
        success(prompt, showTime, cancelOutside);
    }

    public void success(String prompt, int showTime) {
        success(prompt, showTime, cancelOutside);
    }

    public void success(String prompt, int showTime, boolean cancelOutside) {
        hideDialog(TYPE_LOADING);
        successAlertDialog = createDialog(prompt, SUCCESS_LAYOUT, cancelOutside);
        successAlertDialog.show();
        setHideTimer(showTime, TYPE_SUCCESS);
    }

    public void failed(String prompt) {
        failed(prompt, showTime, cancelOutside);
    }

    public void failed(String prompt, int showTime) {
        failed(prompt, showTime, cancelOutside);
    }

    public void failed(String prompt, int showTime, boolean cancelOutside) {
        hideDialog(TYPE_LOADING);
        failedAlertDialog = createDialog(prompt, FAILED_LAYOUT, cancelOutside);
        failedAlertDialog.show();
        setHideTimer(showTime, TYPE_FAILED);
    }

    public void warning(String prompt) {
        warning(prompt, showTime, cancelOutside);
    }

    public void warning(String prompt, int showTime) {
        warning(prompt, showTime, cancelOutside);
    }

    public void warning(String prompt, int showTime, boolean cancelOutside) {
        warningAlertDialog = createDialog(prompt, WARNING_LAYOUT, cancelOutside);
        warningAlertDialog.show();
        setHideTimer(showTime, TYPE_WARNING);
    }

    public void loadingToSuccess(String prompt) {
        loadingToWhat(R.drawable.success, prompt, showTime);
    }

    public void loadingToSuccess(String prompt, int showTime) {
        loadingToWhat(R.drawable.success, prompt, showTime);
    }

    public void loadingToFailed(String prompt) {
        loadingToWhat(R.drawable.failed, prompt, showTime);
    }

    public void loadingToFailed(String prompt, int showTime) {
        loadingToWhat(R.drawable.failed, prompt, showTime);
    }

    private void loadingToWhat(int resID, String prompt, int showTime) {
        LinearLayout replacePart = loadingAlertDialog.findViewById(R.id.replace_part);
        replacePart.removeAllViews();
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setImageResource(resID);
        replacePart.addView(imageView);
        TextView textView = loadingAlertDialog.findViewById(R.id.prompt);
        textView.setText(prompt);
        setHideTimer(showTime, 0);
    }

    public AlertDialog getDialog(int type) {
        AlertDialog dialog = null;
        switch (type) {
            case 0:
                if (loadingAlertDialog != null) dialog = loadingAlertDialog;
                break;
            case 1:
                if (successAlertDialog != null) dialog = successAlertDialog;
                break;
            case 2:
                if (failedAlertDialog != null) dialog = failedAlertDialog;
                break;
            case 3:
                if (warningAlertDialog != null) dialog = warningAlertDialog;
                break;
        }
        return dialog;
    }


    public void hideDialog(int type) {
        switch (type) {
            case 0:
                if (loadingAlertDialog != null) loadingAlertDialog.dismiss();
                break;
            case 1:
                if (successAlertDialog != null) successAlertDialog.dismiss();
                break;
            case 2:
                if (failedAlertDialog != null) failedAlertDialog.dismiss();
                break;
            case 3:
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
