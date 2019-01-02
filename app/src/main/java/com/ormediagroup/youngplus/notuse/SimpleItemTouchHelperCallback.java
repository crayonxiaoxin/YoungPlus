package com.ormediagroup.youngplus.notuse;

import android.graphics.Canvas;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.ViewGroup;

/**
 * Created by Lau on 2018/12/27.
 */

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final SimpleItemTouchHelperAdapter mAdapter;

    public SimpleItemTouchHelperCallback(SimpleItemTouchHelperAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            final int swipeFlags = ItemTouchHelper.START;
            return makeMovementFlags(dragFlags, swipeFlags);
        } else if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            final int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        }
        return 0;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (viewHolder.getItemViewType() != target.getItemViewType()) {
            return false;
        }
        mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            Log.i("ORM", "onChildDraw: " + dX);
//            if (Math.abs(dX) <= getSliderLimitation(viewHolder)) {
//                Log.i("ORM", "onChildDraw: dX=" + dX + " Item=" + getSliderLimitation(viewHolder));
//                viewHolder.itemView.scrollTo(-(int) dX, 0);
//            } else {
//                viewHolder.itemView.scrollTo(-(int) dX, 0);
//            }

            int scrollX = getSliderLimitation(viewHolder);
            if (dX < -scrollX) {
                dX = -scrollX;
                Log.i("ORM", "onChildDraw: dX=" + dX + " Item=" + getSliderLimitation(viewHolder));
                viewHolder.itemView.scrollTo(-(int) dX, 0);
            } else {
                viewHolder.itemView.scrollTo(0, 0);
//                super.onChildDraw(c, recyclerView, viewHolder, scrollX, dY, actionState, isCurrentlyActive);
            }

//            int translateX = getSliderLimitation(viewHolder);
//            if (viewHolder.itemView!=null){
//                if (dX<-translateX){
//                    dX = -translateX;
//                    viewHolder.itemView.setTranslationX(dX);
//                }else{
//                    viewHolder.itemView.setTranslationX(dX);
//                }
//            }else{
//                return;
//            }
            Log.i("ORM", "onChildDraw: " + dX);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setScrollX(0);

    }

    public int getSliderLimitation(RecyclerView.ViewHolder viewHolder) {
        ViewGroup viewGroup = (ViewGroup) viewHolder.itemView;
        return viewGroup.getChildAt(1).getWidth();
    }
}
