package com.ormediagroup.youngplus.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.bean.SingleSelectBean;
import com.ormediagroup.youngplus.lau.CommonHolder;
import com.ormediagroup.youngplus.lau.SingleViewCommonAdapter;

import java.util.List;

/**
 * Created by Lau on 2019/4/30.
 */
public class SingleSelectAdapter extends SingleViewCommonAdapter<SingleSelectBean> {
    private int selectPosition = 0;

    public SingleSelectAdapter(Context context, List<SingleSelectBean> list, int layoutId) {
        super(context, list, layoutId);
    }

    public SingleSelectAdapter(Context context, List<SingleSelectBean> list, int layoutId, RecyclerView recyclerView) {
        super(context, list, layoutId, recyclerView);
    }

    @Override
    protected void convert(Context context, CommonHolder holder, SingleSelectBean singleSelectBean, int position) {
        holder.setText(R.id.title, singleSelectBean.getTitle());
        if (singleSelectBean.getContent() != null) {
            holder.setText(R.id.content, singleSelectBean.getContent());
        }
        if (singleSelectBean.getImageRes() > 0) {
            holder.setImageResource(R.id.image, singleSelectBean.getImageRes());
        }
        ImageView icon = holder.getView(R.id.status);
        if (position == selectPosition) {
            icon.setImageResource(R.drawable.success);
        } else {
            icon.setImageResource(R.drawable.icon_single_select_dark);
        }
    }

    public void setSelectPosition(int position) {
        this.selectPosition = position;
        notifyDataSetChanged();
    }
}
