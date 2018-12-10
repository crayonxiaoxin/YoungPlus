package com.ormediagroup.youngplus.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.lau.LauUtil;
import com.ormediagroup.youngplus.lau.LoadingAndRetryManager;
import com.ormediagroup.youngplus.lau.OnLoadingAndRetryListener;
import com.ormediagroup.youngplus.lau.ServiceWebviewClient;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lau on 2018/11/26.
 */

public class ServiceDetailFragment extends BaseFragment {
    private View view;
    private WebView service_detail;
    private String REQUEST_URL = "http://youngplus.com.hk/app-get-service-detail";
    private String link = "";
    private int flag = 0;
    private LoadingAndRetryManager loadingAndRetryManager;
    private LinearLayout parentLayout;

    public static final ServiceDetailFragment newInstance(int id) {
        ServiceDetailFragment f = new ServiceDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        f.setArguments(bundle);
        return f;
    }

    public static final ServiceDetailFragment newInstance(String title, String url) {
        ServiceDetailFragment f = new ServiceDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);
        f.setArguments(bundle);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_service_detail, null);
        initView();
        initData();
        return view;
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
    }

    private void initView() {
        parentLayout = view.findViewById(R.id.parentLayout);
        service_detail = new WebView(mActivity);
        parentLayout.addView(service_detail);
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String param;
            if (bundle.getInt("id") > 0) {
                param = "id=" + bundle.getInt("id");
            } else if (!bundle.getString("title").isEmpty()) {
                param = "title=" + bundle.getString("title");
                link = bundle.getString("url");
            } else {
                param = "";
            }
            new JSONResponse(mActivity, REQUEST_URL, param, new JSONResponse.onComplete() {
                @Override
                public void onComplete(JSONObject json) {
                    if (json.length() > 0) {
                        Log.i(TAG, "onComplete: json");
                        loadingAndRetryManager.showContent();
                        try {
                            if (json.getInt("rc") == -3) {
                                flag = -3;
                                service_detail.loadUrl(link);
                            } else {
//                                service_detail.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                                String bannerHtml = json.getString("banner");
                                String bodyHtml = json.getJSONObject("data").getString("post_content");
                                service_detail.loadDataWithBaseURL("file:///android_asset/",
                                        LauUtil.formatHTML(bannerHtml + bodyHtml),
                                        "text/html;charset=UTF-8",
                                        null,
                                        null);
                                WebSettings ws = service_detail.getSettings();
                                ws.setJavaScriptEnabled(true);
                                ws.setLoadsImagesAutomatically(true);
                                ws.setSupportZoom(false);
                                ws.setDomStorageEnabled(true);
                                service_detail.setWebViewClient(new ServiceWebviewClient(mActivity));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        loadingAndRetryManager.showRetry();
                    }
                }
            });
        }
    }

    @Override
    public boolean onBackPressed() {
        setOnServiceDetailFragmentListener sosdfl = (setOnServiceDetailFragmentListener) mActivity;
        if (sosdfl != null) {
            sosdfl.toHome("detail", 0);
            return true;
        } else {
            return super.onBackPressed();
        }
    }

    public interface setOnServiceDetailFragmentListener {
        void toHome(String tag, int flag);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (flag == -3 && !link.equals("")) {
            getFragmentManager().popBackStack();
        }
    }

}
