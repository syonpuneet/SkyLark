package in.vipplaza;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
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

import in.vipplaza.adapter.PaymentAdapter;
import in.vipplaza.info.InfoPayments;
import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;

/**
 * Created by manish on 21-07-2015.
 */

public class PaymentOptions extends AppCompatActivity {

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

    ArrayList<InfoPayments> arr_list;
    PaymentAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.shipping_method);

        mContext = PaymentOptions.this;

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


        listView=(ListView)findViewById(R.id.listView);

        arr_list=new ArrayList<>();

        getSupportActionBar().setTitle(getString(R.string.activity_payment_methods));

        if(cd.isConnectingToInternet())
        {
            progressBar.setVisibility(View.VISIBLE);
            PaymentOptionListing();

        }

        else
        {
            String error=getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }

    }


    private void PaymentOptionListing() {
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
                String serverUrl = Constant.MAIN_URL + "paymentMethods";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("", "");




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


                        JSONArray arrJson = objJson.getJSONArray("data");
                        InfoPayments info;

                        for (int i = 0; i < arrJson.length(); i++) {
                            info = new InfoPayments();
                            JSONObject obj = arrJson.getJSONObject(i);
                            info.id = obj.optString("id");
                            info.title = obj.getString("title");




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


    private  void setAdapter()
    {
        adapter=new PaymentAdapter(mContext,R.layout.single_textview_cell,arr_list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


}
