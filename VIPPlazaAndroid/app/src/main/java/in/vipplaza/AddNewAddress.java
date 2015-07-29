package in.vipplaza;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.vipplaza.info.InfoCountry;
import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;

/**
 * Created by manish on 20-07-2015.
 */

/**
 * Created by manish on 10-07-2015.
 */
public class AddNewAddress extends AppCompatActivity {

    ProgressDialog progressDialog;
    ConnectionDetector cd;
    Toolbar toolbar;
    Context mContext;
    SharedPreferences mPref;
    ImageView activity_title;


    EditText et_first_name, et_last_name, et_wide_street1, et_wide, et_country;
    EditText et_region, et_city, et_postal_code, et_telephone, et_fax;


    Button btn_next;
    ArrayList<InfoCountry> arr_province;
    ArrayList<InfoCountry> arr_state;
    ArrayList<InfoCountry> arr_city;

    int countr_no=0;
    int state_no=0; int city_no=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.address_fill_up);

        mContext = AddNewAddress.this;

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


        initalizevariable();
    }

    private void initalizevariable() {

        //  EditText et_first_name,et_last_name,et_wide_street1,et_wide,et_country;
        //  EditText et_region,et_city,et_postal_code,et_telephone,et_fax;

        arr_province=new ArrayList<>();
        arr_city=new ArrayList<>();
        arr_state=new ArrayList<>();

        et_first_name = (EditText) findViewById(R.id.et_first_name);
        et_last_name = (EditText) findViewById(R.id.et_last_name);
        et_wide_street1 = (EditText) findViewById(R.id.et_wide_street1);
        et_wide = (EditText) findViewById(R.id.et_wide);
        et_country = (EditText) findViewById(R.id.et_country);
        et_region = (EditText) findViewById(R.id.et_region);
        et_city = (EditText) findViewById(R.id.et_city);
        et_postal_code = (EditText) findViewById(R.id.et_postal_code);
        et_telephone = (EditText) findViewById(R.id.et_telephone);
        et_fax = (EditText) findViewById(R.id.et_fax);

        btn_next=(Button)findViewById(R.id.btn_next);

        if(cd.isConnectingToInternet())
        {
            getProvince();
        }

        else {


            String error = getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }



        et_country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final CharSequence[] items = new String[arr_province.size()];

                for (int i = 0; i < arr_province.size(); i++) {
                    items[i] = arr_province.get(i).name;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.txt_country);
                builder.setSingleChoiceItems(items, countr_no,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int pos) {
                                // TODO Auto-generated method stub


                                dialog.dismiss();
                                countr_no = pos;
                                progressDialog.show();
                                getState(arr_province.get(countr_no).id);
                                et_country.setText(items[pos]);


                            }
                        });

                builder.show();

            }
        });


        et_region.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final CharSequence[] items=new String[arr_state.size()] ;

                for(int i=0;i<arr_state.size();i++)
                {
                    items[i]=arr_state.get(i).name;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.txt_region);
                builder.setSingleChoiceItems(items, state_no,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int pos) {
                                // TODO Auto-generated method stub


                                dialog.dismiss();
                                state_no=pos;
                                progressDialog.show();
                                getCity(arr_state.get(state_no).name);
                                et_region.setText(items[pos]);

                            }
                        });

                builder.show();

            }
        });


        et_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final CharSequence[] items = new String[arr_city.size()];

                for (int i = 0; i < arr_city.size(); i++) {
                    items[i] = arr_city.get(i).name;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.txt_city);
                builder.setSingleChoiceItems(items, city_no,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int pos) {
                                // TODO Auto-generated method stub


                                dialog.dismiss();
                                city_no = pos;

                                et_city.setText(items[pos]);

                            }
                        });

                builder.show();

            }
        });



        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                checkValidation();
            }
        });



    }


    private void  checkValidation()
    {
        if(cd.isConnectingToInternet())
        {
            if(et_first_name.getText().toString().isEmpty())
            {

                String error=getString(R.string.error_first_name);
                Constant.showAlertDialog(mContext,error,false);

            }
            else if(et_last_name.getText().toString().isEmpty())
            {
                String error=getString(R.string.error_last_name);
                Constant.showAlertDialog(mContext,error,false);
            }

            else if(et_wide_street1.getText().toString().isEmpty())
            {
                String error=getString(R.string.error_wide_street1);
                Constant.showAlertDialog(mContext,error,false);
            }

            else if(et_telephone.getText().toString().isEmpty())
            {
                String error=getString(R.string.error_phone_no);
                Constant.showAlertDialog(mContext,error,false);
            }

            else
            {
                SaveAddress();


            }

        }


        else {


            String error = getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }

    }




    public void SaveAddress() {
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
                String serverUrl = Constant.MAIN_URL + "addAddress";
                Map<String, String> parameter = new HashMap<String, String>();


                parameter.put("uid", mPref.getString(Constant.user_id, ""));
                parameter.put("fname", et_first_name.getText().toString());
                parameter.put("lname", et_last_name.getText().toString());
                parameter.put("address1", et_wide_street1.getText().toString());
                parameter.put("address2", et_wide.getText().toString());
                parameter.put("country_id", arr_province.get(countr_no).id);
                parameter.put("state", et_country.getText().toString());
                parameter.put("state_id", arr_state.get(state_no).id);
                parameter.put("city", et_city.getText().toString());
                parameter.put("zip", et_postal_code.getText().toString());
                parameter.put("mobile", et_telephone.getText().toString());
                parameter.put("othernum", et_fax.getText().toString());


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

                    status = Integer.parseInt(objJson.getString("status")
                            .toString());

                    msg = objJson.getString("msg").toString();

                    //user_id


                    if (status == 1) {


                        Constant.showAlertDialog(mContext,msg,false);
                    }


                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }


    public void getProvince() {
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
                String serverUrl = Constant.MAIN_URL + "getProvince";
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


                arr_province.clear();
                try {
                    JSONArray arrJson = new JSONArray(result);


                    InfoCountry info;
                    for (int i=0;i<arrJson.length();i++)
                    {
                        JSONObject obj=arrJson.getJSONObject(i);

                        info=new InfoCountry();
                        info.id=obj.getString("country_code");
                        info.name=obj.getString("country_name");

                        arr_province.add(info);

                    }


                    et_country.setText(arr_province.get(0).name);
                    getState(arr_province.get(0).id);


                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }


    public void getState(final String county_id) {
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
                String serverUrl = Constant.MAIN_URL + "getState";
                Map<String, String> parameter = new HashMap<String, String>();

                parameter.put("country_id", county_id);


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




                arr_state.clear();
                try {
                    JSONArray arrJson = new JSONArray(result);


                    InfoCountry info;
                    for (int i=0;i<arrJson.length();i++)
                    {
                        JSONObject obj=arrJson.getJSONObject(i);

                        info=new InfoCountry();
                         info.id=obj.getString("id");
                        info.name=obj.getString("name");

                        arr_state.add(info);

                    }

                    state_no=0;

                    et_region.setText(arr_state.get(0).name);
                    getCity(arr_state.get(0).name);


                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }


    public void getCity(final String region_name) {
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
                String serverUrl = Constant.MAIN_URL + "getCity";
                Map<String, String> parameter = new HashMap<String, String>();

                parameter.put("region_name", region_name);


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


                arr_city.clear();
                try {
                    JSONArray arrJson = new JSONArray(result);


                    InfoCountry info;
                    for (int i=0;i<arrJson.length();i++)
                    {
                        JSONObject obj=arrJson.getJSONObject(i);

                        info=new InfoCountry();
                        info.id=obj.getString("id");
                        info.name=obj.getString("city_name");

                        arr_city.add(info);

                    }



                    city_no=0;
                    et_city.setText(arr_city.get(0).name);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }
}
