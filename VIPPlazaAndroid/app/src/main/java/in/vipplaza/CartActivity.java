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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import in.vipplaza.adapter.CartAdapter;
import in.vipplaza.info.InfoCart;
import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;

/**
 * Created by manish on 10-07-2015.
 */
public class CartActivity extends AppCompatActivity {


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

    ArrayList<InfoCart> arr_list;
    CartAdapter adapter;
    View footer_view;

    TextView cart_total;
    String cartTotal="";
    Button btn_checkout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cart_activity);

        mContext = CartActivity.this;

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


        noData=(TextView)findViewById(R.id.noData);

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



        getSupportActionBar().setTitle(R.string.activity_shopping_bag);
        listView = (ListView) findViewById(R.id.listView);

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footer_view = inflater.inflate(R.layout.cart_footer, null);

        cart_total=(TextView)footer_view.findViewById(R.id.cart_total);
        btn_checkout=(Button)footer_view.findViewById(R.id.btn_checkout);

        arr_list=new ArrayList<>();

        if (cd.isConnectingToInternet()) {
            GetCartInformation();
        } else {
            String error = getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }


        btn_checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(arr_list.size()>0)
                {
                    Constant.sendIntent(mContext,SelectCheckoutAddress.class);
                }


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


        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();


        if (mPref.getBoolean(Constant.isLogin, false)) {


            if (cd.isConnectingToInternet()) {
                updateCartCounter();
            }
        } else {
            String error = getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }


    }

    private void GetCartInformation() {
        new AsyncTask<Void, Integer, String>() {

            int status;
            String msg = "";

            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.show();

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
                String serverUrl = Constant.MAIN_URL + "cartDetail";
                Map<String, String> parameter = new HashMap<String, String>();
               // parameter.put("entity_id", mPref.getString(Constant.entity_id, ""));
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

                    JSONArray data=objJson.getJSONArray("data");

                    InfoCart info;
                    arr_list.clear();

                    for(int i=0;i<data.length();i++)
                    {
                        JSONObject obj=data.getJSONObject(i);

                        info=new InfoCart();
                        info.id=obj.getString("id");
                        info.name=obj.getString("name");
                        info.sku=obj.getString("sku");
                        info.ssku=obj.getString("ssku");
                        info.img=obj.getString("img");
                        info.qty=obj.getString("qty");
                        info.price=obj.getString("price");
                        info.totalqty=obj.getString("totalqty");
                        info.entity_id=obj.getString("entity_id");
                        info.mainprice=obj.getString("mainprice");

                        info.price_incl_tax=obj.getString("price_incl_tax");


                        String size=obj.optString("size");
                                info.size=size;

                        arr_list.add(info);

                    }


                    cartTotal=objJson.getString("subtotal");

                    String price_tag = getString(R.string.txt_price_tag);

                    cart_total.setText(price_tag+" "+cartTotal);

                    setAdapter();

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }


    private void setAdapter()
    {


        if(arr_list.size()==0)
        {
            listView.setVisibility(View.GONE);
            noData.setVisibility(View.VISIBLE);
        }

        else {

            if(listView.getFooterViewsCount()==0)
            {
                listView.addFooterView(footer_view);
            }

            listView.setVisibility(View.VISIBLE);
            noData.setVisibility(View.GONE);
            adapter=new CartAdapter(mContext, R.layout.cart_product_cell, arr_list);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
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


                try {
                    JSONObject objJson = new JSONObject(result);

                    status = Integer.parseInt(objJson.getString("status"));
                    msg = objJson.getString("msg");

                    if (status == 1) {
                        int count = Integer.parseInt(objJson.getString("count"));

                        String entity_id=objJson.getString("entity_id");

                        mPref.edit().putString(Constant.entity_id,entity_id).commit();
                        if (mPref.getBoolean(Constant.isLogin, false)) {
                            if (count == 0) {
                                mCartCounter.setVisibility(View.GONE);
                            } else {
                                mCartCounter.setVisibility(View.VISIBLE);
                                mCartCounter.setText(objJson.getString("count"));
                            }
                        } else {
                            mCartCounter.setVisibility(View.GONE);

                        }


                    } else {
                       // Constant.showAlertDialog(mContext, msg, false);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }

    public void removeProduct(final String product_id, final String entity_id) {
        new AsyncTask<Void, Integer, String>() {

            int status;
            String msg = "";

            protected void onPreExecute() {
                super.onPreExecute();

                progressDialog.show();
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
                String serverUrl = Constant.MAIN_URL + "removeCartProduct";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("pid", product_id);
                parameter.put("entity_id", entity_id);


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

                    status = Integer.parseInt(objJson.getString("status"));
                    msg = objJson.getString("msg");

                    if (status == 1) {

                        GetCartInformation();
                        updateCartCounter();


                    } else {
                        Constant.showAlertDialog(mContext, msg, false);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }


    public void UpdateCart(final String product_id, final String qty, final String entity_id) {
        new AsyncTask<Void, Integer, String>() {

            int status;
            String msg = "";

            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.show();

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
                String serverUrl = Constant.MAIN_URL + "updateCart";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("pid", product_id);
                parameter.put("qty", qty);
                parameter.put("uid", mPref.getString(Constant.user_id,""));
                parameter.put("entity_id",entity_id);



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

                    status=Integer.parseInt(objJson.getString("status"));
                    msg=objJson.getString("msg");

                    if(status==1)
                    {

                        GetCartInformation();
                        updateCartCounter();
                    }

                    else
                    {
                        Constant.showAlertDialog(mContext,msg,false);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }

}
