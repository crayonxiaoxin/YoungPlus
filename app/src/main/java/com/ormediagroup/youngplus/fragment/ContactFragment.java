package com.ormediagroup.youngplus.fragment;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.lau.API;
import com.ormediagroup.youngplus.lau.LauUtil;
import com.ormediagroup.youngplus.lau.ProcessingDialog;
import com.ormediagroup.youngplus.network.JSONResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by Lau on 2018/11/30.
 */

public class ContactFragment extends BaseFragment {
    private View view;
    private EditText bookName, contactName, bookPhone, contactPhone, contactMsg;
    private Spinner bookSex, contactSex, bookService, contactService;
    private Button bookSubmit, contactSubmit;
    private LinearLayout bookPanel1, bookPanel2;

    //    private String debug = "&to=lau@efortunetech.com";
    private String debug = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contact, container, false);
        initView();
        initData();
        return view;
    }

    private void initData() {
        String[] sex = {"男", "女"};
        LauUtil.setSpinner(mActivity, bookSex, sex);
        LauUtil.setSpinner(mActivity, contactSex, sex);
//        String[] services = {"靶向肽療程", "逆齡療程", "營養管理計劃", "中醫診斷及配方",
//                "脊醫診斷及治療", "醫學美容", "DNA基因檢測", "全面體檢"};
        String[] services = {"- 請選擇 -","抗衰老療程", "營養管理計劃", "DNA檢測", "醫療檢測",
                "進階性美容療程", "度身訂造修身 / 體重管理"};
        LauUtil.setSpinner(mActivity, bookService, services);
        LauUtil.setSpinner(mActivity, contactService, services);
//        bookDate.setInputType(InputType.TYPE_NULL);
//        bookDate.setFocusable(false);
//        bookDate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                addDatePicker(bookDate);
//            }
//        });
        ArrayList<String> timesList = new ArrayList<>();
        for (int i = 10; i <= 18; i++) {
            for (int j = 0; j < 60; j += 30) {
                String formatTime = String.format(Locale.getDefault(), "%02d", i) + ":" + String.format(Locale.getDefault(), "%02d", j);
                timesList.add(formatTime);
            }
        }
        String[] times = new String[timesList.size()];
        timesList.toArray(times);
//        setSpinner(bookTime, times);
        final ProcessingDialog dialog = new ProcessingDialog(mActivity);
        bookSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!LauUtil.isNull(bookName) && !LauUtil.isNull(bookPhone)) {
                    if (LauUtil.isPhone(bookPhone.getText().toString())) {
                        dialog.loading("正在提交...");
                        String sexStr = bookSex.getSelectedItem().toString().equals("男") ? "M" : "F";
                        String serviceStr = bookService.getSelectedItem().toString().equals("- 請選擇 -") ? "" : bookService.getSelectedItem().toString();
                        String param = "username=" + bookName.getText() + "&phone=" + bookPhone.getText()
                                + "&sex=" + sexStr + "&service=" + serviceStr
                                + "&action=booking" + debug;
                        new JSONResponse(mActivity, API.API_BOOKING, param, new JSONResponse.onComplete() {
                            @Override
                            public void onComplete(JSONObject json) {
                                try {
                                    if (json.getInt("rc") == 0) {
                                        Log.i(TAG, "onComplete: json = " + json);
                                        dialog.success("提交成功").setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialog) {
                                                bookName.setText("");
                                                bookPhone.setText("");
                                                bookSex.setSelection(0);
                                                bookService.setSelection(0);
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
                        dialog.warning("請輸入8~11位電話號碼").setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                bookPhone.requestFocus();
                            }
                        });
                    }
                } else {
                    dialog.warning("請不要留空").setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            LauUtil.nullEditTextFocus(bookPanel1);
                        }
                    });
                }

            }
        });
        contactSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!LauUtil.isNull(contactName) && !LauUtil.isNull(contactPhone) && !LauUtil.isNull(contactMsg)) {
                    if (LauUtil.isPhone(contactPhone.getText().toString())) {
                        dialog.loading("正在提交...");
                        String sexStr = contactSex.getSelectedItem().toString().equals("男") ? "M" : "F";
                        String serviceStr = contactService.getSelectedItem().toString().equals("- 請選擇 -") ? "" : contactService.getSelectedItem().toString();
                        String param = "username=" + contactName.getText() + "&phone=" + contactPhone.getText()
                                + "&sex=" + sexStr + "&service=" + serviceStr
                                + "&msg=" + contactMsg.getText() + "&action=contact" + debug;
                        new JSONResponse(mActivity, API.API_BOOKING, param, new JSONResponse.onComplete() {
                            @Override
                            public void onComplete(JSONObject json) {
                                try {
                                    if (json.getInt("rc") == 0) {
                                        Log.i(TAG, "onComplete: json = " + json);
                                        dialog.success("提交成功").setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialog) {
                                                contactName.setText("");
                                                contactPhone.setText("");
                                                contactMsg.setText("");
                                                contactSex.setSelection(0);
                                                contactService.setSelection(0);
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
                        dialog.warning("請輸入8~11位電話號碼").setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                contactPhone.requestFocus();
                            }
                        });
                    }
                } else {
                    dialog.warning("請不要留空").setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            LauUtil.nullEditTextFocus(bookPanel2);
                        }
                    });
                }
            }
        });
    }

    private void initView() {
        bookName = view.findViewById(R.id.name1);
        contactName = view.findViewById(R.id.name2);
        bookPhone = view.findViewById(R.id.phone1);
        contactPhone = view.findViewById(R.id.phone2);
        bookSex = view.findViewById(R.id.book_sex1);
        contactSex = view.findViewById(R.id.book_sex2);
        bookService = view.findViewById(R.id.book_service1);
        contactService = view.findViewById(R.id.book_service2);
        contactMsg = view.findViewById(R.id.contact_msg);
        bookSubmit = view.findViewById(R.id.book_submit1);
        contactSubmit = view.findViewById(R.id.book_submit2);
        bookPanel1 = view.findViewById(R.id.bookPanel1);
        bookPanel2 = view.findViewById(R.id.bookPanel2);
    }

//    private void setSpinner(Spinner spinner, String[] array) {
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity, R.layout.support_simple_spinner_dropdown_item, array);
//        spinner.setAdapter(adapter);
//    }

    private String formatDateAndTime(int number) {
        return number < 10 ? "0" + number : "" + number;
    }

    private void addDatePicker(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(mActivity, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.ENGLISH);
                Calendar c2 = (Calendar) c.clone();
                c2.set(Calendar.DATE, dayOfMonth);
                c2.set(Calendar.MONTH, monthOfYear);
                c2.set(Calendar.YEAR, year);
                long timeMills = c2.getTimeInMillis();
                String dayOfWeek = sdf.format(timeMills);
                if (!dayOfWeek.equals("Sunday") && !dayOfWeek.equals("Saturday")) {
                    String formatDate = formatDateAndTime(monthOfYear + 1) + "/" + formatDateAndTime(dayOfMonth) + "/" + year;
                    editText.setText(formatDate);
                } else {
                    Toast.makeText(mActivity, "請選擇工作日", Toast.LENGTH_SHORT).show();
                    editText.callOnClick();
                }
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        DatePicker datePicker = datePickerDialog.getDatePicker();
        datePicker.setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

}
