package com.ormediagroup.youngplus.lau;

import android.content.Context;
import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by Lau on 2018/11/30.
 */

public class ServiceWebviewClient extends WebViewClient {
    private Context context;

    public ServiceWebviewClient(Context context) {
        this.context = context;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            breakUrlLoading(String.valueOf(request.getUrl()));
        }
        return true;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        breakUrlLoading(url);
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        view.getSettings().setJavaScriptEnabled(true);
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        view.getSettings().setJavaScriptEnabled(true);
        super.onPageFinished(view, url);
    }

    private void breakUrlLoading(String url) {
        try {
            url = URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String[] array = url.split("/");
        ServiceWebviewListener swvl = (ServiceWebviewListener) context;
        if (swvl != null) {
            swvl.toDetailByTitle(array[array.length - 1], url);
        }
    }

    public interface ServiceWebviewListener {
        void toDetailByTitle(String title, String url);
    }
}
