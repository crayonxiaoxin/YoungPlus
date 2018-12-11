package com.ormediagroup.youngplus.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.adapter.CarouselPagerAdapter;
import com.ormediagroup.youngplus.bean.BannerBean;
import com.ormediagroup.youngplus.bean.ServicesBean;
import com.ormediagroup.youngplus.lau.SingleViewCommonAdapter;
import com.ormediagroup.youngplus.lau.CommonHolder;
import com.ormediagroup.youngplus.lau.LauUtil;
import com.ormediagroup.youngplus.loadAndRetry.LoadingAndRetryManager;
import com.ormediagroup.youngplus.loadAndRetry.OnLoadingAndRetryListener;
import com.ormediagroup.youngplus.network.JSONResponse;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lau on 2018/11/23.
 */

public class HomeFragment extends BaseFragment {
    private View view;
    private ViewPager viewPager;
    private CarouselPagerAdapter adapter;
    private final static int HOME_AD_LOOP = 1;
    private String SERVICE_URL = "http://youngplus.com.hk/app-get-services";
    private LoadingAndRetryManager loadingAndRetryManager;
    private NestedScrollView parentLayout;

    static class myHandler extends Handler {
        private final WeakReference<Activity> mActivity;
        private ViewPager viewPager;

        public myHandler(Activity activity, ViewPager viewPager) {
            this.mActivity = new WeakReference<Activity>(activity);
            this.viewPager = viewPager;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HOME_AD_LOOP:
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                    break;
            }
        }
    }

    private RecyclerView aceServiceView;
    private RecyclerView healthManagementView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, null);
        initView();
        initData();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadingAndRetryManager = LoadingAndRetryManager.generate(this.parentLayout, new OnLoadingAndRetryListener() {
            @Override
            public void setRetryEvent(View retryView) {
                retryView.findViewById(R.id.base_retry).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadingAndRetryManager.showLoading();
                        initData();
                    }
                });
            }
        });
        loadingAndRetryManager.showLoading();
    }

    private void initData() {
        new JSONResponse(mActivity, SERVICE_URL, "", new JSONResponse.onComplete() {
            @Override
            public void onComplete(JSONObject json) {
                if (!json.isNull("data")) {
                    loadingAndRetryManager.showContent();
                    List<BannerBean> list = new ArrayList<>();
                    try {
                        JSONArray banners = json.getJSONObject("data").getJSONArray("homeBanner");
                        for (int i = 0; i < banners.length(); i++) {
                            list.add(new BannerBean(banners.getString(i)));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    adapter = new CarouselPagerAdapter(mActivity, list);
                    viewPager.setAdapter(adapter);
                    viewPager.setCurrentItem(list.size() * 10000, false);
                    adapter.setOnItemClickListener(new CarouselPagerAdapter.setOnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
//                            Toast.makeText(mActivity, "position = " + position, Toast.LENGTH_SHORT).show();
                        }
                    });
                    final myHandler handler = new myHandler(mActivity, viewPager);
                    handler.sendEmptyMessageDelayed(HOME_AD_LOOP, 3000);
                    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                            if (handler.hasMessages(HOME_AD_LOOP)) {
                                handler.removeMessages(HOME_AD_LOOP);
                            }
                            handler.sendEmptyMessageDelayed(HOME_AD_LOOP, 3000);
                        }

                        @Override
                        public void onPageSelected(int position) {

                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });
                    List<ServicesBean> aceServiceList = new ArrayList<>();
                    List<ServicesBean> healthManagementList = new ArrayList<>();
                    try {
                        JSONArray aceServices = json.getJSONObject("data").getJSONArray("aceServices");
                        JSONArray healthManagement = json.getJSONObject("data").getJSONArray("healthManagement");
                        for (int i = 0; i < aceServices.length(); i++) {
                            aceServiceList.add(new ServicesBean(
                                    aceServices.getJSONObject(i).getInt("id"),
                                    aceServices.getJSONObject(i).getString("title"),
                                    aceServices.getJSONObject(i).getString("img"),
                                    aceServices.getJSONObject(i).getInt("detail")
                            ));
                        }
                        for (int i = 0; i < healthManagement.length(); i++) {
                            healthManagementList.add(new ServicesBean(
                                    healthManagement.getJSONObject(i).getInt("id"),
                                    healthManagement.getJSONObject(i).getString("title"),
                                    healthManagement.getJSONObject(i).getString("img"),
                                    healthManagement.getJSONObject(i).getInt("detail")
                            ));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    initRecyclerView(aceServiceView, aceServiceList, "皇牌服務");
                    initRecyclerView(healthManagementView, healthManagementList, "全方位健康管理");
                } else {
                    loadingAndRetryManager.showRetry();
                }
            }
        });

    }

    private void initView() {
        parentLayout = view.findViewById(R.id.parentLayout);
        viewPager = view.findViewById(R.id.viewPager);
        aceServiceView = view.findViewById(R.id.aceServiceView);
        healthManagementView = view.findViewById(R.id.healthManagementView);
    }

    private void initRecyclerView(RecyclerView recyclerView, final List<ServicesBean> dataList, String title) {
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        // 避免recycleriew与scrollview滚动冲突
        recyclerView.setNestedScrollingEnabled(false);

        SingleViewCommonAdapter<ServicesBean> adapter = new SingleViewCommonAdapter<ServicesBean>(mActivity, dataList, R.layout.item_services) {
            @Override
            protected void convert(Context context, CommonHolder holder, ServicesBean servicesBean) {
                holder.setText(R.id.service_title, servicesBean.getTitle());
                Picasso.get().load(servicesBean.getImg()).resize(LauUtil.dip2px(mActivity, 240), LauUtil.dip2px(mActivity, 240)).config(Bitmap.Config.RGB_565).into((ImageView) holder.getView(R.id.service_img));
            }
        };
        recyclerView.setAdapter(adapter);
        View header = LayoutInflater.from(mActivity).inflate(R.layout.item_services_header, recyclerView, false);
        ((TextView) header.findViewById(R.id.big_title)).setText(title);
        adapter.setHeaderView(header);
        adapter.setOnItemClickListener(new SingleViewCommonAdapter.setOnItemClickListener() {
            @Override
            public void onItemClick(int position) {
//                Toast.makeText(mActivity, "" + position, Toast.LENGTH_SHORT).show();
                onHomeFragmentListener ohfl = (onHomeFragmentListener) mActivity;
                if (ohfl != null) {
                    ohfl.toDetail(dataList.get(position).getDetailID());
                }
            }
        });
    }

    public interface onHomeFragmentListener {
        void toDetail(int id);
    }

}
