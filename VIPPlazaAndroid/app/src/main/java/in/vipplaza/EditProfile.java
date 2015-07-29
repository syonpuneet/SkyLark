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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import in.vipplaza.utills.IOUtils;

/**
 * Created by manish on 06-07-2015.
 */
public class EditProfile extends AppCompatActivity {


    ProgressDialog progressDialog;
    ConnectionDetector cd;
    Toolbar toolbar;
    Context mContext;
    SharedPreferences mPref;
    ImageView activity_title;
    ProgressBar progressBar;
    LinearLayout component_layout,change_password_layout;
    EditText et_first_name,et_last_name,et_email,et_gender;
    Button btn_save;
    int sort_by_selected_item=0;
    CheckBox password_checkbox;
    EditText old_password,new_password,confirm_password;
    Button btn_submit;
    String Useremail="";

    private MenuItem mCartItem;
    private TextView mCartCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_profile);

        mContext = EditProfile.this;

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

        et_first_name=(EditText)findViewById(R.id.et_first_name);
        et_last_name=(EditText)findViewById(R.id.et_last_name);
        et_email=(EditText)findViewById(R.id.et_email);
        et_gender=(EditText)findViewById(R.id.et_gender);

        old_password=(EditText)findViewById(R.id.old_password);
        new_password=(EditText)findViewById(R.id.new_password);
        confirm_password=(EditText)findViewById(R.id.confirm_password);
        password_checkbox=(CheckBox)findViewById(R.id.password_checkbox);
        change_password_layout=(LinearLayout)findViewById(R.id.change_password_layout);


        btn_save=(Button)findViewById(R.id.btn_save);
        btn_submit=(Button)findViewById(R.id.btn_submit);

        if(cd.isConnectingToInternet())
        {
            progressBar.setVisibility(View.VISIBLE);
            component_layout.setVisibility(View.GONE);
            GetProfileinformation();
        }

        else
        {
            String error=getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }


        et_gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final CharSequence[] items = getResources().getStringArray(
                        R.array.gender_arr);

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.txt_gender);
                builder.setSingleChoiceItems(items, sort_by_selected_item,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int pos) {
                                // TODO Auto-generated method stub


                                et_gender.setText(items[pos]);
                                dialog.dismiss();
                                sort_by_selected_item=pos;


                            }
                        });

                builder.show();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (cd.isConnectingToInternet()) {
                    String first_name = et_first_name.getText().toString();
                    String last_name = et_last_name.getText().toString();
                    String email = et_email.getText().toString();


                    if (first_name.isEmpty()) {

                        String error = getString(R.string.error_first_name);

                        Constant.showAlertDialog(mContext, error, false);

                    } else if (last_name.isEmpty()) {
                        String error = getString(R.string.error_last_name);

                        Constant.showAlertDialog(mContext, error, false);
                    } else if (email.isEmpty()) {
                        String error = getString(R.string.error_not_valid_email);

                        Constant.showAlertDialog(mContext, error, false);
                    } else if (!IOUtils.isValidEmail(email)) {
                        String error = getString(R.string.error_not_valid_email);

                        Constant.showAlertDialog(mContext, error, false);
                    } else {
                        progressDialog.show();
                        SaveProfileinformation();
                    }

                } else {
                    String error = getString(R.string.alert_no_internet);
                    Constant.showAlertDialog(mContext, error, false);
                }

            }
        });

        password_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    change_password_layout.setVisibility(View.VISIBLE);
                } else {
                    change_password_layout.setVisibility(View.GONE);
                }
            }
        });


        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (cd.isConnectingToInternet()) {
                    String oldPassword = old_password.getText().toString();
                    String newpassword = new_password.getText().toString();
                    String confirmPassword = confirm_password.getText().toString();


                    if (oldPassword.isEmpty() || oldPassword.length() < 6) {

                        String error = getString(R.string.error_old_password);

                        Constant.showAlertDialog(mContext, error, false);

                    } else if (newpassword.isEmpty() || newpassword.length() < 6) {
                        String error = getString(R.string.alert_password);

                        Constant.showAlertDialog(mContext, error, false);
                    } else if (!confirmPassword.equals(newpassword)) {
                        String error = getString(R.string.error_confirm_password);

                        Constant.showAlertDialog(mContext, error, false);
                    } else {
                        progressDialog.show();
                        ChangePassword();
                    }

                } else {
                    String error = getString(R.string.alert_no_internet);
                    Constant.showAlertDialog(mContext, error, false);
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

                        et_first_name.setText(obj.getString("firstname"));
                        et_last_name.setText(obj.getString("lastname"));
                        et_email.setText(obj.getString("email"));

                        Useremail=obj.getString("email");

                        String gender=obj.getString("gender");


                       // txt_hello.setText(getString(R.string.txt_hello)+" "+first_name+" "+last_name+"!" );

                        if(gender.equalsIgnoreCase("1"))
                        {
                            et_gender.setText(R.string.txt_male);
                            sort_by_selected_item=0;
                        }

                        else
                        {
                            et_gender.setText(R.string.txt_female);
                            sort_by_selected_item=1;
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


    private void SaveProfileinformation() {
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
                String serverUrl = Constant.MAIN_URL + "editProfile";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("uid", mPref.getString(Constant.user_id,""));
                parameter.put("fname", et_first_name.getText().toString());
                parameter.put("lname", et_last_name.getText().toString());
                parameter.put("email", et_email.getText().toString());
                parameter.put("gender", ""+(sort_by_selected_item+1));


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

                    // JSONObject objJson = obj.getJSONObject("Data");
                    status = Integer.parseInt(objJson.getString("status")
                            .toString());

                    msg = objJson.getString("msg").toString();

                    //user_id


                    if (status == 1) {

                        Useremail=et_email.getText().toString();
                        Constant.showAlertDialog(mContext,msg,false);


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


    private void ChangePassword() {
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
                String serverUrl = Constant.MAIN_URL + "changePass";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("uid", mPref.getString(Constant.user_id, ""));
                parameter.put("email", Useremail);
                parameter.put("oldpass", old_password.getText().toString());
                parameter.put("newpass", new_password.getText().toString());



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

                    // JSONObject objJson = obj.getJSONObject("Data");
                    status = Integer.parseInt(objJson.getString("status")
                            .toString());

                    msg = objJson.getString("msg").toString();

                    //user_id


                    if (status == 1) {

                        old_password.setText("");
                        new_password.setText("");
                        confirm_password.setText("");

                        Constant.showAlertDialog(mContext,msg, false);


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
