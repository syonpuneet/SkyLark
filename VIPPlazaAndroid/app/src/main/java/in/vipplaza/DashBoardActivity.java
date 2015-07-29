package in.vipplaza;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.vipplaza.adapter.NavDrawerListAdapter;
import in.vipplaza.fragment.HomeListFragment;
import in.vipplaza.info.NavdrwableIems;
import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;

/**
 * Created by manish on 29-06-2015.
 */
public class DashBoardActivity extends AppCompatActivity implements View.OnClickListener{

    private RelativeLayout list_top;
    private RelativeLayout list_bottom;
    private RelativeLayout relativeLayout;
    private DrawerLayout drawerLayoutt;
    private ListView listView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    private String[] navigationDrawerItems;
    private ArrayList<NavdrwableIems> navDrawerItems;
    NavDrawerListAdapter adapter;
    SharedPreferences mPref;
    Context mContext;
    ConnectionDetector cd;
    Bundle savedInstanceState;
    String result = "";
    // View footer_view;
    Button loginLogout;
    boolean isListChildShowing = false;
    TextView header_text;

    private MenuItem mCartItem;
    private TextView mCartCounter;

    LinearLayout btn_faq,btn_privacy,btn_about;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);
        mPref = getSharedPreferences(getResources()
                .getString(R.string.pref_name), Activity.MODE_PRIVATE);
        mContext = DashBoardActivity.this;


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.status_bar_color));

        Constant.getOverflowMenu(mContext);

        cd = new ConnectionDetector(mContext);
        relativeLayout = (RelativeLayout) findViewById(R.id.relative_layout);
        list_top = (RelativeLayout) findViewById(R.id.list_top);
        list_bottom = (RelativeLayout) findViewById(R.id.list_bottom);
        header_text = (TextView) findViewById(R.id.header_text);

        this.savedInstanceState = savedInstanceState;
        initializeVariable();
    }


    @SuppressLint("InflateParams")
    private void initializeVariable() {

        btn_privacy=(LinearLayout)findViewById(R.id.btn_privacy);
        btn_faq=(LinearLayout)findViewById(R.id.btn_faq);
        btn_about=(LinearLayout)findViewById(R.id.btn_about);



        btn_faq.setOnClickListener(this);
        btn_privacy.setOnClickListener(this);
        btn_about.setOnClickListener(this);

        navDrawerItems = new ArrayList<NavdrwableIems>();
        Bundle bundle = getIntent().getExtras();
        result = bundle.getString("response");
        setDrawerData();
        setDrawer();


        header_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isListChildShowing = false;
                manageDrwaerListView();
                setDrawerData();
                adapter.notifyDataSetChanged();
            }
        });

    }



    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        drawerLayoutt.closeDrawer(relativeLayout);
        Intent intent=new Intent(mContext,StaticPages.class);
        switch (view.getId()) {


            case R.id.btn_privacy:

                intent.putExtra("page_no",2);
                intent.putExtra("name",getString(R.string.txt_kebijakan));
                break;

            case R.id.btn_faq:

                intent.putExtra("page_no",1);
                intent.putExtra("name",getString(R.string.txt_faq));
                break;
            case R.id.btn_about:

                intent.putExtra("page_no",3);
                intent.putExtra("name",getString(R.string.txt_about));
                break;

            default:
                break;
        }


        startActivity(intent);

    }


    private void setDrawerData() {
        try {

            navDrawerItems.clear();

            JSONArray arrjson = new JSONArray(result);

            NavdrwableIems info;

            navigationDrawerItems = new String[arrjson.length()];

            for (int i = 0; i < arrjson.length(); i++) {
                JSONObject objJson = arrjson.getJSONObject(i);

                info = new NavdrwableIems();

                info.id = objJson.getString("id");
                info.name = objJson.getString("name");
                info.path = objJson.getString("path");

                navigationDrawerItems[i] = objJson.getString("name");

                navDrawerItems.add(info);

            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

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

               return  true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    private void setDrawer() {

        navigationDrawerItems = getResources().getStringArray(
                R.array.nav_drawer_items);
        drawerLayoutt = (DrawerLayout) findViewById(R.id.drawer_layout);
        listView = (ListView) findViewById(R.id.left_drawer);


        drawerLayoutt.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);

        //LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // footer_view = inflater.inflate(R.layout.drawer_footer, null);

        loginLogout = (Button) findViewById(R.id.login_btn);

        adapter = new NavDrawerListAdapter(mContext, navDrawerItems);

        //  listView.addFooterView(footer_view);
        listView.setAdapter(adapter);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayoutt,
                toolbar, R.string.app_name, R.string.app_name);
        drawerLayoutt.setDrawerListener(actionBarDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState == null) {
            selectItem(0);
        }


        loginLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mPref.getBoolean(Constant.isLogin, false)) {

                    drawerLayoutt.closeDrawer(relativeLayout);
                    Intent intent = new Intent(mContext, ProfileActivity.class);
                    startActivity(intent);


                } else {
                    drawerLayoutt.closeDrawer(relativeLayout);
                    Intent intent = new Intent(mContext, LoginRegister.class);
                    startActivity(intent);
                }
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                // if(isListChildShowing)
                //  {


                drawerLayoutt.closeDrawer(relativeLayout);
                Intent intent = new Intent(mContext, CategoryDealListing.class);
                intent.putExtra("id", navDrawerItems.get(position).id);
                intent.putExtra("name", navDrawerItems.get(position).name);
                startActivity(intent);
                //     }

//                else {
//                    isListChildShowing=true;
//                    manageDrwaerListView();
//                    try {
//
//                        JSONArray arrjson = new JSONArray(result);
//
//                        NavdrwableIems info;
//
//                        //  navigationDrawerItems=new String[arrjson.length()];
//
//
//                        navDrawerItems.clear();
//
//                        JSONObject objJson = arrjson.getJSONObject(position);
//
//                        header_text.setText(objJson.getString("name"));
//
//                        JSONArray data = objJson.getJSONArray("0");
//
//                        for (int i = 0; i < data.length(); i++) {
//
//                            JSONObject dataJson = data.getJSONObject(i);
//                            info = new NavdrwableIems();
//
//                            info.id = dataJson.getString("id");
//                            info.name = dataJson.getString("name");
//                            info.path = dataJson.getString("url");
//
//                            //navigationDrawerItems[i] = objJson.getString("name");
//
//                            navDrawerItems.add(info);
//                        }
//
//
//
//                    } catch (JSONException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//
//                    }
//
//
//
//                    adapter.notifyDataSetChanged();
//
//
//                }
            }
        });
    }


    private void manageDrwaerListView() {
        if (isListChildShowing) {

            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            listView.startAnimation(animation);

            list_bottom.setVisibility(View.GONE);
            list_top.setVisibility(View.VISIBLE);
        } else {

            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            listView.startAnimation(animation);
            list_top.setVisibility(View.GONE);
            list_bottom.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPref.getBoolean(Constant.isLogin, false)) {
            loginLogout.setText(R.string.txt_profile);

        } else {
            loginLogout.setText(R.string.txt_login);

        }


        if (cd.isConnectingToInternet()) {

            updateCartCounter();

        } else {
            String error = getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void setTitle(int position) {

    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        try {
            actionBarDrawerToggle.syncState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectItem(int position) {

        Fragment fragment = null;
        switch (position) {

//for profile fragment(-1)


            default:
                fragment = new HomeListFragment();
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment).addToBackStack(null).commit();
        listView.setItemChecked(position, true);
        // listView.setSelection(position);
        // setTitle(navigationDrawerItems[position]);
        drawerLayoutt.closeDrawer(relativeLayout);

    }

    public class DummyFragment extends Fragment {
        public final static String ARG_MENU_INDEX = "index";

        public DummyFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.dummy_fragment,
                    container, false);
            // int index = getArguments().getInt(ARG_MENU_INDEX);
            // String text = String.format("Menu at index %s", index);
            ((TextView) rootView.findViewById(R.id.textView))
                    .setText("Under Construction!");
            // getActivity().setTitle(text);
            return rootView;
        }
    }


    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                finish();
            } else {
                getSupportFragmentManager().popBackStack();
            }
        } else {
            super.onBackPressed();
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
                        //Constant.showAlertDialog(mContext, msg, false);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }


}
