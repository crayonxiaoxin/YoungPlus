package com.ormediagroup.youngplus.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.lau.API;
import com.ormediagroup.youngplus.lau.LauUtil;
import com.ormediagroup.youngplus.lau.ProcessingDialog;
import com.ormediagroup.youngplus.lau.User;
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

    private SharedPreferences sp;
    private LinearLayout loginPanel;
    private TextView loginRegister;
    private TextView loginResetPass;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        initView();
        initData();
        return view;
    }

    private void initData() {
        sp = new User(mActivity).getsp();
        final String token = sp.getString("token", "");
        final ProcessingDialog dialog = new ProcessingDialog(mActivity);
        loginSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!LauUtil.isNull(loginEmail) && !LauUtil.isNull(loginPass)) {
                    dialog.loading("登入中...");
                    String params = "username=" + loginEmail.getText().toString() + "&userpass="
                            + loginPass.getText().toString() + "&token=" + token;
                    new JSONResponse(mActivity, API.API_LOGIN, params, new JSONResponse.onComplete() {
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
                                            LoginFragmentListener lfl = (LoginFragmentListener) mActivity;
                                            if (lfl!=null){
                                                lfl.updateLoginStatus();
                                            }
//                                            getFragmentManager().popBackStackImmediate(); // backPressed won't display this fragment
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
        loginRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginFragmentListener lfl = (LoginFragmentListener) mActivity;
                if (lfl!=null){
                    lfl.toRegister();
                }
            }
        });
        loginResetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginFragmentListener lfl = (LoginFragmentListener) mActivity;
                if (lfl!=null){
                    lfl.toResetPass();
                }
            }
        });
    }

    private void initView() {
        loginEmail = view.findViewById(R.id.login_email);
        loginPass = view.findViewById(R.id.login_pass);
        loginSubmit = view.findViewById(R.id.login_submit);
        loginPanel = view.findViewById(R.id.login_panel);
        loginRegister = view.findViewById(R.id.login_register);
        loginResetPass = view.findViewById(R.id.login_reset_pass);
    }

    public interface LoginFragmentListener{
        void updateLoginStatus();
        void toRegister();
        void toResetPass();
    }

}
