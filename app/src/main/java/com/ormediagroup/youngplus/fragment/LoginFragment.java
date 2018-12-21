package com.ormediagroup.youngplus.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.lau.LauUtil;
import com.ormediagroup.youngplus.lau.ProcessingDialog;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lau on 2018/12/20.
 */

public class LoginFragment extends BaseFragment {
    private View view;
    private EditText loginEmail, loginPass;
    private Button loginSubmit;

    private String SUBMIT_URL = "http://youngplus.com.hk/app-login/";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, null);
        initView();
        initData();
        return view;
    }

    private void initData() {
        final ProcessingDialog dialog = new ProcessingDialog(mActivity);
        loginSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!LauUtil.isNull(loginEmail) && !LauUtil.isNull(loginPass)) {
                    dialog.loading("登入中...");
                    String params = "username=" + loginEmail.getText().toString() + "&userpass="
                            + loginPass.getText().toString();
                    new JSONResponse(mActivity, SUBMIT_URL, params, new JSONResponse.onComplete() {
                        @Override
                        public void onComplete(JSONObject json) {
                            try {
                                if (json.getInt("rc") == 0) {
                                    dialog.completed("登入成功", 2000);
                                    dialog.getDialog(1).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            Toast.makeText(mActivity, "success???", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    Log.i(TAG, "onComplete: json = " + json.getJSONObject("data"));
                                } else {
                                    dialog.failed("登入失敗，賬戶或密碼錯誤", 2000);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                dialog.failed("請檢查網絡連接", 2000);
                            }
                        }
                    });
                } else {
                    dialog.warning("請不要留空", 1000);
                }
            }
        });
    }

    private void initView() {
        loginEmail = view.findViewById(R.id.login_email);
        loginPass = view.findViewById(R.id.login_pass);
        loginSubmit = view.findViewById(R.id.login_submit);
    }
}
