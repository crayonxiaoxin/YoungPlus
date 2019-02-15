package com.ormediagroup.youngplus.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.ormediagroup.youngplus.MainActivity;
import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.lau.API;
import com.ormediagroup.youngplus.lau.LauUtil;
import com.ormediagroup.youngplus.lau.ProcessingDialog;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lau on 2018/12/20.
 */

public class RegisterFragment extends BaseFragment {
    private View view;
    private EditText registerName, registerEmail, registerPhone, registerPass, registerPassAgain;
    private Button registerSubmit;

    private LinearLayout registerPanel;
    private TextView registerLogin;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_register, container, false);
        initView();
        initData();
        return view;
    }

    private void initData() {
        final ProcessingDialog dialog = new ProcessingDialog(mActivity);
        registerSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!LauUtil.isNull(registerName) && !LauUtil.isNull(registerPhone)
                        && !LauUtil.isNull(registerEmail) && !LauUtil.isNull(registerPass)
                        && !LauUtil.isNull(registerPassAgain)) {
                    if (LauUtil.isPhone(registerPhone.getText().toString().trim())) {
                        if (LauUtil.isEmail(registerEmail.getText().toString().trim())) {
                            String password = registerPass.getText().toString();
                            String passwordAgain = registerPassAgain.getText().toString();

                            if (!password.contains(" ") && password.length() >= 6 && password.length() <= 16) {
                                if (passwordAgain.equals(password)) {
                                    dialog.loading("請稍候...");
                                    String params = "username=" + registerName.getText().toString()
                                            + "&userpass=" + password + "&useremail=" + registerEmail.getText().toString()
                                            + "&userphone=" + registerPhone.getText().toString();
                                    new JSONResponse(mActivity, API.API_REGISTER, params, new JSONResponse.onComplete() {
                                        @Override
                                        public void onComplete(JSONObject json) {
                                            try {
                                                if (json.getInt("rc") == 0) {
                                                    Log.i(TAG, "onComplete: user = " + json.getJSONObject("data"));
                                                    dialog.loadingToSuccess("註冊成功").setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                        @Override
                                                        public void onDismiss(DialogInterface dialog) {
                                                            initEditTexts();
                                                            toLogin();
                                                        }
                                                    });
                                                } else {
                                                    dialog.loadingToFailed("註冊失敗，電郵已存在。如有疑問，請聯絡Young+客服。");
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                dialog.loadingToFailed("請檢查網絡連接");
                                            }
                                        }
                                    });
                                } else {
                                    dialog.warning("兩次密碼輸入不一致").setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            registerPassAgain.requestFocus();
                                        }
                                    });
                                }
                            } else {
                                dialog.warning("密碼長度6~16位").setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        registerPass.requestFocus();
                                    }
                                });
                            }
                        } else {
                            dialog.warning("請輸入正確的電郵").setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    registerEmail.requestFocus();
                                }
                            });
                        }
                    } else {
                        dialog.warning("請輸入8~11位電話號碼").setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                registerPhone.requestFocus();
                            }
                        });
                    }
                } else {
                    dialog.warning("請不要留空").setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            LauUtil.nullEditTextFocus(registerPanel);
                        }
                    });
                }
            }
        });
        registerLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStackImmediate();
            }
        });
    }

    private void initEditTexts() {
        registerName.setText("");
        registerPhone.setText("");
        registerEmail.setText("");
        registerPass.setText("");
        registerPassAgain.setText("");
    }

    private void initView() {
        registerName = view.findViewById(R.id.register_name);
        registerEmail = view.findViewById(R.id.register_email);
        registerPhone = view.findViewById(R.id.register_phone);
        registerPass = view.findViewById(R.id.register_pass);
        registerPassAgain = view.findViewById(R.id.register_pass_again);
        registerSubmit = view.findViewById(R.id.register_submit);
        registerPanel = view.findViewById(R.id.register_panel);
        registerLogin = view.findViewById(R.id.register_login);
    }

    private void toLogin(){
        new AlertDialog.Builder(mActivity)
                .setIcon(R.mipmap.ic_youngplus)
                .setTitle("註冊成功")
                .setMessage("是否立即登入？")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getFragmentManager().popBackStackImmediate();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

}
