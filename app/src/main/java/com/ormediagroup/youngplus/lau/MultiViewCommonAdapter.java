package com.ormediagroup.youngplus.lau;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * CommonAdapter for multiple itemView's RecyclerView but only for single dataType
 * Created by Lau on 2018/12/6.
 */

public abstract class MultiViewCommonAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<T> list;

    private View headerView, footerView;
    private int TYPE_HEADER = 9998;
    private int TYPE_FOOTER = 9999;

    protected MultiViewCommonAdapter(Context context, List<T> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new CommonHolder(headerView, true);
        } else if (viewType == TYPE_FOOTER) {
            return new CommonHolder(footerView, true);
        } else {
            return defineViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) != TYPE_HEADER && getItemViewType(position) != TYPE_FOOTER) {
            int realPosition = hasHeader() ? position - 1 : position;
            convert(context, (CommonHolder) holder, realPosition, list.get(realPosition), getItemViewType(position));
        }
    }

    @Override
    public int getItemCount() {
        if (hasHeader() && !hasFooter()) {
            return list.size() + 1;
        } else if (!hasHeader() && hasFooter()) {
            return list.size() + 1;
        } else if (hasHeader() && hasFooter()) {
            return list.size() + 2;
        } else {
            return list.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && hasHeader()) {
            return TYPE_HEADER;
        } else if (position == getItemCount() - 1 && hasFooter()) {
            return TYPE_FOOTER;
        } else {
            int realPosition = hasHeader() ? position - 1 : position;
            return setItemViewType(list.get(realPosition));
        }
    }

    protected CommonHolder createHolder(ViewGroup parent, int layoutID) {
        return new CommonHolder(LayoutInflater.from(context).inflate(layoutID, parent, false), false);
    }

    public void setHeaderView(View headerView) {
        this.headerView = headerView;
        notifyItemInserted(0);
    }

    public void setFooterView(View footerView) {
        this.footerView = footerView;
        notifyItemInserted(list.size() - 1);
    }

    private boolean hasHeader() {
        return headerView != null;
    }

    private boolean hasFooter() {
        return footerView != null;
    }

    public void addAll(List<T> appendList) {
        if (appendList != null && appendList.size() > 0) {
            int start = list.size() - 1;
            list.addAll(appendList);
            notifyItemRangeChanged(start, appendList.size());
        }
    }

    public void updateAll(List<T> updateList) {
        if (updateList != null && updateList.size() > 0) {
            list.clear();
            list.addAll(updateList);
            notifyDataSetChanged();
        }
    }

    public void updateOne(int position, T data) {
        // 传递item位置
        if (hasHeader()) {
            list.set(position - 1, data);
        } else {
            list.set(position, data);
        }
        notifyItemChanged(position);
    }

    public void removeOne(int position) {
        // 传递item位置
        if (hasHeader()) {
            list.remove(position - 1);
        } else {
            list.remove(position);
        }
        notifyItemRemoved(position);
        // 刷新后面所有item位置，防止数组越界
        notifyItemRangeChanged(position, list.size() - position);
    }

    public abstract int setItemViewType(T bean);

    public abstract CommonHolder defineViewHolder(ViewGroup parent, int viewType);

    public abstract void convert(Context context, CommonHolder holder, int position, T bean, int viewType);
}
