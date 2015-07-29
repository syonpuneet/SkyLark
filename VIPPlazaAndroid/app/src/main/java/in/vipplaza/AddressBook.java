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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

import in.vipplaza.adapter.AddressAdapter;
import in.vipplaza.info.InfoAddress;
import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;

/**
 * Created by manish on 20-07-2015.
 */
public class AddressBook  extends AppCompatActivity {

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

    ArrayList<InfoAddress> arr_list;

    AddressAdapter adapter;

    String result="";
    View footer_view;

    Button btn_add_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.address_book);

        mContext = AddressBook.this;

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
        getSupportActionBar().setTitle(getString(R.string.activity_address_book));

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footer_view = inflater.inflate(R.layout.add_address_footer, null);
        TextView or_text=(TextView)footer_view.findViewById(R.id.txt_or);
        or_text.setVisibility(View.GONE);

        btn_add_address=(Button) footer_view.findViewById(R.id.btn_add_address);

        btn_add_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constant.sendIntent(mContext, AddNewAddress.class);
            }
        });

        arr_list=new ArrayList<>();

    }


    @Override
    protected void onResume() {
        super.onResume();



        if(cd.isConnectingToInternet())
        {
            progressBar.setVisibility(View.VISIBLE);
            AddressListing();

        }

        else
        {
            String error=getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }


    }


    private void AddressListing() {
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

               // String result = "";
                String serverUrl = Constant.MAIN_URL + "checkAddress";
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
                        InfoAddress info;

                        for (int i = 0; i < arrJson.length(); i++) {
                            info = new InfoAddress();
                            JSONObject obj = arrJson.getJSONObject(i);
                            info.id = obj.optString("address_id");
                            info.firstname = obj.getString("firstname");
                            info.lastname = obj.getString("lastname");
                            info.street = obj.getString("street");
                            info.city = obj.getString("city");
                            info.region = obj.getString("region");
                            info.fax = obj.getString("fax");
                            info.postcode = obj.getString("postcode");
                            info.telephone = obj.getString("telephone");
                            info.address_type = obj.getString("address_type");

                            arr_list.add(info);


                        }


                        setAdapter();

                    } else {
                        // error, print message.
                       // Constant.showAlertDialog(mContext, msg, false);
                        setAdapter();
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


        if(arr_list.size()==2)
        {
            btn_add_address.setVisibility(View.GONE);
        }
        else
        {
            btn_add_address.setVisibility(View.VISIBLE);
        }


        if(listView.getFooterViewsCount()==0)
        {
            listView.addFooterView(footer_view);
        }


        adapter=new AddressAdapter(mContext, R.layout.address_book_cell,arr_list, result,false);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}
