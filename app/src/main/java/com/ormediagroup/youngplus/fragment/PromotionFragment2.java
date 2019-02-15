package com.ormediagroup.youngplus.fragment;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.lau.API;
import com.ormediagroup.youngplus.lau.LauUtil;
import com.ormediagroup.youngplus.lau.NoAutoScrollView;
import com.ormediagroup.youngplus.lau.ProcessingDialog;
import com.ormediagroup.youngplus.lau.ServiceWebviewClient;
import com.ormediagroup.youngplus.loadAndRetry.LoadingAndRetryManager;
import com.ormediagroup.youngplus.loadAndRetry.OnLoadingAndRetryListener;
import com.ormediagroup.youngplus.network.JSONResponse;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lau on 2018/12/19.
 */

public class PromotionFragment2 extends BaseFragment {
    private View view;
    private TextView promotionTopIn;
    private WebView promotionMiddleDesc;
    private EditText promotionName, promotionPhone, promotionEmail;
    private Button promotionSubmit;

    //    private String debug = "&to=lau@efortunetech.com";
    private String debug = "";

    private LinearLayout promotionPanel;
    private ScrollView promotionParentLayout;
    private LoadingAndRetryManager loadingAndRetryManager;
    private Spinner promotionSex;

    public static PromotionFragment2 newInstance(int id) {
        PromotionFragment2 f = new PromotionFragment2();
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        f.setArguments(bundle);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_promotion2, container, false);
        initView();
        initData();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadingAndRetryManager = LoadingAndRetryManager.generate(this.promotionParentLayout, new OnLoadingAndRetryListener() {
            @Override
            public void setRetryEvent(View retryView) {
                retryView.findViewById(R.id.base_retry).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadingAndRetryManager.showLoading();
                        initData();
                    }
                });
            }
        });
        loadingAndRetryManager.showLoading();
    }

    private void initData() {
        String[] sex = {"男", "女"};
        LauUtil.setSpinner(mActivity, promotionSex, sex);
        Bundle bundle = getArguments();
        if (bundle != null) {
            int id = bundle.getInt("id", 0);
            if (id != 0) {
                new JSONResponse(mActivity, API.API_GET_PROMOTION, "id=" + id, new JSONResponse.onComplete() {
                    @Override
                    public void onComplete(JSONObject json) {
                        loadingAndRetryManager.showContent();
                        try {
                            JSONObject data = json.getJSONObject("data");
                            final String title = data.getString("title");
                            String top_desc = data.getString("top_desc");
                            String middle_desc = data.getString("middle_desc");
                            promotionTopIn.setText(LauUtil.HTMLTagDecode(top_desc));
                            promotionMiddleDesc.loadDataWithBaseURL("file:///android_asset/",
                                    LauUtil.formatHTML(middle_desc),
                                    "text/html;charset=UTF-8",
                                    null,
                                    null);
                            WebSettings ws = promotionMiddleDesc.getSettings();
                            ws.setJavaScriptEnabled(true);
                            ws.setLoadsImagesAutomatically(true);
                            ws.setSupportZoom(false);
                            ws.setDomStorageEnabled(true);
//                            ws.setNeedInitialFocus(false);
                            promotionMiddleDesc.setWebViewClient(new ServiceWebviewClient(mActivity));
                            final ProcessingDialog dialog = new ProcessingDialog(mActivity);
                            promotionSubmit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!LauUtil.isNull(promotionName) && !LauUtil.isNull(promotionPhone) && !LauUtil.isNull(promotionEmail)) {
                                        if (LauUtil.isPhone(promotionPhone.getText().toString().trim())) {
                                            if (LauUtil.isEmail(promotionEmail.getText().toString().trim())) {
                                                dialog.loading("正在提交...");
                                                String sexStr = promotionSex.getSelectedItem().toString().equals("男") ? "M" : "F";
                                                String params = "username=" + promotionName.getText().toString()
                                                        + "&userphone=" + promotionPhone.getText().toString()
                                                        + "&useremail=" + promotionEmail.getText().toString()
                                                        + "&usersex=" + sexStr
                                                        + "&title=" + title + debug;
                                                Log.i(TAG, "onClick: promotion params = " + params);
                                                new JSONResponse(mActivity, API.API_ADD_PROMOTION, params, new JSONResponse.onComplete() {
                                                    @Override
                                                    public void onComplete(JSONObject json) {
                                                        Log.i(TAG, "onComplete: promotion = " + json);
                                                        try {
                                                            if (json.getInt("rc") == 0) {
                                                                dialog.loadingToSuccess("提交成功").setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                                    @Override
                                                                    public void onDismiss(DialogInterface dialog) {
                                                                        promotionName.setText("");
                                                                        promotionPhone.setText("");
                                                                        promotionEmail.setText("");
                                                                    }
                                                                });
                                                            } else {
                                                                dialog.loadingToFailed("提交失敗，請聯絡Young+客服");
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
                                                        promotionEmail.requestFocus();
                                                    }
                                                });
                                            }
                                        } else {
                                            dialog.warning("請輸入8~11位電話號碼").setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialog) {
                                                    promotionPhone.requestFocus();
                                                }
                                            });
                                        }
                                    } else {
                                        dialog.warning("請不要留空").setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialog) {
                                                LauUtil.nullEditTextFocus(promotionPanel);
                                            }
                                        });
                                    }
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    private void initView() {
        promotionTopIn = view.findViewById(R.id.promotion_top_in);
        promotionMiddleDesc = view.findViewById(R.id.promotion_middle_desc);
        promotionName = view.findViewById(R.id.promotion_name);
        promotionPhone = view.findViewById(R.id.promotion_phone);
        promotionEmail = view.findViewById(R.id.promotion_email);
        promotionSex = view.findViewById(R.id.promotion_sex);
        promotionSubmit = view.findViewById(R.id.promotion_submit);
        promotionPanel = view.findViewById(R.id.promotion_panel);
        promotionParentLayout = view.findViewById(R.id.promotion_parent);

        // 解决EditText抢占焦点问题
        promotionParentLayout.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        promotionParentLayout.setFocusable(true);
        promotionParentLayout.setFocusableInTouchMode(true);
        promotionParentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
            }
        });

    }

}
