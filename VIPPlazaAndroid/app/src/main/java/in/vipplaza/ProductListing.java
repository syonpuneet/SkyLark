package in.vipplaza;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import in.vipplaza.adapter.ProductListingAdapter;
import in.vipplaza.info.InfoProductListing;
import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;

/**
 * Created by manish on 29-06-2015.
 */
public class ProductListing extends AppCompatActivity implements View.OnClickListener {

    //activity components
    ProgressDialog progressDialog;
    ConnectionDetector cd;
    Toolbar toolbar;
    Context mContext;
    SharedPreferences mPref;

    ImageView activity_title;
    TextView noData;
    SwipeRefreshLayout swipe_container;
    GridView gridView;
    ProgressBar progressBar;

    ArrayList<InfoProductListing> arr_list;
    String cat_id;
    ProductListingAdapter adapter;
    TextView time_stamp, days;
    String event_end = "";
    LinearLayout sort_layout, filter_layout;
    TextView sort_text, name_sort;
    int sort_by_selected_item = 0;
    String order_price = "ASC", order_name = "ASC";
    TextView total_product;

    boolean isPriceAssending = true;
    boolean isNameAssending = true;

    private MenuItem mCartItem;
    private TextView mCartCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.produt_listing);

        mContext = ProductListing.this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPref = getSharedPreferences(getResources()
                .getString(R.string.pref_name), Activity.MODE_PRIVATE);

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        activity_title = (ImageView) findViewById(R.id.activity_title);
        activity_title.setVisibility(View.GONE);


        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.status_bar_color));
        Constant.getOverflowMenu(mContext);

        cd = new ConnectionDetector(mContext);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        swipe_container = (SwipeRefreshLayout)
                findViewById(R.id.swipe_container);

        //swipe_container.setColorSchemeColors(colors)
        swipe_container.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        TypedValue typed_value = new TypedValue();
        getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        swipe_container.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        noData = (TextView) findViewById(R.id.noData);
        noData.setText(R.string.no_product);

        initalizevariable();
    }


    private void initalizevariable() {


        arr_list = new ArrayList<>();

        time_stamp = (TextView) findViewById(R.id.time_stamp);
        days = (TextView) findViewById(R.id.days);
        gridView = (GridView) findViewById(R.id.gridView);

        filter_layout = (LinearLayout) findViewById(R.id.filter_layout);
        sort_layout = (LinearLayout) findViewById(R.id.sort_layout);
        sort_text = (TextView) findViewById(R.id.sort_text);
        name_sort = (TextView) findViewById(R.id.name_sort);

        total_product = (TextView) findViewById(R.id.total_product);

        days.setText(Html.fromHtml("<font color='#CB9B39'>" + "0" + "</font>" + " <br>HARI</br>"));

        Bundle bundle = getIntent().getExtras();
        cat_id = bundle.getString("id");

        getSupportActionBar().setTitle(bundle.getString("name"));


        if (cd.isConnectingToInternet()) {
            progressBar.setVisibility(View.VISIBLE);
            total_product.setVisibility(View.GONE);
            ProdutListing();
        } else {
            String error = getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(mContext, ProductDetail.class);
                intent.putExtra("product_id", arr_list.get(position).id);
                intent.putExtra("name", arr_list.get(position).brand);
                // intent.putExtra("product_id","134222");
                startActivity(intent);
            }
        });


        swipe_container.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                // TODO Auto-generated method stub

                if (cd.isConnectingToInternet()) {
                    ProdutListing();
                } else {
                    swipe_container.setRefreshing(false);
                    String connection_alert = getResources().getString(
                            R.string.alert_no_internet);
                    Constant.showAlertDialog(mContext, connection_alert, false);
                }
                //
            }
        });


