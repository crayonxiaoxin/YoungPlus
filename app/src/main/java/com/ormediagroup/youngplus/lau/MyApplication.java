package com.ormediagroup.youngplus.lau;

import android.app.Application;

import com.ormediagroup.youngplus.R;

/**
 * Created by Lau on 2018/12/4.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LoadingAndRetryManager.BASE_EMPTY_LAYOUT_ID = R.layout.base_empty;
        LoadingAndRetryManager.BASE_LOADING_LAYOUT_ID = R.layout.base_loading;
        LoadingAndRetryManager.BASE_RETRY_LAYOUT_ID = R.layout.base_retry;
    }
}
