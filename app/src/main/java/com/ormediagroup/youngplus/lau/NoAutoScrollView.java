package com.ormediagroup.youngplus.lau;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

/**
 * Created by Lau on 2019/1/10.
 */

public class NoAutoScrollView extends ScrollView {
    public NoAutoScrollView(Context context) {
        super(context);
    }

    public NoAutoScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoAutoScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
    }
}
