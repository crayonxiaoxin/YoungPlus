package com.ormediagroup.youngplus.fragment;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.lau.LauUtil;
import com.ormediagroup.youngplus.lau.ProcessingDialog;
import com.ormediagroup.youngplus.network.JSONResponse;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lau on 2018/12/19.
 */

public class PromotionFragment extends BaseFragment {
    private View view;
    private TextView promotionTopIn;
    private ImageView promotionMiddleImg;
    private TextView promotionMiddleDesc;
    private EditText promotionName, promotionPhone, promotionEmail;
    private Button promotionSubmit;

    private String SUBMIT_URL = "http://youngplus.com.hk/app-promotion";
    private LinearLayout promotionPanel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_promotion, container, false);
        initView();
        initData();
        return view;
    }

    private void initData() {
        promotionTopIn.setText(LauUtil.HTMLTagDecode("<p><b><font color=\\\"#ff0000\\\">" +
                "「內在健康，外在美麗」</font></b>是<b>Young+ </b>的服務理念。" +
                "透過高科技醫學檢測為客戶深入了解身體潛在問題，匯聚醫學、皮膚護理、營養監控及體重管理等專業服務， " +
                "提供<b>量身訂制的個人化「全方位個人化健康評估」</b>，從根源改善體質，達致<b>內在健康，外在美麗</b>。" +
                "</p><p>由即日起至2018年12月31日止，新客戶即可<b>免費享用「全方位專業綜合檢測 (總值HK$2800)」" +
                "</b>的服務，名額有限(首100名)，額滿即止。</p>"));
        Picasso.get()
                .load("http://youngplus.com.hk/wp-content/uploads/2018/12/DEC_01outline_1500x1500-01.jpg")
                .config(Bitmap.Config.RGB_565)
                .resize(LauUtil.dip2px(mActivity, 300), LauUtil.dip2px(mActivity, 300))
                .into(promotionMiddleImg);
        promotionMiddleDesc.setText(LauUtil.HTMLTagDecode("<h5><font color=\"#754C24\">" +
                "<b>「全方位專業綜合檢測 (總值HK$2800)」</b>服務包括:</font></h5><p></p><ol class=\"myol\">" +
                "<li>\t德國智能皮膚管理分析測試</li><li>\t肌肉掃瞄分析</li><li>\t個人身體組成分析</li>" +
                "<li>\t營養師諮詢及報告分析服務</li><li>\t體驗後即可獲贈個人化專業健康方案(3天體驗版)</li>" +
                "<li>\t客戶更可享用以 <b>優惠價$199體驗「註冊脊醫脊骨體態檢查」服務一次</b>。</li></ol>"));
        final ProcessingDialog dialog = new ProcessingDialog(mActivity);
        promotionSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!LauUtil.isNull(promotionName) && !LauUtil.isNull(promotionPhone) && !LauUtil.isNull(promotionEmail)) {
                    if (LauUtil.isPhone(promotionPhone.getText().toString().trim())) {
                        if (LauUtil.isEmail(promotionEmail.getText().toString().trim())) {
                            dialog.loading("正在提交...");
                            String params = "username=" + promotionName.getText().toString()
                                    + "&userphone=" + promotionPhone.getText().toString()
                                    + "&useremail=" + promotionEmail.getText().toString();
                            new JSONResponse(mActivity, SUBMIT_URL, params, new JSONResponse.onComplete() {
                                @Override
                                public void onComplete(JSONObject json) {
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
    }

    private void initView() {
        promotionTopIn = view.findViewById(R.id.promotion_top_in);
        promotionMiddleImg = view.findViewById(R.id.promotion_middle_img);
        promotionMiddleDesc = view.findViewById(R.id.promotion_middle_desc);
        promotionName = view.findViewById(R.id.promotion_name);
        promotionPhone = view.findViewById(R.id.promotion_phone);
        promotionEmail = view.findViewById(R.id.promotion_email);
        promotionSubmit = view.findViewById(R.id.promotion_submit);
        promotionPanel = view.findViewById(R.id.promotion_panel);
    }

}
