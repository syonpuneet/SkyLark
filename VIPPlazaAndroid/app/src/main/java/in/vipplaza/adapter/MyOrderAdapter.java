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
import in.vipplaza.info.InfoMyOrder;

/**
 * Created by manish on 21-07-2015.
 */

public class MyOrderAdapter extends ArrayAdapter<InfoMyOrder> {

    SharedPreferences mPref;
    private Context activity;
    private ArrayList<InfoMyOrder> info;
    private LayoutInflater inflater = null;

    DisplayImageOptions options;


    public MyOrderAdapter(Context act, int resource,
                           ArrayList<InfoMyOrder> arrayList) {
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

            convertView = inflater.inflate(R.layout.my_order_cell, parent,
                    false);

            holder = new Holder();


            holder.txt_order_no = (TextView) convertView.findViewById(R.id.txt_order_no);
            holder.txt_date = (TextView) convertView.findViewById(R.id.txt_date);
            holder.txt_buyer_name = (TextView) convertView.findViewById(R.id.txt_buyer_name);
            holder.txt_total_pensan = (TextView) convertView.findViewById(R.id.txt_total_pensan);
            holder.txt_status = (TextView) convertView.findViewById(R.id.txt_status);


            convertView.setTag(holder);

        } else {

            holder = (Holder) convertView.getTag();

        }

        mPref = activity.getSharedPreferences(activity.getResources()
                .getString(R.string.app_name), Activity.MODE_PRIVATE);

//   TextView txt_order_no,txt_date,txt_buyer_name,txt_total_pensan,txt_status;
        holder.txt_order_no.setText(info.get(position).order_id );
        holder.txt_date.setText(info.get(position).date );
        holder.txt_buyer_name.setText(info.get(position).name );
        String price_tag = activity.getString(R.string.txt_price_tag);
        holder.txt_total_pensan.setText(price_tag+" "+info.get(position).total  );

        holder.txt_status.setText(info.get(position).status );



        return convertView;
    }

    private static class Holder {

        TextView txt_order_no,txt_date,txt_buyer_name,txt_total_pensan,txt_status;


    }
}