package com.ormediagroup.youngplus.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ormediagroup.youngplus.bean.BannerBean;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Lau on 2018/11/23.
 */

public class CarouselPagerAdapter extends PagerAdapter {

    private Context context;
    private List<BannerBean> imageList;
    private setOnItemClickListener itemClickListener;

    public CarouselPagerAdapter(Context context, List<BannerBean> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        ImageView imageView = new ImageView(context);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Picasso.get().load(imageList.get(position % imageList.size()).getImg()).config(Bitmap.Config.RGB_565).fit().into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(position);
                }
            }
        });
        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public interface setOnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(setOnItemClickListener onItemClickListener) {
        this.itemClickListener = onItemClickListener;
    }
}
