package com.ormediagroup.youngplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ormediagroup.youngplus.lau.LauUtil;
import com.ormediagroup.youngplus.lau.ProcessingDialog;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private EditText loginEmail, loginPass;
    private Button loginSubmit;

    private String SUBMIT_URL = "http://youngplus.com.hk/app-login/";
    private String TAG = "ORM";
    private SharedPreferences sp;
    private LinearLayout loginPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initData();
    }

    private void initData() {
        sp = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        final String token = sp.getString("token", "");
        final ProcessingDialog dialog = new ProcessingDialog(this);
        loginSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!LauUtil.isNull(loginEmail) && !LauUtil.isNull(loginPass)) {
                    dialog.loading("登入中...");
                    String params = "username=" + loginEmail.getText().toString() + "&userpass="
                            + loginPass.getText().toString() + "&token=" + token;
                    new JSONResponse(LoginActivity.this, SUBMIT_URL, params, new JSONResponse.onComplete() {
                        @Override
                        public void onComplete(JSONObject json) {
                            try {
                                if (json.getInt("rc") == 0) {
                                    JSONObject data = json.getJSONObject("data");
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("userid", data.get("ID").toString());
                                    editor.putBoolean("isvip", data.getBoolean("is_vip"));
                                    editor.putString("name", data.getString("display_name"));
                                    editor.putString("email", data.getString("user_email"));
                                    editor.apply();
                                    dialog.loadingToSuccess("登入成功").setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            Toast.makeText(LoginActivity.this, "success???", Toast.LENGTH_SHORT).show();
                                            getFragmentManager().popBackStackImmediate(); // backPressed won't display this fragment
                                        }
                                    });
                                    Log.i(TAG, "onComplete: json = " + json.getJSONObject("data"));
                                } else {
                                    dialog.loadingToFailed("登入失敗，賬戶或密碼錯誤");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                dialog.loadingToFailed("請檢查網絡連接");
                            }
                        }
                    });
                } else {
                    dialog.warning("請不要留空").setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            LauUtil.nullEditTextFocus(loginPanel);
                        }
                    });
                }
            }
        });
    }

    private void initView() {
        loginEmail = findViewById(R.id.login_email);
        loginPass = findViewById(R.id.login_pass);
        loginSubmit = findViewById(R.id.login_submit);
        loginPanel = findViewById(R.id.login_panel);
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

}
