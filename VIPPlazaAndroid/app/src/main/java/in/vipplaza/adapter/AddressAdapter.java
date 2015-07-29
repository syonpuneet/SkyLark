package in.vipplaza.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.ArrayList;

import in.vipplaza.EditAddress;
import in.vipplaza.R;
import in.vipplaza.info.InfoAddress;

/**
 * Created by manish on 20-07-2015.
 */
public class AddressAdapter extends ArrayAdapter<InfoAddress> {

    SharedPreferences mPref;
    private Context activity;
    private ArrayList<InfoAddress> info;
    private LayoutInflater inflater = null;

    DisplayImageOptions options;
    String result;
    boolean isFromCheckout=false;

    public AddressAdapter(Context act, int resource,
                       ArrayList<InfoAddress> arrayList, String result, boolean isFromCheckout) {
        super(act, resource, arrayList);
        this.activity = act;
        this.info = arrayList;
        this.result=result;
        this.isFromCheckout=isFromCheckout;

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

            convertView = inflater.inflate(R.layout.address_book_cell, parent,
                    false);

            holder = new Holder();

            holder.address_type = (TextView) convertView
                    .findViewById(R.id.address_type);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.street = (TextView) convertView
                    .findViewById(R.id.street);
            holder.city = (TextView) convertView
                    .findViewById(R.id.city);
            holder.telehone = (TextView) convertView
                    .findViewById(R.id.telehone);
            holder.postal_code = (TextView) convertView
                    .findViewById(R.id.postal_code);

            holder.edit_btn = (LinearLayout) convertView
                    .findViewById(R.id.edit_btn);

            convertView.setTag(holder);

        } else {

            holder = (Holder) convertView.getTag();

        }

        mPref = activity.getSharedPreferences(activity.getResources()
                .getString(R.string.app_name), Activity.MODE_PRIVATE);




        holder.name.setText(info.get(position).firstname+" "+info.get(position).lastname);
        holder.street.setText(info.get(position).street);
        holder.city.setText(info.get(position).city);
        holder.telehone.setText(info.get(position).telephone);
        holder.postal_code.setText(info.get(position).postcode);


        if(isFromCheckout)
        {
            holder.edit_btn.setVisibility(View.GONE);
        }

        else
        {
            holder.edit_btn.setVisibility(View.VISIBLE);
        }
        if(info.get(position).address_type.equalsIgnoreCase("billing"))
        {
            holder.address_type.setText(R.string.txt_shipping_address_header);
        }
        else
        {
            holder.address_type.setText(R.string.txt_deliver_address_header);
        }



        holder.edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(activity, EditAddress.class);
                intent.putExtra("result", result);
                intent.putExtra("position",position);
                activity.startActivity(intent);
            }
        });

        return convertView;
    }

    private static class Holder {

        TextView address_type, name, street,city,telehone,postal_code;

        LinearLayout edit_btn;

    }

}