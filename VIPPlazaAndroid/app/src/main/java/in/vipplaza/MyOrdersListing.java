package in.vipplaza;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.vipplaza.adapter.MyOrderAdapter;
import in.vipplaza.info.InfoMyOrder;
import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;

/**
 * Created by manish on 23-07-2015.
 */
public class MyOrdersListing extends AppCompatActivity {


    ProgressDialog progressDialog;
    ConnectionDetector cd;
    Toolbar toolbar;
    Context mContext;
    SharedPreferences mPref;
    ImageView activity_title;
    TextView noData;

    ListView listView;
    private MenuItem mCartItem;
    private TextView mCartCounter;

    ArrayList<InfoMyOrder> arr_list;
    MyOrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.my_order_listing);

        mContext = MyOrdersListing.this;


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


        noData = (TextView) findViewById(R.id.noData);

        noData.setText(R.string.no_product);
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


        initalizevariable();
    }

    private void initalizevariable() {
        listView = (ListView) findViewById(R.id.listView);
        arr_list=new ArrayList<>();

        getSupportActionBar().setTitle(R.string.activity_my_account);

        if (cd.isConnectingToInternet()) {

            progressDialog.show();
            orderListing();
        } else {
            String error = getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(mContext, MyOrdersDetails.class);
                intent.putExtra("order_id",arr_list.get(position).order_id);
                startActivity(intent);
            }
        });

    }

    private void orderListing() {
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
                String serverUrl = Constant.MAIN_URL + "getOrderHistory";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("uid", mPref.getString(Constant.user_id, ""));


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
                progressDialog.dismiss();


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
                        InfoMyOrder info;



                        for (int i = 0; i < arrJson.length(); i++) {
                            info = new InfoMyOrder();
                            JSONObject obj = arrJson.getJSONObject(i);
                            info.id = obj.getString("id");
                            info.name = obj.getString("name");
                            info.order_id = obj.getString("order_id");
                            info.date = obj.getString("date");
                            info.total = obj.getString("total");
                            info.status = obj.getString("status");

                            arr_list.add(info);


                        }


                        setAdapter();

                    } else {
                        // error, print message.

                        checkNoData();

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
        checkNoData();

        adapter = new MyOrderAdapter(mContext, R.layout.my_order_cell, arr_list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void checkNoData() {
        if (arr_list.size() == 0) {

            noData.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {

            noData.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }


}
