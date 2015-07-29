package in.vipplaza;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
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

import in.vipplaza.adapter.MyOrderdetailAdapter;
import in.vipplaza.info.InfoOrderDetails;
import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;

/**
 * Created by manish on 23-07-2015.
 */
public class MyOrdersDetails extends AppCompatActivity {


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
    View footer_view;
    View header_view;

    String order_id = "";
    ArrayList<InfoOrderDetails> arr_list;
    MyOrderdetailAdapter adapter;


    TextView shipping_address, shipping_method, billing_address, payment_methods;
    TextView txt_subtotal,txt_delivery,txt_total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.my_orders_details);

        mContext = MyOrdersDetails.this;


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
        arr_list = new ArrayList<>();

        order_id = getIntent().getExtras().getString("order_id");
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footer_view = inflater.inflate(R.layout.my_orders_details_footer, null);
        header_view = inflater.inflate(R.layout.my_order_detail_header, null);


        shipping_address = (TextView) header_view.findViewById(R.id.shipping_address);
        shipping_method = (TextView) header_view.findViewById(R.id.shipping_method);
        billing_address = (TextView) header_view.findViewById(R.id.billing_address);
        payment_methods = (TextView) header_view.findViewById(R.id.payment_methods);

       // TextView txt_subtotal,txt_delivery,txt_total;
        txt_subtotal = (TextView) footer_view.findViewById(R.id.txt_subtotal);
        txt_delivery = (TextView) footer_view.findViewById(R.id.txt_delivery);
        txt_total = (TextView) footer_view.findViewById(R.id.txt_total);



        if (cd.isConnectingToInternet()) {

            progressDialog.show();
            orderDetails();

        } else {
            String error = getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }
    }


    private void orderDetails() {
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
                String serverUrl = Constant.MAIN_URL + "orderHistoryDetail";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("order_id", order_id);


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

                        // TextView shipping_address,shipping_method,billing_address,payment_methods;

                        shipping_address.setText(Html.fromHtml(objJson.getString("shipping_address")));
                        shipping_method.setText(Html.fromHtml(objJson.getString("shipping_method")));
                        billing_address.setText(Html.fromHtml(objJson.getString("billing_address")));
                        payment_methods.setText(Html.fromHtml(objJson.getString("payment_method_code")));


                        // // TextView txt_subtotal,txt_delivery,txt_total;

                        String price_tag = getString(R.string.txt_price_tag);

                        txt_subtotal.setText(price_tag+" "+Html.fromHtml(objJson.getString("subtotal")));
                       // txt_delivery.setText(price_tag+" " + Html.fromHtml(objJson.getString("txt_delivery")));
                        txt_total.setText(price_tag+" "+Html.fromHtml(objJson.getString("totalprice")));



                        JSONArray arrJson = objJson.getJSONArray("data").getJSONArray(0);
                        InfoOrderDetails info;


                        for (int i = 0; i < arrJson.length(); i++) {
                            info = new InfoOrderDetails();
                            JSONObject obj = arrJson.getJSONObject(i);
                            info.product_id = obj.getString("product_id");
                            info.product_sku = obj.getString("product_sku");
                            info.product_price = obj.getString("product_price");
                            info.product_name = obj.getString("product_name");
                            info.qty = obj.getString("qty");
                            info.status = obj.getString("status");
                            info.category_name = obj.getString("category_name");
                            info.size = obj.getString("size");
                            info.subtot = obj.getString("subtot");
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

        listView.addHeaderView(header_view);
        listView.addFooterView(footer_view);

        adapter = new MyOrderdetailAdapter(mContext, R.layout.my_orders_detail_cell, arr_list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
