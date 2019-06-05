package com.ormediagroup.youngplus.notuse;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.bean.MenuBean;
import com.ormediagroup.youngplus.lau.CommonHolder;
import com.ormediagroup.youngplus.lau.SingleViewCommonAdapter;

import java.util.Collections;
import java.util.List;

/**
 * Created by Lau on 2018/12/27.
 */

public class TestAdapter extends SingleViewCommonAdapter<MenuBean> implements SimpleItemTouchHelperAdapter {

    private final OnStartDragListener onStartDragListener;
    private List<MenuBean> list;

    public TestAdapter(Context context, List<MenuBean> list, int layoutId, OnStartDragListener onStartDragListener) {
        super(context, list, layoutId);
        this.onStartDragListener = onStartDragListener;
        this.list = list;
    }

    public TestAdapter(Context context, List<MenuBean> list, int layoutId, RecyclerView recyclerView, OnStartDragListener onStartDragListener) {
        super(context, list, layoutId, recyclerView);
        this.onStartDragListener = onStartDragListener;
        this.list = list;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(list, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
//        list.remove(position);
//        notifyItemRemoved(position);
    }

    @Override
    protected void convert(final Context context, final CommonHolder holder, MenuBean menuBean,int position) {
        holder.setText(R.id.menu_item,menuBean.getTitle());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onStartDragListener.onStartDrag(holder);
                return false;
            }
        });
        holder.setOnClickListener(R.id.test_del, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"delete",Toast.LENGTH_SHORT).show();
            }
        });
        holder.setOnClickListener(R.id.test_more, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"more",Toast.LENGTH_SHORT).show();
            }
        });
//        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                onStartDragListener.onStartDrag(holder);
//                return false;
//            }
//        });
    }


}
