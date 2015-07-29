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
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import in.vipplaza.adapter.HomeListAdapter;
import in.vipplaza.info.InfoHomeEvents;
import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;

/**
 * Created by manish on 08-07-2015.
 */


public class CategoryDealListing extends AppCompatActivity implements View.OnClickListener {

    //activity components
    ProgressDialog progressDialog;
    ConnectionDetector cd;
    Toolbar toolbar;
    Context mContext;
    SharedPreferences mPref;
    ImageView activity_title;

    TextView noData;
    SwipeRefreshLayout swipe_container;
    ListView listView;
    ProgressBar progressBar;

    ArrayList<InfoHomeEvents> arr_list;
    String cat_id;
    HomeListAdapter adapter;
    Timer updateTimer;
    String cat_name="";
    private MenuItem mCartItem;
    private TextView mCartCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.category_deal_listing);

        mContext = CategoryDealListing.this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPref = getSharedPreferences(getResources()
                .getString(R.string.pref_name), Activity.MODE_PRIVATE);

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        activity_title=(ImageView)findViewById(R.id.activity_title);
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


        cat_id=getIntent().getExtras().getString("id");

        cat_name=getIntent().getExtras().getString("name");

        getSupportActionBar().setTitle(cat_name);
        arr_list=new ArrayList<>();

        listView=(ListView)findViewById(R.id.listView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if(cd.isConnectingToInternet())
        {
            progressBar.setVisibility(View.VISIBLE);
            ProdutListing();
        }

        else
        {
            String error=getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }



        swipe_container.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                // TODO Auto-generated method stub

                if (cd.isConnectingToInternet()) {
                    ProdutListing();
                }

                else {
                    swipe_container.setRefreshing(false);
                    String connection_alert = getResources().getString(
                            R.string.alert_no_internet);
                    Constant.showAlertDialog(mContext, connection_alert, false);
                }
                //
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(mContext, ProductListing.class);
                intent.putExtra("id", arr_list.get(position).category_id);
                intent.putExtra("name", arr_list.get(position).event_name);
                mContext.startActivity(intent);
            }
        });

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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        Toast.makeText(mContext,""+id,Toast.LENGTH_SHORT).show();
        switch (id) {
            case R.id.action_cart:
                Constant.sendIntent(mContext,CartActivity.class);

                return  true;
            default:
                return super.onOptionsItemSelected(item);
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
                String serverUrl = Constant.MAIN_URL + "catEvents";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("cat_name", cat_name);



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
                progressBar.setVisibility(View.GONE);
                swipe_container.setRefreshing(false);


                try {
                    JSONObject objJson = new JSONObject(result);

                    // JSONObject objJson = obj.getJSONObject("Data");
                    status = Integer.parseInt(objJson.getString("status")
                            .toString());

                    msg = objJson.getString("msg").toString();

                    //user_id


                    if (status == 1) {

                        arr_list.clear();

                        JSONArray arrJson=objJson.getJSONArray("data");
                        InfoHomeEvents info;

                        for(int i=0;i<arrJson.length();i++)
                        {
                            info=new InfoHomeEvents();
                            JSONObject obj=arrJson.getJSONObject(i);
                            //info.event_id=obj.getString("event_id");
                            info.event_name=obj.getString("event_name");
                            info.event_logo=obj.getString("event_logo");
                            info.event_image=obj.getString("event_img");
                            info.disc_amount=obj.getString("event_disc");
                            info.category_id=obj.getString("category_id");
                            info.event_start=obj.getString("event_start");
                            info.event_end=obj.getString("event_end");
                           // info.last_generate=obj.getString("last_generate");
                           // info.event_menu=obj.getString("event_menu");
                            info.disc_info=obj.getString("discount_info");
                            info.event_promo=obj.getString("event_promo");
                           // info.event_category=obj.getString("event_category");



                            arr_list.add(info);


                        }

                        setAdapter();

                    } else {
                        // error, print message.
                        Constant.showAlertDialog(mContext,msg,false);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }

    private void setAdapter()
    {
        checkNoData();
        adapter=new HomeListAdapter(mContext, R.layout.product_listing_cell,arr_list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        callhandler();
    }

    private void checkNoData() {
        if (arr_list.size() == 0) {
            noData.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }

        else {
            noData.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }


    private void callhandler()
    {

        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            public void run() {


                CategoryDealListing.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // This code will always run on the UI thread, therefore is safe to modify UI elements.
                        adapter.notifyDataSetChanged();
                    }
                });


            }

        }, 0, 1000);

    }

    public void stoptimertask() {
        // stop the timer, if it's not already null
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stoptimertask();
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
                       // Constant.showAlertDialog(mContext,msg,false);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }


}
