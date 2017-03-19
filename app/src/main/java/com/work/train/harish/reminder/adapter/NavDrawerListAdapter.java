package com.work.train.harish.reminder.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.work.train.harish.reminder.R;
import com.work.train.harish.reminder.adapter.BaseAdapter;
import com.work.train.harish.reminder.util.NavDrawerItem;

import java.util.ArrayList;



public class NavDrawerListAdapter extends BaseAdapter {

    Context context;
    ArrayList<NavDrawerItem> navDrawerItems;
    int selPosition = 0;

    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }

    public void setSelPosition(int position){
        selPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return navDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
        TextView txtCount = (TextView) convertView.findViewById(R.id.counter);

        int iconId = navDrawerItems.get(position).getIcon();
        if(iconId >= 0)
            imgIcon.setImageResource(iconId);
        else
            imgIcon.setVisibility(View.GONE);
        txtTitle.setText(navDrawerItems.get(position).getTitle());

        // displaying count
        // check whether it set visible or not
        if(navDrawerItems.get(position).getCounterVisibility()){
            txtCount.setText(navDrawerItems.get(position).getCount());
        }else{
            // hide the counter view
            txtCount.setVisibility(View.GONE);
        }

        ((TextView)convertView.findViewById(R.id.title)).setTextColor(position == selPosition ? context.getResources().getColor(R.color.white) : context.getResources().getColor(R.color.colorText));

        return convertView;
    }

}
