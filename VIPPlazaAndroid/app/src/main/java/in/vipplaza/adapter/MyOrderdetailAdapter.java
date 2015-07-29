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
import in.vipplaza.info.InfoOrderDetails;

/**
 * Created by manish on 21-07-2015.
 */

public class MyOrderdetailAdapter extends ArrayAdapter<InfoOrderDetails> {

    SharedPreferences mPref;
    private Context activity;
    private ArrayList<InfoOrderDetails> info;
    private LayoutInflater inflater = null;

    DisplayImageOptions options;


    public MyOrderdetailAdapter(Context act, int resource,
                                ArrayList<InfoOrderDetails> arrayList) {
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

            convertView = inflater.inflate(R.layout.my_orders_detail_cell, parent,
                    false);

            holder = new Holder();
// TextView product_name,product_size,product_sku,product_price,product_qty,product_subtotal;

            holder.product_name = (TextView) convertView.findViewById(R.id.product_name);
            holder.product_size = (TextView) convertView.findViewById(R.id.product_size);
            holder.product_sku = (TextView) convertView.findViewById(R.id.product_sku);
            holder.product_price = (TextView) convertView.findViewById(R.id.product_price);
            holder.product_qty = (TextView) convertView.findViewById(R.id.product_qty);
            holder.product_subtotal = (TextView) convertView.findViewById(R.id.product_subtotal);


            convertView.setTag(holder);

        } else {

            holder = (Holder) convertView.getTag();

        }

        mPref = activity.getSharedPreferences(activity.getResources()
                .getString(R.string.app_name), Activity.MODE_PRIVATE);

//   TextView txt_order_no,txt_date,txt_buyer_name,txt_total_pensan,txt_status;



        String price_tag = activity.getString(R.string.txt_price_tag);
        String txt_sku_tag = activity.getString(R.string.txt_sku_tag);
        String txt_tag_qty = activity.getString(R.string.txt_tag_qty);
        String txt_size = activity.getString(R.string.txt_size);
        String txt_subtotal = activity.getString(R.string.txt_subtotal);



        holder.product_name.setText(info.get(position).product_name );
        holder.product_size.setText(txt_size+" "+info.get(position).size );
        holder.product_sku.setText(txt_sku_tag+" "+info.get(position).product_sku );

        holder.product_price.setText(price_tag+" "+info.get(position).product_price  );

        holder.product_qty.setText(txt_tag_qty+" "+info.get(position).qty );
        holder.product_subtotal.setText(txt_subtotal+"  "+price_tag+" "+info.get(position).subtot );



        return convertView;
    }

    private static class Holder {

        TextView product_name,product_size,product_sku,product_price,product_qty,product_subtotal;


    }
}