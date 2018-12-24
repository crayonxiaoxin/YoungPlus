package com.ormediagroup.youngplus.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.adapter.CarouselPagerAdapter;
import com.ormediagroup.youngplus.bean.BannerBean;
import com.ormediagroup.youngplus.bean.ServicesBean2;
import com.ormediagroup.youngplus.lau.CommonHolder;
import com.ormediagroup.youngplus.lau.LauUtil;
import com.ormediagroup.youngplus.loadAndRetry.LoadingAndRetryManager;
import com.ormediagroup.youngplus.lau.MultiViewCommonAdapter;
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
    private CarouselPagerAdapter bannerAdapter;
    private LoadingAndRetryManager loadingAndRetryManager;
    private LinearLayout parentLayout;
    private RecyclerView homeRecyclerView;
    private MultiViewCommonAdapter<ServicesBean2> serviceAdapter;

    private final static int HOME_AD_LOOP = 1;
    private String SERVICE_URL = "http://youngplus.com.hk/app-get-services";

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
                    bannerAdapter = new CarouselPagerAdapter(mActivity, list);
                    viewPager.setAdapter(bannerAdapter);
                    viewPager.setCurrentItem(list.size() * 10000, false);
                    bannerAdapter.setOnItemClickListener(new CarouselPagerAdapter.setOnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            Log.i(TAG, "onItemClick: homeBanner => " + position);
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
                    List<ServicesBean2> aceServiceList = new ArrayList<>();
                    aceServiceList.add(new ServicesBean2(1, -1, "皇牌服務", "", -1));
                    List<ServicesBean2> healthManagementList = new ArrayList<>();
                    healthManagementList.add(new ServicesBean2(1, -1, "全方位健康管理", "", -1));
                    try {
                        JSONArray aceServices = json.getJSONObject("data").getJSONArray("aceServices");
                        JSONArray healthManagement = json.getJSONObject("data").getJSONArray("healthManagement");
                        for (int i = 0; i < aceServices.length(); i++) {
                            JSONObject obj = aceServices.getJSONObject(i);
                            aceServiceList.add(new ServicesBean2(
                                    2,
                                    obj.getInt("id"),
                                    obj.getString("title"),
                                    obj.getString("img"),
                                    obj.getInt("detail")
                            ));
                        }
                        for (int i = 0; i < healthManagement.length(); i++) {
                            JSONObject obj = healthManagement.getJSONObject(i);
                            healthManagementList.add(new ServicesBean2(
                                    2,
                                    obj.getInt("id"),
                                    obj.getString("title"),
                                    obj.getString("img"),
                                    obj.getInt("detail")
                            ));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    aceServiceList.addAll(healthManagementList);
                    serviceAdapter = new MultiViewCommonAdapter<ServicesBean2>(mActivity, aceServiceList) {
                        @Override
                        public int setItemViewType(ServicesBean2 bean) {
                            return bean.getType();
                        }

                        @Override
                        public CommonHolder defineViewHolder(ViewGroup parent, int viewType) {
                            switch (viewType) {
                                case 1:
                                    return createHolder(parent, R.layout.item_services_header);
                                case 2:
                                    return createHolder(parent, R.layout.item_services);
                                default:
                                    return null;
                            }
                        }

                        @Override
                        public void convert(final Context context, CommonHolder holder, int position, final ServicesBean2 bean, int viewType) {
                            switch (viewType) {
                                case 1:
                                    holder.setText(R.id.big_title, bean.getTitle());
                                    break;
                                case 2:
                                    holder.setText(R.id.service_title, bean.getTitle())
                                            .setOnItemClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    onHomeFragmentListener ohfl = (onHomeFragmentListener) context;
                                                    if (ohfl != null) {
                                                        ohfl.toDetail(bean.getDetailID());
                                                    }
                                                }
                                            });
                                    Picasso.get().load(bean.getImg())
                                            .resize(LauUtil.dip2px(mActivity, 300), LauUtil.dip2px(mActivity, 300))
                                            .config(Bitmap.Config.RGB_565)
                                            .into((ImageView) holder.getView(R.id.service_img));
                                    break;
                                default:
                                    break;
                            }
                        }
                    };
                    serviceAdapter.setHeaderView(viewPager);
                    homeRecyclerView.setAdapter(serviceAdapter);
                } else {
                    loadingAndRetryManager.showRetry();
                }
            }
        });

    }

    private void initView() {
        parentLayout = view.findViewById(R.id.parentLayout);
        homeRecyclerView = view.findViewById(R.id.homeRecyclerView);
        viewPager = new ViewPager(mActivity);
        ViewPager.LayoutParams viewPagerParam = new ViewPager.LayoutParams();
        viewPagerParam.width = ViewPager.LayoutParams.MATCH_PARENT;
        viewPagerParam.height = 320;
        viewPager.setLayoutParams(viewPagerParam);
        homeRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
    }

    public interface onHomeFragmentListener {
        void toDetail(int id);
    }

}
