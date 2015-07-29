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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
public class AccountActivity extends AppCompatActivity {


    ProgressDialog progressDialog;
    ConnectionDetector cd;
    Toolbar toolbar;
    Context mContext;
    SharedPreferences mPref;
    ImageView activity_title;
    ProgressBar progressBar;
    LinearLayout component_layout;
    TextView txt_first_name,txt_last_name,txt_email,txt_gender,txt_hello;
    Button btn_edit,btn_add_address;

    private MenuItem mCartItem;
    private TextView mCartCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.account_activity);

        mContext = AccountActivity.this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPref = getSharedPreferences(getResources()
                .getString(R.string.pref_name), Activity.MODE_PRIVATE);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        activity_title=(ImageView)findViewById(R.id.activity_title);
        activity_title.setVisibility(View.VISIBLE);


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

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        initalizevariable();
    }
    private void initalizevariable() {

        component_layout=(LinearLayout)findViewById(R.id.component_layout);
        component_layout.setVisibility(View.GONE);

        txt_first_name=(TextView)findViewById(R.id.txt_first_name);
        txt_last_name=(TextView)findViewById(R.id.txt_last_name);
        txt_email=(TextView)findViewById(R.id.txt_email);
        txt_gender=(TextView)findViewById(R.id.txt_gender);
        txt_hello=(TextView)findViewById(R.id.txt_hello);

        btn_edit=(Button)findViewById(R.id.btn_edit);

        btn_add_address=(Button)findViewById(R.id.btn_add_address);


        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Constant.sendIntent(mContext,EditProfile.class);
            }
        });


        btn_add_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constant.sendIntent(mContext,AddressBook.class);
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



        if(cd.isConnectingToInternet())
        {
            progressBar.setVisibility(View.VISIBLE);
            component_layout.setVisibility(View.GONE);
            GetProfileinformation();
            if(mPref.getBoolean(Constant.isLogin, false))
            {

                updateCartCounter();
            }
        }

        else
        {
            String error=getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }


    }

    private void GetProfileinformation() {
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
                String serverUrl = Constant.MAIN_URL + "getUserDetail";
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

                progressBar.setVisibility(View.GONE);
                component_layout.setVisibility(View.VISIBLE);


                try {
                    JSONObject objJson = new JSONObject(result);

                    // JSONObject objJson = obj.getJSONObject("Data");
                    status = Integer.parseInt(objJson.getString("status")
                            .toString());

                    msg = objJson.getString("msg").toString();

                    //user_id


                    if (status == 1) {
                        JSONObject obj = objJson.getJSONObject("data");

                        String first_name=obj.getString("firstname");
                        String last_name=obj.getString("lastname");

                        txt_first_name.setText(obj.getString("firstname"));
                        txt_last_name.setText(obj.getString("lastname"));
                        txt_email.setText(obj.getString("email"));

                        String gender=obj.getString("gender");


                        txt_hello.setText(getString(R.string.txt_hello)+" "+first_name+" "+last_name+"!" );

                        if(gender.equalsIgnoreCase("1"))
                        {
                                txt_gender.setText(R.string.txt_male);
                        }

                        else
                        {
                            txt_gender.setText(R.string.txt_female);
                        }


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


                        if(mPref.getBoolean(Constant.isLogin, false))
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
