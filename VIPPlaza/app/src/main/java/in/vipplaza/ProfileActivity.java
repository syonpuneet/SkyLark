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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;

/**
 * Created by manish on 06-07-2015.
 */
public class ProfileActivity  extends AppCompatActivity implements View.OnClickListener{

    //activity components
    ProgressDialog progressDialog;
    ConnectionDetector cd;
    Toolbar toolbar;
    Context mContext;
    SharedPreferences mPref;

    LinearLayout btn_logout, btn_account,btn_orders;

    private MenuItem mCartItem;
    private TextView mCartCounter;
    private int cartCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.profile_fragment);

        mContext = ProfileActivity.this;

        //facebookDetails = new FacebookUserDetails((Activity) mContext);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPref = getSharedPreferences(getResources()
                .getString(R.string.pref_name), Activity.MODE_PRIVATE);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


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

        btn_logout = (LinearLayout)findViewById(R.id.btn_logout);
        btn_account = (LinearLayout)findViewById(R.id.btn_account);
        btn_orders = (LinearLayout)findViewById(R.id.btn_orders);


        btn_logout.setOnClickListener(this);
        btn_account.setOnClickListener(this);
        btn_orders.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.cart_menu, menu);

        mCartItem = menu.findItem(R.id.action_cart);
        View msg_action = mCartItem.getActionView();
        mCartCounter = (TextView) msg_action
                .findViewById(R.id.mCartCounter);

        if (!cd.isConnectingToInternet()){

            if(mPref.getBoolean(Constant.isLogin,false))
            {
                mCartCounter.setVisibility(View.VISIBLE);
                mCartCounter.setText("" + cartCount);
            }
            else
            {
                mCartCounter.setVisibility(View.GONE);
            }

        }


        msg_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mPref.getBoolean(Constant.isLogin,false)) {
                    Constant.sendIntent(mContext, CartActivity.class);
                }
                else
                {

                    Constant.showLoginAlertDialog(mContext, false);
                }
            }
        });

        return true;
    }


    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch (view.getId()) {

            case R.id.btn_account:
                selectItem(1);
                break;

            case R.id.btn_logout:

                mPref.edit().putBoolean(Constant.isLogin, false).commit();
                finish();

                break;

            case R.id.btn_orders:

                selectItem(2);
                break;

            default:
                break;
        }

    }


    @Override
    protected void onResume() {
        super.onResume();


        if(mPref.getBoolean(Constant.isLogin, false))
        {
            if (cd.isConnectingToInternet()) {

                updateCartCounter();

            } else {

                if(Constant.isFileAvailable("cartCount"))
                {
                    updateCartCounterFromSdCard();

                }
                else
                {
                    String error = getString(R.string.alert_no_internet);
                    Constant.showAlertDialog(mContext, error, false);
                }


            }
        }



    }

    private void selectItem(int position) {

        Intent intent = null;
        switch (position) {


            case 1:

                intent = new Intent(mContext, AccountActivity.class);
                break;

            case 2:
                intent = new Intent(mContext, MyOrdersListing.class);
                break;

            default:

                break;
        }


        startActivity(intent);


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

             //   String encrypted = Constant.encryptString(result);

              //  Constant.writtenToFile("cartCount", encrypted, false);

                return result;
            }

            @Override
            protected void onPostExecute(String result) {

                super.onPostExecute(result);




                setCartData(result);


            }
        }.execute(null, null, null);
    }


    private void updateCartCounterFromSdCard() {
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

                String encryptString = Constant.getStringFromFile("cartCount");

                String decryptString = Constant.DecryptString(encryptString);

                return decryptString;
            }

            @Override
            protected void onPostExecute(String result) {

                super.onPostExecute(result);
                setCartData(result);


            }
        }.execute(null, null, null);
    }

    private void setCartData(String result)
    {

        int status=0;
        String msg="";

        try {
            JSONObject objJson = new JSONObject(result);

            status = Integer.parseInt(objJson.getString("status"));
            msg = objJson.getString("msg");

            if (status == 1) {
                cartCount = Integer.parseInt(objJson.getString("count"));

                String entity_id=objJson.getString("entity_id");

                mPref.edit().putString(Constant.entity_id,entity_id).commit();
                if (mPref.getBoolean(Constant.isLogin, false)) {
                    if (cartCount == 0) {
                        mCartCounter.setVisibility(View.GONE);
                    } else {
                        if (cd.isConnectingToInternet()){
                            mCartCounter.setVisibility(View.VISIBLE);
                            mCartCounter.setText(objJson.getString("count"));
                        }
                    }
                } else {
                    mCartCounter.setVisibility(View.GONE);

                }



                if(!cd.isConnectingToInternet())
                {
                    invalidateOptionsMenu();
                }
            } else {
                //Constant.showAlertDialog(mContext, msg, false);
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }


    }



}

