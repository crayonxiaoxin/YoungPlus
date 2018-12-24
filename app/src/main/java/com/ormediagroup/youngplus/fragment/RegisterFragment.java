package com.ormediagroup.youngplus.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ormediagroup.youngplus.R;
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

    private String SUBMIT_URL = "http://youngplus.com.hk/app-register/";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_register, null);
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
                    if (LauUtil.isEmail(registerEmail.getText().toString().trim())) {
                        String password = registerPass.getText().toString();
                        String passwordAgain = registerPassAgain.getText().toString();
                        if (LauUtil.isPhone(registerPhone.getText().toString().trim())) {
                            if (!password.contains(" ") && password.length() >= 6 && password.length() <= 16) {
                                if (passwordAgain.equals(password)) {
                                    dialog.loading("請稍候...");
                                    String params = "username=" + registerName.getText().toString()
                                            + "&userpass=" + password + "&useremail=" + registerEmail.getText().toString()
                                            + "&userphone=" + registerPhone.getText().toString();
                                    new JSONResponse(mActivity, SUBMIT_URL, params, new JSONResponse.onComplete() {
                                        @Override
                                        public void onComplete(JSONObject json) {
                                            try {
                                                if (json.getInt("rc") == 0) {
                                                    Log.i(TAG, "onComplete: user = " + json.getJSONObject("data"));
                                                    dialog.loadingToSuccess("註冊成功");
                                                } else {
                                                    dialog.loadingToFailed("註冊失敗，姓名或電郵已存在。如有疑問，請聯絡Young+客服。");
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                dialog.loadingToFailed("請檢查網絡連接");
                                            }
                                        }
                                    });
                                } else {
                                    dialog.warning("兩次密碼輸入不一致");
                                }
                            } else {
                                dialog.warning("密碼長度6~16位");
                            }
                        } else {
                            dialog.warning("請輸入8~11位電話號碼");
                        }
                    } else {
                        dialog.warning("請輸入正確的電郵");
                    }
                } else {
                    dialog.warning("請不要留空");
                }
            }
        });
    }

    private void initView() {
        registerName = view.findViewById(R.id.register_name);
        registerEmail = view.findViewById(R.id.register_email);
        registerPhone = view.findViewById(R.id.register_phone);
        registerPass = view.findViewById(R.id.register_pass);
        registerPassAgain = view.findViewById(R.id.register_pass_again);
        registerSubmit = view.findViewById(R.id.register_submit);
    }
}
