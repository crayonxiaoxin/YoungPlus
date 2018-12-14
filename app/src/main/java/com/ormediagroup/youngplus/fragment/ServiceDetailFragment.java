package com.ormediagroup.youngplus.fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ormediagroup.youngplus.MainActivity;
import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.lau.LauUtil;
import com.ormediagroup.youngplus.loadAndRetry.LoadingAndRetryManager;
import com.ormediagroup.youngplus.loadAndRetry.OnLoadingAndRetryListener;
import com.ormediagroup.youngplus.lau.ServiceWebviewClient;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Lau on 2018/11/26.
 */

public class ServiceDetailFragment extends BaseFragment {

    private String REQUEST_URL = "http://youngplus.com.hk/app-get-service-detail";
    private String link = "";
    private int flag = 0;

    private View view;
    private WebView service_detail;
    private LoadingAndRetryManager loadingAndRetryManager;
    private RelativeLayout parentLayout;
    private FloatingActionButton shareBtn;

    public static ServiceDetailFragment newInstance(int id) {
        ServiceDetailFragment f = new ServiceDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        f.setArguments(bundle);
        return f;
    }

    public static ServiceDetailFragment newInstance(String title, String url) {
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
//        shareBtn = new ImageView(mActivity);
        shareBtn = new FloatingActionButton(mActivity);
        RelativeLayout.LayoutParams shareBtnParams = new RelativeLayout.LayoutParams(LauUtil.dip2px(mActivity, 60), LauUtil.dip2px(mActivity, 60));
        shareBtnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        shareBtnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        shareBtnParams.setMargins(20, 20, 20, 40);
        shareBtn.setLayoutParams(shareBtnParams);
        shareBtn.setImageResource(R.drawable.icon_share_white);
//        shareBtn.setImageBitmap(texta);
        shareBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#754c24")));
        shareBtn.setScaleType(ImageView.ScaleType.CENTER);
//        shareBtn.setAdjustViewBounds(true);
//        shareBtn.setBackgroundResource(R.drawable.shape_test);
        parentLayout.addView(shareBtn);
        shareBtn.bringToFront();
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
                                final String shareTitle = json.getJSONObject("data").getString("post_title");
                                final String shareLink = "http://youngplus.com.hk/" + json.getJSONObject("data").getString("post_name");
                                shareBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        shareLink(shareTitle, shareLink);
                                    }
                                });
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
        // 没有page时
        if (flag == -3 && !link.equals("")) {
            getFragmentManager().popBackStack();
        }
    }

    public void shareLink(String title, String link) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "Young + " + title + " - " + link);
        intent.setType("text/plain");
        this.startActivity(Intent.createChooser(intent, "分享連結"));
    }

}
