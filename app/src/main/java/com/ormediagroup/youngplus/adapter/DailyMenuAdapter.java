package com.ormediagroup.youngplus.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.bean.DailyMenuBean;
import com.ormediagroup.youngplus.lau.CommonHolder;
import com.ormediagroup.youngplus.lau.SingleViewCommonAdapter;

import java.util.List;

/**
 * Created by Lau on 2019/6/5.
 */
public class DailyMenuAdapter extends SingleViewCommonAdapter<DailyMenuBean> {
    public DailyMenuAdapter(Context context, List<DailyMenuBean> list, int layoutId) {
        super(context, list, layoutId);
    }

    public DailyMenuAdapter(Context context, List<DailyMenuBean> list, int layoutId, RecyclerView recyclerView) {
        super(context, list, layoutId, recyclerView);
    }

    @Override
    protected void convert(Context context, CommonHolder holder, DailyMenuBean dailyMenuBean, int position) {
        holder.setText(R.id.title,dailyMenuBean.getTitle()).setText(R.id.time,dailyMenuBean.getTime()).setText(R.id.content,dailyMenuBean.getContent());
    }
}
