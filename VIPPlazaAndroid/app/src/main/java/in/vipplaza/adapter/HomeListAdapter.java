package in.vipplaza.adapter;

/**
 * Created by manish on 02-07-2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Calendar;

import in.vipplaza.R;
import in.vipplaza.info.InfoHomeEvents;
import in.vipplaza.utills.SquareImageView;


public class HomeListAdapter extends ArrayAdapter<InfoHomeEvents> {

    SharedPreferences mPref;
    private Context activity;
    private ArrayList<InfoHomeEvents> info;
    private LayoutInflater inflater = null;

    DisplayImageOptions options;

    public HomeListAdapter(Context act, int resource,
                                 ArrayList<InfoHomeEvents> arrayList) {
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

            convertView = inflater.inflate(R.layout.home_deal_cell, parent,
                    false);

            holder = new Holder();

            holder.name = (TextView) convertView
                    .findViewById(R.id.name);
            holder.discount = (TextView) convertView.findViewById(R.id.discount);
            holder.time_stamp = (TextView) convertView
                    .findViewById(R.id.time_stamp);
            holder.days = (TextView) convertView
                    .findViewById(R.id.days);

            holder.image = (SquareImageView) convertView
                    .findViewById(R.id.image);
            holder.imageView=(ImageView)convertView.findViewById(R.id.imageView);

            holder.overlay=(View)convertView. findViewById(R.id.overlay);


            convertView.setTag(holder);

        } else {

            holder = (Holder) convertView.getTag();

        }

        mPref = activity.getSharedPreferences(activity.getResources()
                .getString(R.string.app_name), Activity.MODE_PRIVATE);

        holder.name.setText(info.get(position).disc_info);


        holder.discount.setText(info.get(position).disc_amount);


        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());


        long current_time=cal.getTimeInMillis();

        long end_time=Long.parseLong(info.get(position).event_end)*1000;

        long mills=end_time-current_time;

//        int days = (int) (mills / (1000*60*60*24));
//
//
//
//        int Hours = (int) (mills/(1000 * 60 * 60));
//        int Mins = (int) (mills/(1000*60)) % 60;
//        long Secs = (int) (mills / 1000) % 60;


        int opacity = 100; // from 0 to 255
       holder. overlay.setBackgroundColor(opacity * 0x1000000); // black with a variable alpha
        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        holder.overlay.setLayoutParams(params);
        holder.overlay.invalidate();


        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = mills / daysInMilli;
        mills = mills % daysInMilli;

        long elapsedHours = mills / hoursInMilli;
        mills = mills % hoursInMilli;

        long elapsedMinutes = mills / minutesInMilli;
        mills = mills % minutesInMilli;

        long elapsedSeconds = mills / secondsInMilli;



        String diff = elapsedHours + " : " + elapsedMinutes + " : " + elapsedSeconds;

        holder.time_stamp.setText(diff);
        holder.days.setText(elapsedDays + " \nHARI");


       ImageLoader.getInstance().displayImage(info.get(position).event_image,
               holder.image, options);



        if(info.get(position).event_logo.equals(""))
        {
            holder.imageView.setVisibility(View.GONE);
        }
        else
        {
            holder.imageView.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(info.get(position).event_logo,
                    holder.imageView, options);

        }


        return convertView;
    }

    private static class Holder {

        TextView name, discount, time_stamp,days;
        SquareImageView image;
        ImageView imageView;
        View overlay;


    }

}
