package in.vipplaza.adapter;

/**
 * Created by manish on 02-07-2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import in.vipplaza.R;
import in.vipplaza.info.InfoProductListing;
import in.vipplaza.utills.SquareImageView;


public class ProductListingAdapter extends ArrayAdapter<InfoProductListing> {

    SharedPreferences mPref;
    private Context activity;
    private ArrayList<InfoProductListing> info;
    private LayoutInflater inflater = null;

    DisplayImageOptions options;

    public ProductListingAdapter(Context act, int resource,
                               ArrayList<InfoProductListing> arrayList) {
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

            convertView = inflater.inflate(R.layout.product_listing_cell, parent,
                    false);

            holder = new Holder();

            holder.product_name = (TextView) convertView
                    .findViewById(R.id.product_name);
            holder.price = (TextView) convertView.findViewById(R.id.price);
            holder.speprice = (TextView) convertView
                    .findViewById(R.id.speprice);
            holder.image = (SquareImageView) convertView
                    .findViewById(R.id.product_image);

            holder.out_stock = (LinearLayout) convertView
                    .findViewById(R.id.out_stock);
          //  holder.overlay=(View)convertView. findViewById(R.id.overlay);

            convertView.setTag(holder);

        } else {

            holder = (Holder) convertView.getTag();

        }

        mPref = activity.getSharedPreferences(activity.getResources()
                .getString(R.string.app_name), Activity.MODE_PRIVATE);

        holder.product_name.setText(info.get(position).name);

        String price_tag = activity.getString(R.string.txt_price_tag);
        holder.speprice.setText(price_tag+" "+info.get(position).speprice  );


        holder.price.setText(price_tag + " " + info.get(position).price);
        holder.price.setPaintFlags(holder.price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        ImageLoader.getInstance().displayImage(info.get(position).img,
                holder.image, options);


        if(info.get(position).qty.equals("0"))
        {
            holder.out_stock.setVisibility(View.VISIBLE);

//            int opacity = 100; // from 0 to 255
//            holder. overlay.setBackgroundColor(opacity * 0x1000000); // black with a variable alpha
//            FrameLayout.LayoutParams params =
//                    new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
//            holder.overlay.setLayoutParams(params);
//            holder.overlay.invalidate();

        }
        else
        {
            holder.out_stock.setVisibility(View.GONE);
          //  holder.overlay.setVisibility(View.GONE);
        }



        return convertView;
    }

    private static class Holder {

        TextView product_name, speprice, price;
        SquareImageView image;

      //  View overlay;
        LinearLayout out_stock;

    }

}