//        sort_layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final CharSequence[] items = getResources().getStringArray(
//                        R.array.sorting_arr);
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                builder.setTitle(R.string.txt_gender);
//                builder.setSingleChoiceItems(items, sort_by_selected_item,
//                        new DialogInterface.OnClickListener() {
//
//                            @Override
//                            public void onClick(DialogInterface dialog, int pos) {
//                                // TODO Auto-generated method stub
//
//
//                                sort_text.setText(items[pos]);
//                                dialog.dismiss();
//                                sort_by_selected_item=pos;
//
//                                switch (pos)
//                                {
//
//                                    case 0:
//                                        order_sort="";
//                                        order_by="";
//                                        break;
//                                    case 1:
//                                        order_sort="ASC";
//                                        order_by="price";
//                                        break;
//                                    case 2:
//                                        order_sort="DESC";
//                                        order_by="price";
//                                        break;
//                                    case 3:
//                                        order_sort="ASC";
//                                        order_by="name";
//                                        break;
//                                    case 4:
//                                        order_sort="DESC";
//                                        order_by="name";
//                                        break;
//
//
//                                    default:
//                                        break;
//                                }
//
//
//                                progressBar.setVisibility(View.VISIBLE);
//                                gridView.setVisibility(View.GONE);
//                                total_product.setVisibility(View.GONE);
//                                ProdutListing();
//
//                            }
//                        });
//
//                builder.show();
//            }
//        });


        sort_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PriceSorting();
                progressBar.setVisibility(View.VISIBLE);
                gridView.setVisibility(View.GONE);
                total_product.setVisibility(View.GONE);
                ProdutListing();
            }
        });


        filter_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NameSorting();
                progressBar.setVisibility(View.VISIBLE);
                gridView.setVisibility(View.GONE);
                total_product.setVisibility(View.GONE);
                ProdutListing();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.cart_menu, menu);

        mCartItem = menu.findItem(R.id.action_cart);
        View msg_action = mCartItem.getActionView();
        mCartCounter = (TextView) msg_action
                .findViewById(R.id.mCartCounter);
        msg_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mPref.getBoolean(Constant.isLogin,false)) {
                    Constant.sendIntent(mContext, CartActivity.class);
                }
            }
        });


        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();


        if(mPref.getBoolean(Constant.isLogin, false))
        {
            if(cd.isConnectingToInternet())
            {

                updateCartCounter();

            }

            else
            {
                String error=getString(R.string.alert_no_internet);
                Constant.showAlertDialog(mContext, error, false);
            }
        }



    }


    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch (view.getId()) {


            default:
                break;
        }

    }


    private void PriceSorting() {
        if (isPriceAssending) {
            sort_text.setText(R.string.txt_price_desending);
            isPriceAssending = false;
            order_price = "DESC";
        } else {
            sort_text.setText(R.string.txt_price_assending);
            isPriceAssending = true;
            order_price = "ASC";
        }
    }


    private void NameSorting() {
        if (isNameAssending) {
            name_sort.setText(R.string.txt_name_desending);
            isNameAssending = false;
            order_name = "DESC";
        } else {
            name_sort.setText(R.string.txt_name_assending);
            isNameAssending = true;
            order_name = "ASC";
        }
    }

    private void ProdutListing() {
        new AsyncTask<Void, Integer, String>() {

            int status;
            String msg = "";

            protected void onPreExecute() {

                super.onPreExecute();


            }


            @Override
            protected void onCancelled() {

                super.onCancelled();
                // server error , generate toast message.
                //progressDialog.dismiss();
                Toast.makeText(mContext,
                        getResources().getString(R.string.server_eroor),
                        Toast.LENGTH_LONG).show();

            }


            @Override
            protected String doInBackground(Void... params) {

                String result = "";
                String serverUrl = Constant.MAIN_URL + "catProducts";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("cat_id", cat_id);
                parameter.put("order_price", order_price);
                parameter.put("order_name", order_name);


                try {
                    result = Constant.post(serverUrl, parameter);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Log.i("", " result=====" + result);
                return result;
            }

            @Override
            protected void onPostExecute(String result) {

                super.onPostExecute(result);
                swipe_container.setRefreshing(false);
                progressBar.setVisibility(View.GONE);


                try {
                    JSONObject objJson = new JSONObject(result);

                    // JSONObject objJson = obj.getJSONObject("Data");
                    status = Integer.parseInt(objJson.getString("status")
                            .toString());

                    msg = objJson.getString("msg").toString();

                    //user_id


                    if (status == 1) {

                        arr_list.clear();

                        event_end = objJson.getString("event_end").toString();
                        JSONArray arrJson = objJson.getJSONArray("data");
                        InfoProductListing info;

                        total_product.setText(arrJson.length() + " " + getString(R.string.txt_product_tag));

                        for (int i = 0; i < arrJson.length(); i++) {
                            info = new InfoProductListing();
                            JSONObject obj = arrJson.getJSONObject(i);
                            info.id = obj.getString("id");
                            info.name = obj.getString("name");
                            info.short_description = obj.getString("short_description");
                            info.long_description = obj.getString("long_description");
                            info.price = obj.getString("price");
                            info.speprice = obj.getString("speprice");
                            info.img = obj.getString("img");
                            info.url = obj.getString("url");
                            info.brand=obj.getString("brand");
                            info.qty=obj.getString("qty");
                            info.minqty=obj.getString("minqty");
                            arr_list.add(info);


                        }


                        setAdapter();

                    } else {
                        // error, print message.
                        Constant.showAlertDialog(mContext, msg, false);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }

    private void setAdapter() {


        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());


        long current_time = cal.getTimeInMillis();

        long end_time = Long.parseLong(event_end) * 1000;

        long mills = end_time - current_time;

        if (mills > 0) {
            callhandler();
        } else {
            String diff = 00 + " : " + 00 + " : " + 00;

            time_stamp.setText(diff);
           // days.setText(Html.fromHtml("<font color='#CB9B39'>"+0 + " \nHARI"));
            days.setText(Html.fromHtml("<font color='#CB9B39'>" + "0" + "</font>" + " <br>HARI</br>"));
        }

        checkNoData();
        adapter = new ProductListingAdapter(mContext, R.layout.product_listing_cell, arr_list);
        gridView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void checkNoData() {
        if (arr_list.size() == 0) {
            total_product.setVisibility(View.GONE);
            noData.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
        } else {
            total_product.setVisibility(View.VISIBLE);
            noData.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        }
    }

    private void callhandler() {
//        final Handler handler = new Handler();
//        handler.postDelayed( new Runnable() {
//
//            @Override
//            public void run() {
//                adapter.notifyDataSetChanged();
//                handler.postDelayed( this, 60 * 1000 );
//            }
//        }, 60 * 1000 );


        Timer updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            public void run() {
//                try {
//                    long mills = Date1.getTime() - Date2.getTime();
//                    int Hours = millis / (1000 * 60 * 60);
//                    int Mins = millis % (1000 * 60 * 60);
//
//                    String diff = Hours + ":" + Mins; // updated value every1 second
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                ProductListing.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // This code will always run on the UI thread, therefore is safe to modify UI elements.
                        //  adapter.notifyDataSetChanged();
                        setDateData();
                    }
                });


            }

        }, 0, 1000);

    }

    private void setDateData() {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());


        long current_time = cal.getTimeInMillis();

        long end_time = Long.parseLong(event_end) * 1000;

        long mills = end_time - current_time;

