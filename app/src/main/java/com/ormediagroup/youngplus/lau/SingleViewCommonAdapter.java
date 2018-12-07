package com.ormediagroup.youngplus.lau;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * CommonAdapter for single itemView's RecyclerView
 * Created by Lau on 2018/8/13.
 */

public abstract class SingleViewCommonAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<T> list;
    private int layoutId;
    private setOnItemClickListener itemClickListener;
    private setOnLoadMoreListener loadMoreListener;

    private View headerView, footerView;

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_FOOTER = 2;

    /**
     * 不需要滚动监听时，调用这个constructor
     *
     * @param context
     * @param list
     * @param layoutId
     */
    public SingleViewCommonAdapter(Context context, List<T> list, int layoutId) {
        this.context = context;
        this.list = list;
        this.layoutId = layoutId;
    }

    /**
     * 当需要实现上拉loadmore的时候，调用这个constructor
     *
     * @param context
     * @param list
     * @param layoutId
     * @param recyclerView
     */
    public SingleViewCommonAdapter(Context context, List<T> list, int layoutId, RecyclerView recyclerView) {
        this.context = context;
        this.list = list;
        this.layoutId = layoutId;
        initRecyclerView(recyclerView);
    }

    /**
     * 监听RecyclerView滚动事件，并且对LinearLayout以及GridLayout显示item个数处理
     *
     * @param recyclerView
     */
    private void initRecyclerView(final RecyclerView recyclerView) {
        final RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        final boolean FLAG_LINEAR = manager instanceof LinearLayoutManager;
        final boolean FLAG_GRID = manager instanceof GridLayoutManager;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (FLAG_LINEAR || FLAG_GRID) {
                    int lastVisibleItemPosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
                    switch (newState) {
                        // 上拉加载
                        case SCROLL_STATE_IDLE:
                            if ((lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1) && loadMoreListener != null) {
                                loadMoreListener.loadMore();
                            }
                            break;
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        if (FLAG_GRID) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) manager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int viewType = getItemViewType(position);
                    // header和footer占满spancount
                    if (viewType == TYPE_HEADER || viewType == TYPE_FOOTER) {
                        return gridLayoutManager.getSpanCount();
                    } else {
                        return 1;
                    }
                }
            });
            recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) view.getLayoutParams();
                    int spanSize = layoutParams.getSpanSize();
                    int spanIndex = layoutParams.getSpanIndex();
                    if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
                        outRect.bottom = 0;
                    } else {
                        outRect.bottom = 10;
                    }
                    if (spanSize != gridLayoutManager.getSpanCount()) {
                        if (spanIndex == 0) {
                            outRect.left = 10;
                            outRect.right = 10;
                        } else {
                            outRect.right = 10;
                        }
                    }
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new CommonHolder(headerView, true);
        } else if (viewType == TYPE_FOOTER) {
            return new CommonHolder(footerView, true);
        } else {
            View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
            return new CommonHolder(view, false);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_NORMAL) {
            final int realPosition;
            // 当header!=null时，item1应该加载list.get(0)的数据
            if (hasHeader()) {
                realPosition = position - 1;
            } else {
                realPosition = position;
            }
            CommonHolder commonHolder = (CommonHolder) holder;
            // extend这个adapter时需要实现这个abstract方法进行onBindViewHolder
            convert(context, commonHolder, list.get(realPosition));
            // 点击事件
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(realPosition);
                    }
                }
            });
        } else {
            return;
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && hasHeader()) {
            return TYPE_HEADER;
        } else if (position == getItemCount() - 1 && hasFooter()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_NORMAL;
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

    protected abstract void convert(Context context, CommonHolder holder, T t);

    public interface setOnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(setOnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setHeaderView(View headerView) {
        this.headerView = headerView;
        notifyItemInserted(0);
    }

    public void setFooterView(View footerView) {
        this.footerView = footerView;
        notifyItemInserted(list.size() - 1);
    }

    public boolean hasHeader() {
        return headerView != null;
    }

    public boolean hasFooter() {
        return footerView != null;
    }

    /**
     * 追加数据
     *
     * @param appendList
     */
    public void addAll(List<T> appendList) {
        if (appendList != null && appendList.size() > 0) {
            int start = list.size() - 1;
            list.addAll(appendList);
            notifyItemRangeChanged(start, appendList.size());
        }
    }

    /**
     * 更新数据
     *
     * @param updateList
     */
    public void updateAll(List<T> updateList) {
        if (updateList != null && updateList.size() > 0) {
            list.clear();
            list.addAll(updateList);
            notifyDataSetChanged();
        }
    }

    /**
     * 更新单项数据
     *
     * @param position
     * @param data
     */
    public void updateOne(int position, T data) {
        // 传递item位置
        if (hasHeader()) {
            list.set(position - 1, data);
        } else {
            list.set(position, data);
        }
        notifyItemChanged(position);
    }

    /**
     * 删除某一项
     *
     * @param position
     */
    public void removeOne(int position) {
//        // 传递的是list的position时
//        list.remove(position);
//        if (hasHeader()) {
//            position = position + 1;
//        }

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

    public interface setOnLoadMoreListener {
        void loadMore();
    }

    public void loadMore(setOnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }
}
