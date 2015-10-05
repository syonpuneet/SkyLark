package in.vipplaza.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.ArrayList;

import in.vipplaza.R;
import in.vipplaza.info.InfoShipping;

/**
 * Created by manish on 21-07-2015.
 */

public class ShippingAdapter extends ArrayAdapter<InfoShipping> {

    SharedPreferences mPref;
    private Context activity;
    private ArrayList<InfoShipping> info;
    private LayoutInflater inflater = null;

    DisplayImageOptions options;


    public ShippingAdapter(Context act, int resource,
                           ArrayList<InfoShipping> arrayList) {
        super(act, resource, arrayList);
        this.activity = act;
        this.info = arrayList;


        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.dummy)
                .showImageForEmptyUri(R.drawable.dummy)
                .showImageOnFail(R.drawable.dummy).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();

    }

    @Override
    public int getCount() {

        return info.size();
    }

	/*
	 * @ Override public InfoRequest getItem ( int position ) {
	 *
	 * return info.get(position); }
	 */

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Holder holder;

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.single_textview_cell, parent,
                    false);

            holder = new Holder();


            holder.name = (TextView) convertView.findViewById(R.id.name);


            convertView.setTag(holder);

        } else {

            holder = (Holder) convertView.getTag();

        }

        mPref = activity.getSharedPreferences(activity.getResources()
                .getString(R.string.app_name), Activity.MODE_PRIVATE);


        holder.name.setText(info.get(position).method_title );


        return convertView;
    }

    private static class Holder {

        TextView name;


    }
}