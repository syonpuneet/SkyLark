package in.vipplaza.adapter;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import in.vipplaza.R;
import in.vipplaza.info.NavdrwableIems;

public class NavDrawerListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<NavdrwableIems> navDrawerItems;

    public NavDrawerListAdapter(Context context, ArrayList<NavdrwableIems> navDrawerItems) {
        this.context = context;
        this.navDrawerItems = navDrawerItems;
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
            convertView = mInflater.inflate(R.layout.drawer_list_item1, null);
        }


        TextView txtTitle = (TextView) convertView.findViewById(R.id.textView1);
        TextView txtCount = (TextView) convertView.findViewById(R.id.counter);


        txtTitle.setText(navDrawerItems.get(position).name);

        txtCount.setVisibility(View.GONE);

        return convertView;
    }

}
