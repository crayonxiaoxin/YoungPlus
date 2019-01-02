package com.ormediagroup.youngplus.notuse;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.bean.MenuBean;
import com.ormediagroup.youngplus.fragment.BaseFragment;

import java.util.ArrayList;

/**
 * Created by Lau on 2018/12/27.
 */

public class TestFragment extends BaseFragment {
    private View view;
    private RecyclerView recyclerView;
    private ItemTouchHelper mItemTouchHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, null);
        initView();
        initData();
        return view;
    }

    private void initData() {
        ArrayList<MenuBean> list = new ArrayList<MenuBean>();
        for (int i = 1; i < 10; i++) {
            list.add(new MenuBean("title " + i, i));
        }
        TestAdapter adapter = new TestAdapter(mActivity, list, R.layout.item_test, new OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                mItemTouchHelper.startDrag(viewHolder);
            }
        });
        recyclerView.setAdapter(adapter);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

    }

    private void initView() {
        recyclerView = view.findViewById(R.id.homeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
    }
}
