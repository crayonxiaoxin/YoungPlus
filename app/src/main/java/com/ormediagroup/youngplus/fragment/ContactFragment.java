package com.ormediagroup.youngplus.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.ormediagroup.youngplus.R;

import java.util.Calendar;

/**
 * Created by Lau on 2018/11/30.
 */

public class ContactFragment extends BaseFragment {
    private View view;
    private EditText bookName, contactName, bookPhone, contactPhone, contactMsg, bookDate;
    private Spinner bookSex, contactSex, bookService, contactService, bookTime;
    private Button bookSubmit, contactSubmit;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contact, null);
        initView();
        initData();
        return view;
    }

    private void initData() {
        String[] sex = {"男", "女"};
        setSpinner(bookSex, sex);
        setSpinner(contactSex, sex);
        String[] services = {"靶向肽療程", "逆齡療程", "營養管理計劃", "中醫診斷及配方",
                "脊醫診斷及治療", "醫學美容", "DNA基因檢測", "全面體檢"};
        setSpinner(bookService, services);
        setSpinner(contactService, services);
        bookDate.setInputType(InputType.TYPE_NULL);
        bookDate.setFocusable(false);
        bookDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDatePicker(bookDate);
            }
        });
        String[] times = {"10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00", "13:30",
                "14:00", "14:30", "15:00", "15:30", "14:00", "14:30", "15:00", "15:30", "16:00",
                "16:30", "17:00", "17:30", "18:00", "18:30"};
        setSpinner(bookTime, times);
        bookSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mActivity, bookName.getText() + " " + bookPhone.getText() +
                        " " + bookSex.getSelectedItem() + " " + bookDate.getText() + " " +
                        bookTime.getSelectedItem() + " " + bookService.getSelectedItem(), Toast.LENGTH_SHORT).show();
            }
        });
        contactSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mActivity, contactName.getText() + " " + contactPhone.getText() +
                        " " + contactSex.getSelectedItem() + " " + contactService.getSelectedItem() +
                        " " + contactMsg.getText(), Toast.LENGTH_SHORT).show();
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
        bookDate = view.findViewById(R.id.book_date1);
        bookTime = view.findViewById(R.id.book_time1);
        bookService = view.findViewById(R.id.book_service1);
        contactService = view.findViewById(R.id.book_service2);
        contactMsg = view.findViewById(R.id.contact_msg);
        bookSubmit = view.findViewById(R.id.book_submit1);
        contactSubmit = view.findViewById(R.id.book_submit2);
    }

    private void setSpinner(Spinner spinner, String[] array) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity, R.layout.support_simple_spinner_dropdown_item, array);
        spinner.setAdapter(adapter);
    }

    private String formatDateAndTime(int number) {
        return number < 10 ? "0" + number : "" + number;
    }

    private void addDatePicker(final EditText editText) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(mActivity, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                editText.setText(formatDateAndTime(monthOfYear + 1) + "/" + formatDateAndTime(dayOfMonth) + "/" + year);
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        DatePicker datePicker = datePickerDialog.getDatePicker();
        datePicker.setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }
}
