package com.ormediagroup.youngplus.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
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
import com.ormediagroup.youngplus.lau.ReportWebviewClient;
import com.ormediagroup.youngplus.lau.User;
import com.ormediagroup.youngplus.loadAndRetry.LoadingAndRetryManager;
import com.ormediagroup.youngplus.loadAndRetry.OnLoadingAndRetryListener;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;

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

    public static ReportFragment newInstance(String url) {
        ReportFragment f = new ReportFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        f.setArguments(bundle);
        return f;
    }

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
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_report, container, false);
        initView();
        return view;
    }

    private void initView() {
        parentLayout = view.findViewById(R.id.parentLayout);
        reportDetail = view.findViewById(R.id.report_detail);
    }


    private void initData() {
        userInfo = new User(mActivity);
        dialog = new ProcessingDialog(mActivity);
        Bundle bundle = getArguments();
        String link = "";
        if (bundle != null) {
            link = bundle.getString("url", "");
            Log.i(TAG, "initData: url = " + link);
        }
        if (link.equals("")) {
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
                                settings.setLoadsImagesAutomatically(true);
                                settings.setSupportZoom(false);
                                settings.setDomStorageEnabled(true);
                                Log.i(TAG, "onComplete: url = " + link);
                                reportDetail.loadUrl(link);
                                reportDetail.setWebViewClient(new ReportWebviewClient(mActivity, new ReportWebviewClient.ReportWebViewListener() {
                                    @Override
                                    public void onBreak(String url) {
                                        ReportFragmentListener rfl = (ReportFragmentListener) mActivity;
                                        if (rfl != null) {
                                            rfl.toOpenLink(url);
                                        }
                                    }

                                    @Override
                                    public void onPageFinished(WebView view, String url) {
                                        loadingAndRetryManager.showContent();
                                    }
                                }));

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
            }
        } else {
            Log.i(TAG, "initData: link = " + link);
            WebSettings settings = reportDetail.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setLoadsImagesAutomatically(true);
            settings.setSupportZoom(false);
            settings.setDomStorageEnabled(true);
            reportDetail.loadUrl(link);
            reportDetail.setWebViewClient(new ReportWebviewClient(mActivity, new ReportWebviewClient.ReportWebViewListener() {
                @Override
                public void onBreak(String url) {
                    ReportFragmentListener rfl = (ReportFragmentListener) mActivity;
                    if (rfl != null) {
                        rfl.toOpenLink(url);
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    loadingAndRetryManager.showContent();
                }
            }));
        }
    }

    public interface ReportFragmentListener {
        void toOpenLink(String url);
    }
}
