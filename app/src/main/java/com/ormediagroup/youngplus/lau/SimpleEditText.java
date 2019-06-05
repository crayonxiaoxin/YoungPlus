package com.ormediagroup.youngplus.lau;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ormediagroup.youngplus.R;

/**
 * Created by Lau on 2019/5/31.
 */
public class SimpleEditText extends LinearLayout {

    private int textWidth;
    private String title;
    private String hint;
    private int editLines;
    private View view;
    private TextView textView;
    private EditText editText;
    private int inputType;

    public SimpleEditText(Context context) {
        super(context);
        init(context, null);
    }

    public SimpleEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SimpleEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleEditText);
        textWidth = typedArray.getLayoutDimension(R.styleable.SimpleEditText_text_width, 0);
        editLines = typedArray.getInt(R.styleable.SimpleEditText_edit_lines, 1);
        title = typedArray.getString(R.styleable.SimpleEditText_title);
        hint = typedArray.getString(R.styleable.SimpleEditText_hint);
        inputType = typedArray.getInt(R.styleable.SimpleEditText_input_type, 1);
        typedArray.recycle();
        view = LayoutInflater.from(context).inflate(R.layout.custom_simple_edittext, this, false);
        textView = view.findViewById(R.id.textView);
        if (textWidth > 0) {
            textView.setWidth(textWidth);
        }
        textView.setText(title);
        editText = view.findViewById(R.id.editText);
        editText.setHint(hint);
        switch (inputType) {
            case 1:
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case 2:
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case 3:
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case 4:
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                break;
        }
        if (editLines == 1) {
            editText.setSingleLine(true);
            editText.setMaxLines(editLines);
        } else {
            editText.setSingleLine(false);
        }
        editText.setLines(editLines);
        addView(view);
    }

    public TextView getTextView() {
        return this.textView;
    }

    public EditText getEditText() {
        return this.editText;
    }

    public String getValue() {
        return this.editText.getText().toString();
    }

    public void setValue(String str) {
        editText.setText(str);
    }

}
