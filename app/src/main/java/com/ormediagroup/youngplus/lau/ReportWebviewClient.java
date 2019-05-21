package com.ormediagroup.youngplus.lau;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by Lau on 2018/11/30.
 */

public class ReportWebviewClient extends WebViewClient {
    private Context context;
    ReportWebViewListener listener;
    private boolean isFirst;

    public ReportWebviewClient(Context context, ReportWebViewListener listener) {
        this.context = context;
        this.listener = listener;
        isFirst = true;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (!isFirst) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                breakUrlLoading(String.valueOf(request.getUrl()));
            }
            return true;
        } else {
            isFirst = false;
            return false;
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (!isFirst){
            breakUrlLoading(url);
            return true;
        }else{
            isFirst = false;
            return false;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        view.getSettings().setJavaScriptEnabled(true);
        Log.i("ORM", "onPageStarted: url = " + url);
        super.onPageStarted(view, url, favicon);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onPageFinished(WebView view, String url) {
        view.getSettings().setJavaScriptEnabled(true);
        listener.onPageFinished(view, url);
        super.onPageFinished(view, url);
    }

    private void breakUrlLoading(String url) {
        try {
            url = URLDecoder.decode(url, "utf-8");
            Log.i("ORM", "breakUrlLoading: url = " + url);
            listener.onBreak(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public interface ReportWebViewListener {
        void onBreak(String url);

        void onPageFinished(WebView view, String url);
    }
}
