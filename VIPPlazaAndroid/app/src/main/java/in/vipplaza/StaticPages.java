package in.vipplaza;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;

/**
 * Created by manish on 16-07-2015.
 */

@SuppressLint("SetJavaScriptEnabled")
public class StaticPages extends AppCompatActivity {

    Context mContext;
    SharedPreferences mPref;
    WebView webView;
    ProgressBar progressBar;
    ConnectionDetector cd;
    Toolbar toolbar;
    ImageView activity_title;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        mContext = StaticPages.this;

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
        initializeVariable();

    }

    private void initializeVariable() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        webView = (WebView) findViewById(R.id.webView1);

        webView.getSettings().setDomStorageEnabled(true);
        webView.setInitialScale(1);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);

        getSupportActionBar().setTitle(getIntent().getExtras().getString("name"));

        if (cd.isConnectingToInternet()) {

             progressBar.setVisibility(View.VISIBLE);


            Bundle bundle=getIntent().getExtras();
            int page_no=bundle.getInt("page_no");

            switch (page_no)
            {
                case 1:
                    GetUrl(""+13);
                    break;

                case 2:
                    GetUrl(""+8);
                    break;

                case 3:
                    GetUrl(""+14);
                    break;

                default:
                    break;
            }

//            Bundle bundle=getIntent().getExtras();
//
//            int page_no=bundle.getInt("page_no");
//            String URL = "";
//
//            switch (page_no)
//            {
//                case 1:
//                    URL=Constant.FAQ_URL;
//                    break;
//
//                case 2:
//                    URL=Constant.PRIVACY_URL;
//                    break;
//
//                case 3:
//                    URL=Constant.ABOUT_URL;
//                    break;
//
//                default:
//                    break;
//            }
//
//            WebViewClient viewClient = new WebViewClient() {
//
//                @Override
//                public boolean shouldOverrideUrlLoading(WebView view,
//                                                        String url) {
//
//                    view.loadUrl(url);
//
//                    return true;
//                }
//            };
//
//            webView.setWebViewClient(viewClient);
//
//            webView.loadUrl(URL);
//
//            webView.setWebViewClient(viewClient);
//            webView.setWebChromeClient(new WebChromeClient() {
//                public void onProgressChanged(WebView view, int progress) {
//                    if (progress < 100
//                            && progressBar.getVisibility() == ProgressBar.GONE) {
//                        progressBar.setVisibility(ProgressBar.VISIBLE);
//                        // txtview.setVisibility(View.VISIBLE);
//                    }
//                    progressBar.setProgress(progress);
//                    if (progress == 100) {
//                        progressBar.setVisibility(ProgressBar.GONE);
//                        // txtview.setVisibility(View.GONE);
//                    }
//                }
//            });

        }

        else

        {
            String message = mContext.getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, message, false);
        }

		/*
		 * webView.setOnTouchListener(new OnTouchListener() {
		 *
		 * @Override public boolean onTouch(View v, MotionEvent event) { // TODO
		 * Auto-generated method stub return true; } });
		 */
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {

            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void GetUrl(final String id) {
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
                String serverUrl = Constant.MAIN_URL + "cms";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("id", id);




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


                try {
                    JSONObject objJson = new JSONObject(result);


                    String data=objJson.getString("html");
                    webView.loadData(data, "text/html", "UTF-8");
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        System.out.println("facebook destroyed");

        webView.stopLoading();

    }
}
