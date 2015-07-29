package in.vipplaza;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;
import in.vipplaza.utills.TouchImageView;
import in.vipplaza.utills.VerticalViewPager;

/**
 * Created by manish on 07-07-2015.
 */
public class ProductDetail extends AppCompatActivity{

    ProgressDialog progressDialog;
    ConnectionDetector cd;
    Toolbar toolbar;
    Context mContext;
    SharedPreferences mPref;
    ImageView activity_title;

    VerticalViewPager pager;
    String product_id;

    String id, long_description;
    String images[];
    DisplayImageOptions options;
    private CirclePageIndicator mIndicator;
    TextView discount_text;
    TextView name,price,sell_price;
    LinearLayout btn_detail, btn_size,add_to_bag;
    String size_arr[];
    int sort_by_selected_item=-1;
    TextView size_text;

    private MenuItem mCartItem;
    private TextView mCartCounter;

    boolean isSizeSelected=false;
    String size="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.product_detail);

        mContext = ProductDetail.this;

        //facebookDetails = new FacebookUserDetails((Activity) mContext);

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

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.dummy)
                .showImageForEmptyUri(R.drawable.dummy)
                .showImageOnFail(R.drawable.dummy).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        initalizevariable();
    }

    private void initalizevariable() {


        discount_text=(TextView)findViewById(R.id.discount_text);
        name=(TextView)findViewById(R.id.name);
        price=(TextView)findViewById(R.id.price);
        sell_price=(TextView)findViewById(R.id.sell_price);
        size_text=(TextView)findViewById(R.id.size_text);

        btn_detail=(LinearLayout)findViewById(R.id.btn_detail);
        btn_size=(LinearLayout)findViewById(R.id.btn_size);
        add_to_bag=(LinearLayout)findViewById(R.id.add_to_bag);


        Bundle bundle=getIntent().getExtras();
        product_id=bundle.getString("product_id");
        getSupportActionBar().setTitle(bundle.getString("name"));

        pager=(VerticalViewPager)findViewById(R.id.pager);


        if(cd.isConnectingToInternet())
        {
           progressDialog.show();

            ProductDetail();
        }

        else
        {
            String error=getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }


        btn_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                alert.setTitle(mContext.getResources().getString(R.string.txt_details));
                alert.setCancelable(true);
                alert.setMessage(Html.fromHtml(long_description));
                alert.setNeutralButton(R.string.close, null);
                alert.show();
            }
        });


        btn_size.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (size_arr != null) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.txt_size);
                    builder.setSingleChoiceItems(size_arr, sort_by_selected_item,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int pos) {
                                    // TODO Auto-generated method stub

                                    // sort_text.setText(items[pos]);
                                    isSizeSelected=true;
                                    dialog.dismiss();
                                    sort_by_selected_item = pos;
                                    size=size_arr[pos];

                                    size_text.setText(size_arr[pos] + "(Int)");
                                    size_text.setTextColor(getResources().getColor(R.color.btn_background_color));


                                }
                            });

                    builder.show();
                }

            }
        });


        add_to_bag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mPref.getBoolean(Constant.isLogin, false)) {
                    if (isSizeSelected) {

                        if (cd.isConnectingToInternet()) {
                            progressDialog.show();
                            AddToCart();
                        } else {
                            String error = getString(R.string.alert_no_internet);
                            Constant.showAlertDialog(mContext, error, false);
                        }


                    } else {

                        String error = getString(R.string.txt_select_size);
                        Constant.showAlertDialog(mContext, error, false);

                    }
                } else {
                    Intent intent = new Intent(mContext, LoginRegister.class);
                    startActivity(intent);

                }

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();


        if(mPref.getBoolean(Constant.isLogin, false))
        {
            if(cd.isConnectingToInternet())
            {

                updateCartCounter();

            }

            else
            {
                String error=getString(R.string.alert_no_internet);
                Constant.showAlertDialog(mContext, error, false);
            }
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



    private void ProductDetail() {
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
                String serverUrl = Constant.MAIN_URL + "productDetail";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("pid", product_id);



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
                    JSONArray objJson = new JSONArray(result);


                    JSONObject obj=objJson.getJSONObject(0);

                    //id=obj.getString("id");
                    name.setText(obj.getString("name"));
                   // short_description=obj.getString("short_description");
                   long_description=obj.getString("long_description");
                    price.setText(getString(R.string.txt_price_tag)+" "+obj.getString("price"));
                    sell_price.setText(getString(R.string.txt_price_tag)+" "+obj.getString("speprice"));
                    discount_text.setText(obj.getString("discount")+" OFF");



                    price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                    images=new String[obj.getJSONArray("img").length()];


                    JSONArray size=obj.optJSONArray("size");

                    if(size!=null)
                    {
                        size_arr=new String[size.length()];

                        for(int i=0; i<size.length();i++)
                        {
                            size_arr[i]=size.getString(i);
                        }
                    }


                    else
                    {

                        isSizeSelected=true;
                        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.MATCH_PARENT, 0.5f);

                        add_to_bag.setLayoutParams(param);
                        btn_detail.setLayoutParams(param);
                        btn_size.setVisibility(View.GONE);
                    }


                    for(int i=0;i<obj.getJSONArray("img").length();i++)
                    {
                        images[i]=obj.getJSONArray("img").getString(i);
                    }

                    Log.i("","images length==="+images.length);


                    setData();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }


    private void setData()
    {
        pager.setAdapter(new TouchImageAdapter());
        mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        mIndicator.setOrientation(android.widget.LinearLayout.VERTICAL);
        mIndicator.setViewPager(pager);

    }


    public class TouchImageAdapter extends PagerAdapter {

        private LayoutInflater inflater;
        ImageLoader imageLoader;
        @Override
        public int getCount() {
            return images.length;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {

            inflater = (LayoutInflater) container.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View imageLayout = inflater.inflate(R.layout.item_pager_image,
                    container, false);
            final TouchImageView img = (TouchImageView) imageLayout
                    .findViewById(R.id.image);
            final ProgressBar spinner = (ProgressBar) imageLayout
                    .findViewById(R.id.loading);


            ImageLoader.getInstance().displayImage(images[position], img, options,
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri,
                                                     View view) {
                            spinner.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri,
                                                    View view, FailReason failReason) {
                            String message = null;
                            switch (failReason.getType()) {
                                case IO_ERROR:
                                    message = "Input/Output error";
                                    break;
                                case DECODING_ERROR:
                                    message = "Image can't be decoded";
                                    break;
                                case NETWORK_DENIED:
                                    message = "Downloads are denied";
                                    break;
                                case OUT_OF_MEMORY:
                                    message = "Out Of Memory error";
                                    break;
                                case UNKNOWN:
                                    message = "Unknown error";
                                    break;
                            }
                           // Toast.makeText(mContext, message,
                              //      Toast.LENGTH_LONG).show();

                            Log.i("", "image load failed");


                            spinner.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri,
                                                      View view, Bitmap loadedImage) {
                            Log.i("", "onLoadingComplete");

                            spinner.setVisibility(View.GONE);
                            }
                        });



            container.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);


        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

    private void AddToCart() {
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
                String serverUrl = Constant.MAIN_URL + "addtocart";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("pid", product_id);
                parameter.put("qty", "1");
                parameter.put("size", size);
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

                progressDialog.dismiss();


                try {
                    JSONObject objJson = new JSONObject(result);

                    status=Integer.parseInt(objJson.getString("status"));
                    msg=objJson.getString("msg");

                    if(status==1)
                    {

                        Constant.showAlertDialog(mContext,msg,false);
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

                        if(mPref.getBoolean(Constant.isLogin,false))
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
