package com.ormediagroup.youngplus.lau;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ormediagroup.youngplus.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Lau on 2018/12/20.
 */

public class ProcessingDialog {

    public static final int LOADING_LAYOUT = R.layout.loading_dialog;
    public static final int COMPLETED_LAYOUT = R.layout.completed_dialog;
    public static final int FAILED_LAYOUT = R.layout.failed_dialog;
    public static final int WARNING_LAYOUT = R.layout.warning_dialog;

    private int TYPE_LOADING = 0;
    private int TYPE_COMPLETED = 1;
    private int TYPE_FAILED = 2;
    private int TYPE_WARNING = 3;

    private Context context;
    private boolean cancelOutside;
    private AlertDialog loadingAlertDialog, completedAlertDialog, failedAlertDialog, warningAlertDialog;

    public ProcessingDialog(Context context) {
        this.context = context;
        this.cancelOutside = false;
    }

    public ProcessingDialog(Context context, boolean cancelOutside) {
        this.context = context;
        this.cancelOutside = cancelOutside;
    }

    public void loading(String tips) {
        loading(tips, cancelOutside);
    }

    public void loading(String tips, boolean cancelOutside) {
        loadingAlertDialog = createDialog(tips, LOADING_LAYOUT, cancelOutside);
        loadingAlertDialog.show();
    }

    public void completed(String tips, int showTime) {
        complted(tips, showTime, cancelOutside);
    }

    public void complted(String tips, int showTime, boolean cancelOutside) {
        hideDialog(TYPE_LOADING);
        completedAlertDialog = createDialog(tips, COMPLETED_LAYOUT, cancelOutside);
        completedAlertDialog.show();
        setHideTimer(showTime, TYPE_COMPLETED);
    }

    public void failed(String tips, int showTime) {
        failed(tips, showTime, cancelOutside);
    }

    public void failed(String tips, int showTime, boolean cancelOutside) {
        hideDialog(TYPE_LOADING);
        failedAlertDialog = createDialog(tips, FAILED_LAYOUT, cancelOutside);
        failedAlertDialog.show();
        setHideTimer(showTime, TYPE_FAILED);
    }

    public void warning(String tips, int showTime) {
        warning(tips, showTime, cancelOutside);
    }

    public void warning(String tips, int showTime, boolean cancelOutside) {
        warningAlertDialog = createDialog(tips, WARNING_LAYOUT, cancelOutside);
        warningAlertDialog.show();
        setHideTimer(showTime, TYPE_WARNING);
    }

    public AlertDialog getDialog(int type) {
        AlertDialog dialog = null;
        switch (type) {
            case 0:
                if (loadingAlertDialog != null) dialog = loadingAlertDialog;
                break;
            case 1:
                if (completedAlertDialog != null) dialog = completedAlertDialog;
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
                if (completedAlertDialog != null) completedAlertDialog.dismiss();
                break;
            case 2:
                if (failedAlertDialog != null) failedAlertDialog.dismiss();
                break;
            case 3:
                if (warningAlertDialog != null) warningAlertDialog.dismiss();
                break;
        }
    }

    private AlertDialog createDialog(String tips, int layout, boolean cancelOutside) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(layout, null);
        TextView showTips = dialogView.findViewById(R.id.tips);
        showTips.setText(tips);
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
