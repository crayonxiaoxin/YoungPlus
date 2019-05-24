package com.ormediagroup.youngplus.fragment;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;

import com.ormediagroup.youngplus.R;

import java.util.Map;

/**
 * Created by Lau on 2018/11/23.
 */

public class BaseFragment extends Fragment {

    public String TAG = "ORM";

    protected Activity mActivity;

    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView: ");
        unbindDrawables(getView());
        System.gc();
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

    public String getFullUrl(String url, Map<String, String> params) {
        if (params != null && params.size() > 0) {
            StringBuilder link = new StringBuilder(url + "?");
            int index = 0;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                link.append(entry.getKey()).append("=").append(entry.getValue());
                if (index < params.size() - 1) {
                    link.append("&");
                    index++;
                }
            }
            return link.toString();
        } else {
            return url;
        }
    }

}
