package in.vipplaza.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import in.vipplaza.ProductListing;
import in.vipplaza.R;
import in.vipplaza.adapter.HomeListAdapter;
import in.vipplaza.info.InfoHomeEvents;
import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;

/**
 * Created by manish on 02-07-2015.
 */
public class HomeListFragment extends Fragment {

    ProgressDialog progressDialog;
    ConnectionDetector cd;
    Context mContext;
    SharedPreferences mPref;

    TextView noData;
    SwipeRefreshLayout swipe_container;
    ListView listView;
    ProgressBar progressBar;

    ArrayList<InfoHomeEvents> arr_list;
    String cat_id;
    HomeListAdapter adapter;
    View rootView;
    Timer updateTimer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflat and return the layout

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.home_list,
                    container, false);

            mContext = getActivity();
            cd = new ConnectionDetector(mContext);
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.setCancelable(false);

            mPref = mContext.getSharedPreferences(
                    getResources().getString(R.string.app_name),
                    Activity.MODE_PRIVATE);

            noData = (TextView) rootView.findViewById(R.id.noData);

            noData.setText(getString(R.string.no_product));
            swipe_container = (SwipeRefreshLayout) rootView
                    .findViewById(R.id.swipe_container);

            // swipe_container.setColorSchemeColors(colors)
            swipe_container.setColorSchemeResources(
                    android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);

            TypedValue typed_value = new TypedValue();
            mContext.getTheme().resolveAttribute(
                    android.support.v7.appcompat.R.attr.actionBarSize,
                    typed_value, true);
            swipe_container.setProgressViewOffset(false, 0, getResources()
                    .getDimensionPixelSize(typed_value.resourceId));

            // setHasOptionsMenu(true);
            initalizeItems(rootView);

        } else {

        }
        // Perform any camera updates here
        return rootView;
    }

    private void initalizeItems(View rootView) {

        arr_list=new ArrayList<>();

        listView=(ListView)rootView.findViewById(R.id.listView);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

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

                Intent intent=new Intent(mContext, ProductListing.class);
                intent.putExtra("id",arr_list.get(position).category_id);

                intent.putExtra("name",arr_list.get(position).event_name);
                mContext.startActivity(intent);
            }
        });
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
                String serverUrl = Constant.MAIN_URL + "homeevents";
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
                           // info.event_id=obj.getString("event_id");
                            info.event_name=obj.getString("event_name");
                            info.event_logo=obj.getString("event_logo");
                            info.event_image=obj.getString("event_image");
                            info.disc_amount=obj.getString("event_disc");
                            info.category_id=obj.getString("category_id");
                            info.event_start=obj.getString("event_start");
                            info.event_end=obj.getString("event_end");
                           // info.last_generate=obj.getString("last_generate");
                           // info.event_menu=obj.getString("event_menu");
                            info.disc_info=obj.getString("event_disinfo");
                            info.event_promo=obj.getString("event_promo");
                          //  info.event_category=obj.getString("event_category");



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

        try {

            updateTimer = new Timer();
            updateTimer.schedule(new TimerTask() {
                public void run() {


                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }

                }

            }, 0, 1000);




        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

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

    
}
