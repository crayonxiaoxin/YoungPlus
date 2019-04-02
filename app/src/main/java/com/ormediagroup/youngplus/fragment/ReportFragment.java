package com.ormediagroup.youngplus.fragment;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.lau.API;
import com.ormediagroup.youngplus.lau.ProcessingDialog;
import com.ormediagroup.youngplus.lau.User;
import com.ormediagroup.youngplus.loadAndRetry.LoadingAndRetryManager;
import com.ormediagroup.youngplus.loadAndRetry.OnLoadingAndRetryListener;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Lau on 2019/2/15.
 */

public class ReportFragment extends BaseFragment {
    private View view;
    private LoadingAndRetryManager loadingAndRetryManager;
    private LinearLayout parentLayout;
    private WebView reportDetail;
    private ProcessingDialog dialog;
    private User userInfo;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadingAndRetryManager = LoadingAndRetryManager.generate(this.parentLayout, new OnLoadingAndRetryListener() {
            @Override
            public void setRetryEvent(View retryView) {
                retryView.findViewById(R.id.base_retry).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadingAndRetryManager.showLoading();
                        initData();
                    }
                });
            }
        });
        loadingAndRetryManager.showLoading();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_report, container, false);
        initView();
        initData();
        return view;
    }

    private void initView() {
        parentLayout = view.findViewById(R.id.parentLayout);
        reportDetail = view.findViewById(R.id.report_detail);
    }


    private void initData() {
        userInfo = new User(mActivity);
        dialog = new ProcessingDialog(mActivity);
        if (userInfo.isUserLoggedIn()) {
            new JSONResponse(mActivity, API.API_GET_REPORT, "uid=" + userInfo.getUserId(), new JSONResponse.onComplete() {
                @Override
                public void onComplete(JSONObject json) {
                    try {
                        int rc = json.getInt("rc");
                        if (rc == 0) {
                            String link = json.getString("data");
                            WebSettings settings = reportDetail.getSettings();
                            settings.setJavaScriptEnabled(true);
                            reportDetail.loadUrl(link);
                            reportDetail.setWebViewClient(new WebViewClient(){
                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    super.onPageFinished(view, url);
                                    loadingAndRetryManager.showContent();
                                }
                            });
                        } else {
                            dialog.warning("沒有報告").setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    mActivity.onBackPressed();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {

        }
    }
}
