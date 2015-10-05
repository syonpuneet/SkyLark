package in.vipplaza;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;
import in.vipplaza.utills.IOUtils;

/**
 * Created by manish on 30-07-2015.
 */
public class ContactUs extends AppCompatActivity implements View.OnClickListener {

    Context mContext;
    SharedPreferences mPref;

    ProgressDialog progressDialog;
    ConnectionDetector cd;
    Toolbar toolbar;
    ImageView activity_title;

    TextView office_address, telephone, txt_facebook, txt_twitter, txt_instagram, mail1, mail2, mail3, mail4;
    EditText et_name, et_email, et_telephone, et_order;
    Button btn_send;

    String facebook_url = "", twitter_url = "", instagram_url = "";

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_us);

        mContext = ContactUs.this;

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
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initializeVariable();

    }

    private void initializeVariable() {

        // EditText et_name,et_email,et_telephone,et_order;
        // Button btn_send;


        getSupportActionBar().setTitle(R.string.activity_contact_us);

        office_address = (TextView) findViewById(R.id.office_address);
        telephone = (TextView) findViewById(R.id.telephone);
        txt_facebook = (TextView) findViewById(R.id.txt_facebook);
        txt_twitter = (TextView) findViewById(R.id.txt_twitter);
        txt_instagram = (TextView) findViewById(R.id.txt_instagram);
        mail1 = (TextView) findViewById(R.id.mail1);
        mail2 = (TextView) findViewById(R.id.mail2);
        mail3 = (TextView) findViewById(R.id.mail3);
        mail4 = (TextView) findViewById(R.id.mail4);

        et_name = (EditText) findViewById(R.id.et_name);
        et_email = (EditText) findViewById(R.id.et_email);
        et_telephone = (EditText) findViewById(R.id.et_telephone);
        et_order = (EditText) findViewById(R.id.et_order);

        btn_send = (Button) findViewById(R.id.btn_send);


        txt_facebook.setOnClickListener(this);
        txt_twitter.setOnClickListener(this);
        txt_instagram.setOnClickListener(this);


        mail1.setOnClickListener(this);
        mail2.setOnClickListener(this);
        mail3.setOnClickListener(this);
        mail4.setOnClickListener(this);


        if (cd.isConnectingToInternet()) {

            progressDialog.show();
            ContactUs();
        } else {
            String message = mContext.getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, message, false);
        }


        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkValidation();
            }
        });


    }


    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub

        switch (view.getId()) {


            case R.id.mail1:

                sendEmail(mail1.getText().toString());
                break;

            case R.id.mail2:

                sendEmail(mail2.getText().toString());
                break;
            case R.id.mail3:

                sendEmail(mail3.getText().toString());
                break;

            case R.id.mail4:

                sendEmail(mail4.getText().toString());
                break;

            case R.id.txt_facebook:

                openBrowser(facebook_url);
                break;


            case R.id.txt_twitter:

                openBrowser(twitter_url);
                break;


            case R.id.txt_instagram:

                openBrowser(instagram_url);
                break;

            default:
                break;
        }


    }


    private void openBrowser(String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
        startActivity(intent);
    }


    private void sendEmail(String email) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_SUBJECT, "");
        intent.putExtra(Intent.EXTRA_TEXT, "");
        startActivity(Intent.createChooser(intent, ""));
    }


    private void ContactUs() {
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
                String serverUrl = Constant.MAIN_URL + "contactInfo";
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

                progressDialog.dismiss();

                try {
                    JSONArray objJson = new JSONArray(result);

                    JSONObject obj = objJson.getJSONObject(0);

                    office_address.setText(Html.fromHtml(obj.getString("contactinfo")));
                    telephone.setText(Html.fromHtml(obj.getString("customersupport")));

                    txt_facebook.setText(obj.getJSONObject("social").getJSONObject("facebook").getString("name"));
                    txt_twitter.setText(obj.getJSONObject("social").getJSONObject("twitter").getString("name"));
                    txt_instagram.setText(obj.getJSONObject("social").getJSONObject("instagram").getString("name"));

                    facebook_url = obj.getJSONObject("social").getJSONObject("facebook").getString("url");
                    twitter_url = obj.getJSONObject("social").getJSONObject("twitter").getString("url");
                    instagram_url = obj.getJSONObject("social").getJSONObject("instagram").getString("url");

                    mail1.setText(obj.getJSONObject("email_address").getJSONArray("email").getString(0));
                    mail2.setText(obj.getJSONObject("email_address").getJSONArray("email").getString(1));
                    mail3.setText(obj.getJSONObject("email_address").getJSONArray("email").getString(2));
                    mail4.setText(obj.getJSONObject("email_address").getJSONArray("email").getString(3));


                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }

    private void checkValidation() {
        if (cd.isConnectingToInternet()) {

            if (et_name.getText().toString().isEmpty()) {
                String error = getString(R.string.error_first_name);
                Constant.showAlertDialog(mContext, error, false);

            } else if (et_email.getText().toString().isEmpty()) {

                String error = getString(R.string.error_not_valid_email);
                Constant.showAlertDialog(mContext, error, false);

            } else if (!IOUtils.isValidEmail(et_email.getText().toString())) {
                String error = getString(R.string.error_not_valid_email);
                Constant.showAlertDialog(mContext, error, false);

            }

            else if (et_order.getText().toString().isEmpty()) {

                String error = getString(R.string.error_pesan);
                Constant.showAlertDialog(mContext, error, false);

            }


            else
            {
                progressDialog.show();
                SendQuery();
            }



        } else {
            String message = mContext.getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, message, false);
        }

    }


    private void SendQuery() {
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
                String serverUrl = Constant.MAIN_URL + "contactMail";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("name", et_name.getText().toString());
                parameter.put("email", et_email.getText().toString());
                parameter.put("telephone", et_telephone.getText().toString());
                parameter.put("comment", et_order.getText().toString());


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

                    status=Integer.parseInt(objJson.getString("success"));
                    msg=objJson.getString("msg");

                    if(status==1)
                    {
                        et_email.setText("");
                        et_name.setText("");
                        et_order.setText("");
                        et_telephone.setText("");

                        Constant.showAlertDialog(mContext,msg,false);

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
