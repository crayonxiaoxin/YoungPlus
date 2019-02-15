package com.ormediagroup.youngplus.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.lau.API;
import com.ormediagroup.youngplus.lau.LauUtil;
import com.ormediagroup.youngplus.lau.ProcessingDialog;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.internal.Util;

/**
 * Created by Lau on 2019/1/28.
 */

public class ResetPassFragment extends BaseFragment {
    private View view;
    private EditText resetPassEmail;
    private Button resetPassSubmit;
    private TextView resetPassBack;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_reset_pass, container, false);
        initView();
        initData();
        return view;
    }

    private void initData() {
        resetPassBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStackImmediate();
            }
        });
        final ProcessingDialog dialog = new ProcessingDialog(mActivity);
        resetPassSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LauUtil.isEmail(resetPassEmail.getText().toString().trim())) {
                    dialog.loading("正在提交...");
                    new JSONResponse(mActivity, API.API_RESET_PASSWORD, "email=" + resetPassEmail.getText().toString().trim(), new JSONResponse.onComplete() {
                        @Override
                        public void onComplete(JSONObject json) {
                            try {
                                int rc = json.getInt("rc");
                                if (rc == 0) {
                                    dialog.loadingToSuccess("已發送郵件，請查收", 1000).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            resetPassEmail.setText("");
                                            getFragmentManager().popBackStackImmediate();
                                        }
                                    });
                                } else if (rc == -1) {
                                    dialog.loadingToFailed("發送失敗，電郵格式不正確");
                                } else if (rc == -2) {
                                    dialog.loadingToFailed("發送失敗，用戶不存在");
                                } else if (rc == -3) {
                                    dialog.loadingToFailed("發送失敗，請聯絡Young+客服");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                dialog.loadingToFailed("請檢查網絡連接");
                            }
                        }
                    });
                } else {
                    dialog.warning("請輸入正確的電郵").setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            resetPassEmail.requestFocus();
                        }
                    });
                }
            }
        });
    }

    private void initView() {
        resetPassEmail = view.findViewById(R.id.reset_pass_email);
        resetPassSubmit = view.findViewById(R.id.reset_pass_submit);
        resetPassBack = view.findViewById(R.id.reset_pass_back);
    }
}
