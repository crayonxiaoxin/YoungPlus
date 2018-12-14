package com.ormediagroup.youngplus.lau;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * CommonHolder for RecyclerView
 * Created by Lau on 2018/8/13.
 */

public class CommonHolder extends RecyclerView.ViewHolder {

    private SparseArray<View> mViews;

    public CommonHolder(View itemView, boolean isHeaderOrFooter) {
        super(itemView);
        if (isHeaderOrFooter) {
            return;
        }
        mViews = new SparseArray<>();
    }

    /**
     * 从稀疏数组中取出对应的view，没有则findViewById添加到稀疏数组中
     *
     * @param resID 资源id
     * @param <T>   任意类型view
     * @return
     */
    public <T extends View> T getView(int resID) {
        View view = mViews.get(resID);
        if (view == null) {
            view = itemView.findViewById(resID);
            mViews.put(resID, view);
        }
        return (T) view;
    }

    public void setItemBackGroundColor(int color) {
        itemView.setBackgroundColor(color);
    }

    public CommonHolder setText(int resID, String text) {
        TextView textView = getView(resID);
        textView.setText(text);
        return this;
    }

    public CommonHolder setImageResource(int resID, int imageId) {
        ImageView imageView = getView(resID);
        imageView.setImageResource(imageId);
        return this;
    }

    public CommonHolder setImageURL(int resID, String url) {
        ImageView imageView = getView(resID);
        Picasso.get().load(url).resize(300, 300).into(imageView);
        return this;
    }

    public void setOnItemClickListener(View.OnClickListener listener) {
        itemView.setOnClickListener(listener);
    }

    public void setOnClickListener(int resID, View.OnClickListener listener) {
        getView(resID).setOnClickListener(listener);
    }

    // ... and so on.
}
