package com.ormediagroup.youngplus.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.ormediagroup.youngplus.R;
import com.ormediagroup.youngplus.bean.MenuBean;
import com.ormediagroup.youngplus.bean.ServicesBean;

import java.util.List;

/**
 * Created by Lau on 2018/11/28.
 */

public class SidebarExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<MenuBean> group;
    private List<List<ServicesBean>> child;

    public SidebarExpandableListAdapter(Context context, List<MenuBean> group, List<List<ServicesBean>> child) {
        this.context = context;
        this.group = group;
        this.child = child;
    }

    @Override
    public int getGroupCount() {
        return group.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return child.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return group.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return child.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_sidebar, null);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.menu_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(group.get(groupPosition).getTitle());
        holder.textView.setTextSize(22);
        holder.textView.setPadding(0, 30, 0, 30);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_sidebar, null);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.menu_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(child.get(groupPosition).get(childPosition).getTitle());
        holder.textView.setTextColor(Color.parseColor("#333333"));
        holder.textView.setTextSize(18);
        holder.textView.setPadding(0, 25, 0, 25);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    class ViewHolder {
        TextView textView;
    }
}
