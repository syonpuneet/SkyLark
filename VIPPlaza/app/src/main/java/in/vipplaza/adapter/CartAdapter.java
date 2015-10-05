package in.vipplaza.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import in.vipplaza.CartActivity;
import in.vipplaza.R;
import in.vipplaza.info.InfoCart;
import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;
import in.vipplaza.utills.SquareImageView;

/**
 * Created by manish on 15-07-2015.
 */


public class CartAdapter extends ArrayAdapter<InfoCart> {

    SharedPreferences mPref;
    private Context activity;
    private ArrayList<InfoCart> info;
    private LayoutInflater inflater = null;

    DisplayImageOptions options;

    ConnectionDetector cd;

    public CartAdapter(Context act, int resource,
                                 ArrayList<InfoCart> arrayList) {
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

        cd=new ConnectionDetector(activity);

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

            convertView = inflater.inflate(R.layout.cart_product_cell, parent,
                    false);

            holder = new Holder();


            holder.product_name = (TextView) convertView
                    .findViewById(R.id.product_name);
            holder.sku = (TextView) convertView.findViewById(R.id.sku);
            holder.message = (TextView) convertView.findViewById(R.id.message);
            holder.ssku = (TextView) convertView
                    .findViewById(R.id.ssku);
            holder.size = (EditText) convertView
                    .findViewById(R.id.size);
            holder.price = (TextView) convertView
                    .findViewById(R.id.price);
            holder.old_price = (TextView) convertView
                    .findViewById(R.id.old_price);
            holder.product_image = (SquareImageView) convertView
                    .findViewById(R.id.product_image);

            holder.remove_product=(LinearLayout)convertView.findViewById(R.id.remove_product);

            holder.qty=(EditText)convertView.findViewById(R.id.qty);
            convertView.setTag(holder);

        } else {

            holder = (Holder) convertView.getTag();

        }

        mPref = activity.getSharedPreferences(activity.getResources()
                .getString(R.string.app_name), Activity.MODE_PRIVATE);

        holder.product_name.setText(info.get(position).name);

        String price_tag = activity.getString(R.string.txt_price_tag);
        String sku_tag = activity.getString(R.string.txt_sku_tag);
        String ssku_tag = activity.getString(R.string.txt_ssku_tag);
        //String size_tag = activity.getString(R.string.txt_size_tag);




        holder.price.setText(price_tag + " " + info.get(position).price);
        holder.old_price.setText(price_tag + " " + info.get(position).mainprice);
        holder.old_price.setPaintFlags(holder.price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.sku.setText(sku_tag + " " + info.get(position).sku);
        holder.ssku.setText(ssku_tag + " " + info.get(position).ssku);


        if(info.get(position).size==null||info.get(position).size.equals("")) {


            holder.size.setText(R.string.txt_tag_free);
        }

        else
        {

            holder.size.setText( info.get(position).size);
        }
       holder.qty.setText(info.get(position).submitqty);


        if(info.get(position).isAvaialbale)
        {
            holder.message.setVisibility(View.GONE);
        }
        else
        {
            holder.message.setVisibility(View.VISIBLE);
        }


        ImageLoader.getInstance().displayImage(info.get(position).img,
                holder.product_image, options);


        holder.qty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(cd.isConnectingToInternet()) {

                    final CharSequence[] items = activity.getResources().getStringArray(R.array.product_qty);

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(R.string.txt_number);
                    builder.setSingleChoiceItems(items, info.get(position).cart_pos,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int pos) {
                                    // TODO Auto-generated method stub


                                    int qty = Integer.parseInt(items[pos].toString());

                                    dialog.dismiss();
                                    if (qty > Integer.parseInt(info.get(position).totalqty)) {

                                        String error = activity.getString(R.string.txt_not_available);
                                        Constant.showAlertDialog(activity, error, false);

                                    } else if (qty > 2) {
                                        String error = activity.getString(R.string.txt_max_allowed);
                                        Constant.showAlertDialog(activity, error, false);

                                    } else {

                                        if (info.get(position).cart_pos != pos) {
                                            holder.qty.setText(items[pos]);

                                            String click_val = "";
                                            if (info.get(position).cart_pos < pos) {
                                                click_val = "plus";
                                            } else {
                                                click_val = "minus";
                                            }

                                            if (activity instanceof CartActivity) {


                                                String size="";
                                                String sku=info.get(position).sku;
                                                if(info.get(position).size==null||info.get(position).size.equals("")) {

                                                    size="";

                                                }
                                                else
                                                {
                                                    size=info.get(position).size;
                                                }

                                               size= size.trim();

                                                ((CartActivity) activity).UpdateCart(info.get(position).id, "" + qty, info.get(position).entity_id, click_val, sku, size);
                                            }
                                        }

                                    }

                                }
                            });

                    builder.show();
                }

                else
                {
                    String connection_alert = activity.getResources().getString(
                            R.string.alert_no_internet);
                    Constant.showAlertDialog(activity, connection_alert, false);
                }
            }
        });

        holder.remove_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(cd.isConnectingToInternet()) {

                    if (activity instanceof CartActivity) {
                        ((CartActivity) activity).removeProduct(info.get(position).sku, info.get(position).entity_id);
                    }
                }

                else
                {
                    String connection_alert = activity.getResources().getString(
                            R.string.alert_no_internet);
                    Constant.showAlertDialog(activity, connection_alert, false);
                }
            }
        });


        return convertView;
    }

    private static class Holder {

        TextView product_name, sku, ssku,price,old_price,message;
        SquareImageView product_image;
        EditText qty;
        EditText size;

        LinearLayout remove_product;

    }

}
