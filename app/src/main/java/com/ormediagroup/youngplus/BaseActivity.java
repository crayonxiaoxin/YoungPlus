package com.ormediagroup.youngplus;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by Lau on 2019/2/25.
 */

public class BaseActivity extends AppCompatActivity {

    protected int getStatusBarHeight() {
        int result = 0;
        Context context1 = getApplicationContext();
        int resourceId = context1.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context1.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    protected void initImmersiveStatusBar() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // 4.4
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 5.0
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); // 確認取消半透明設置。
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 全螢幕顯示，status bar 不隱藏，activity 上方 layout 會被 status bar 覆蓋。
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE); // 配合其他 flag 使用，防止 system bar 改變後 layout 的變動。
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); // 跟系統表示要渲染 system bar 背景。
            window.setStatusBarColor(Color.TRANSPARENT);
            setStatusBarView();
        }

    }

    public void setStatusBarView() {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    protected void replaceFragment(Fragment f, String tag, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment f1 = fm.findFragmentByTag(tag);
        if (f1 == null) {
//            ft.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left);
            ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            ft.replace(R.id.frameLayout, f, tag);
            if (addToBackStack) {
                ft.addToBackStack(tag);
            }
            ft.commit();
        } else {
            fm.popBackStack(tag, 0);
        }
    }

//    protected void addFragment(Fragment f, String tag, boolean addToBackStack) {
////        FragmentManager fm = getSupportFragmentManager();
////
////        Fragment f1 = fm.findFragmentByTag(tag);
////        if (f1 == null) {
////            FragmentTransaction ft = fm.beginTransaction();
////            ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
//////            ft.hide(fm.findFragmentById(R.id.frameLayout));
////            ft.add(R.id.frameLayout, f, tag);
////            if (addToBackStack) {
////                ft.addToBackStack(tag);
////            }
////            ft.commit();
////        } else {
////            fm.popBackStack(tag, 0);
////        }
//
//
//        FragmentManager fm = getSupportFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();
//        Fragment f1 = fm.findFragmentByTag(tag);
//        ft.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left);
////        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
//        if (f1 == null) {
//            ft.add(R.id.frameLayout, f, tag);
//            if (addToBackStack) {
//                ft.addToBackStack(tag);
//            }
//            ft.commit();
//        } else {
//            fm.popBackStack(tag, 0);
//        }
//
//    }

    protected void addFragment(int frameLayoutId, Fragment f, String tag, boolean addToBackStack, boolean allowingStateLoss) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment f1 = fm.findFragmentByTag(tag);
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        if (f1 == null) {
            ft.add(frameLayoutId, f, tag);
            if (addToBackStack) {
                ft.addToBackStack(tag);
            }
            if (allowingStateLoss) {
                ft.commitAllowingStateLoss();
            } else {
                ft.commit();
            }
        } else {
            fm.popBackStack(tag, 0);
        }
    }
}