//        int days = (int) (mills / (1000*60*60*24));
//
//
//
//        int Hours = (int) (mills/(1000 * 60 * 60));
//        int Mins = (int) (mills/(1000*60)) % 60;
//        long Secs = (int) (mills / 1000) % 60;


        if (mills > 0) {


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

            System.out.printf(
                    "%d days, %d hours, %d minutes, %d seconds%n",
                    elapsedDays,
                    elapsedHours, elapsedMinutes, elapsedSeconds);

            String diff = elapsedHours + " : " + elapsedMinutes + " : " + elapsedSeconds;

            time_stamp.setText(diff);


            days.setText(Html.fromHtml("<font color='#CB9B39'>" + elapsedDays + "</font>" + " <br>HARI</br>"));
        } else {
            String diff = "00 " + " : " + "00" + " : " + "00";

            time_stamp.setText(diff);
            days.setText(Html.fromHtml("<font color='#CB9B39'>" + "0" + "</font>" + " <br>HARI</br>"));
        }

    }

    private void updateCartCounter() {
        new AsyncTask<Void, Integer, String>() {

            int status;
            String msg = "";

            protected void onPreExecute() {
                super.onPreExecute();

            }


            @Override
            protected void onCancelled() {

                super.onCancelled();
                // server error , generate toast message.
                //progressDialog.dismiss();
                Toast.makeText(mContext,
                        getResources().getString(R.string.server_eroor),
                        Toast.LENGTH_LONG).show();

            }


            @Override
            protected String doInBackground(Void... params) {

                String result = "";
                String serverUrl = Constant.MAIN_URL + "cartCount";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("uid", mPref.getString(Constant.user_id,""));



                try {
                    result = Constant.post(serverUrl, parameter);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Log.i("", " result=====" + result);
                return result;
            }

            @Override
            protected void onPostExecute(String result) {

                super.onPostExecute(result);




                try {
                    JSONObject objJson = new JSONObject(result);

                    status=Integer.parseInt(objJson.getString("status"));
                    msg=objJson.getString("msg");

                    if(status==1)
                    {
                        int count=Integer.parseInt(objJson.getString("count"));

                        String entity_id=objJson.getString("entity_id");

                        mPref.edit().putString(Constant.entity_id,entity_id).commit();
                        if(mPref.getBoolean(Constant.isLogin,false))
                        {
                            if(count==0)
                            {
                                mCartCounter.setVisibility(View.GONE);
                            }
                            else
                            {
                                mCartCounter.setVisibility(View.VISIBLE);
                                mCartCounter.setText(objJson.getString("count"));
                            }
                        }
                        else
                        {
                            mCartCounter.setVisibility(View.GONE);

                        }




                    }

                    else
                    {
                        //Constant.showAlertDialog(mContext,msg,false);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }


}
