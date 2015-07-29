package in.vipplaza;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.internal.SessionTracker;
import com.facebook.internal.Utility;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;
import in.vipplaza.utills.IOUtils;

/**
 * Created by manish on 29-06-2015.
 */
public class LoginRegister extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener,View.OnClickListener , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //activity components
    ProgressDialog progressDialog;
    ConnectionDetector cd;
    Toolbar toolbar;
    Context mContext;
    SharedPreferences mPref;


    LinearLayout login_tab, register_tab;
    LinearLayout login_layout, register_layout;
    boolean isLoginLayoutVisible = true;

    //Login components
    EditText login_email, login_password;
    TextView forgot_password;
    Button btn_login;
    //signup components

    EditText register_email, register_password,register_confirm_password;
    Button btn_female, btn_male;

  // private FacebookUserDetails facebookDetails;
  public static SessionTracker mSessionTracker;

    LinearLayout fb_sign, google_sign;

    // google plus sign in
    private static final int RC_SIGN_IN = 0;
    // Logcat tag
    private static final String TAG = "MainActivity";

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private boolean mIntentInProgress;

    private boolean mSignInClicked;

    private ConnectionResult mConnectionResult;

    CheckBox login_password_eye,register_password_eye,register_confirm_password_eye;

    private MenuItem mCartItem;
    private TextView mCartCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_register);

        mContext = LoginRegister.this;

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


        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
        initalizevariable();
    }


    private void initalizevariable() {
        login_tab = (LinearLayout) findViewById(R.id.login_tab);
        register_tab = (LinearLayout) findViewById(R.id.register_tab);
        login_layout = (LinearLayout) findViewById(R.id.login_layout);
        register_layout = (LinearLayout) findViewById(R.id.register_layout);

        login_password_eye=(CheckBox)findViewById(R.id.login_password_eye);
        register_password_eye=(CheckBox)findViewById(R.id.register_password_eye);
        register_confirm_password_eye=(CheckBox)findViewById(R.id.register_password_eye);


        //Login components
        login_email = (EditText) findViewById(R.id.login_email);
        login_password = (EditText) findViewById(R.id.login_password);
        forgot_password = (TextView) findViewById(R.id.forgot_password);
        btn_login = (Button) findViewById(R.id.btn_login);

        //signup components
        register_email = (EditText) findViewById(R.id.register_email);
        register_password = (EditText) findViewById(R.id.register_password);
        register_confirm_password = (EditText) findViewById(R.id.register_confirm_password);

        btn_female = (Button) findViewById(R.id.btn_female);
        btn_male = (Button) findViewById(R.id.btn_male);

        login_tab.setOnClickListener(this);
        register_tab.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        btn_female.setOnClickListener(this);
        btn_male.setOnClickListener(this);

        fb_sign = (LinearLayout) findViewById(R.id.fb_sign);
        google_sign = (LinearLayout) findViewById(R.id.google_sign);


        fb_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signInWithFacebook();

            }
        });

        google_sign.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Log.i("", "google button is clicked");
                if (cd.isConnectingToInternet()) {

                    mGoogleApiClient.connect();
                    signInWithGplus();
                } else {
                    String connection_alert = getResources().getString(
                            R.string.alert_no_internet);
                    Constant.showAlertDialog(mContext, connection_alert, false);
                }
            }
        });

        login_password_eye.setOnCheckedChangeListener(this);
        register_password_eye.setOnCheckedChangeListener(this);
        register_confirm_password_eye.setOnCheckedChangeListener(this);


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


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId())
        {
            case R.id.login_password_eye:

                showHidePassword(isChecked, login_password);
                break;

            case  R.id.register_password_eye:

                showHidePassword(isChecked,register_password);
                break;

            case  R.id.register_confirm_password_eye:

                showHidePassword(isChecked,register_confirm_password);
                break;

            default:
                break;
        }

    }


    private void showHidePassword(boolean isChecked, EditText editText)
    {


        if(isChecked) {
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            editText.setInputType(129);
        }
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch (view.getId()) {

            case R.id.login_tab:
                if (!isLoginLayoutVisible) {
                    showLoginLayout();
                }
                break;

            case R.id.register_tab:

                if (isLoginLayoutVisible) {
                    showRegisterlayout();
                }
                break;

            case R.id.btn_login:
                checkValidation();
                break;

            case R.id.btn_male:
                checkValidationSignup("1");
                break;

            case R.id.btn_female:
                checkValidationSignup("2");
                break;

            default:
                break;
        }

    }


    private void showLoginLayout() {
        register_tab.setBackgroundResource(R.color.white);
        login_tab.setBackgroundResource(R.drawable.bottom_border);

        isLoginLayoutVisible = true;
        login_layout.setVisibility(View.VISIBLE);
        register_layout.setVisibility(View.GONE);
    }

    private void showRegisterlayout() {


        login_tab.setBackgroundResource(R.color.white);
        register_tab.setBackgroundResource(R.drawable.bottom_border);
        isLoginLayoutVisible = false;
        login_layout.setVisibility(View.GONE);
        register_layout.setVisibility(View.VISIBLE);
    }

    private void checkValidation() {
        String emailText = login_email.getText().toString();
        String passwordText = login_password.getText().toString();

        if (cd.isConnectingToInternet()) {

            if (emailText.isEmpty()) {

                String error = getString(R.string.error_not_valid_email);
                Constant.showAlertDialog(mContext, error, false);

            } else if (!IOUtils.isValidEmail(emailText)) {
                String error = getString(R.string.error_not_valid_email);
                Constant.showAlertDialog(mContext, error, false);

            } else if (passwordText.isEmpty() || passwordText.length() < 6) {
                String error = getString(R.string.alert_password);
                Constant.showAlertDialog(mContext, error, false);


            } else {

                LoginTask();


            }
        } else {
            String error = getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }
    }

    private void LoginTask() {
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
                String serverUrl = Constant.MAIN_URL + "login";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("email", login_email.getText().toString());
                parameter.put("password", login_password.getText().toString());


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


                        String user_id = objJson.getString("user_id").toString();
                        mPref.edit().putString(Constant.user_id, user_id).commit();
                        mPref.edit().putBoolean(Constant.isLogin, true).commit();

                        finish();


                    } else {
                        // error, print message.
                        Constant.showAlertDialog(mContext, msg, false);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }


    private void checkValidationSignup(String gender) {
        String emailText = register_email.getText().toString();
        String passwordText = register_password.getText().toString();
        String confirm_password=register_confirm_password.getText().toString();

        if (cd.isConnectingToInternet()) {

            if (emailText.isEmpty()) {

                String error = getString(R.string.error_not_valid_email);
                Constant.showAlertDialog(mContext, error, false);

            } else if (!IOUtils.isValidEmail(emailText)) {
                String error = getString(R.string.error_not_valid_email);
                Constant.showAlertDialog(mContext, error, false);

            } else if (passwordText.isEmpty() || passwordText.length() < 6) {
                String error = getString(R.string.alert_password);
                Constant.showAlertDialog(mContext, error, false);


            }
            else if (!confirm_password.equals(passwordText)) {
                String error = getString(R.string.error_confirm_password);
                Constant.showAlertDialog(mContext, error, false);


            }

            else {

                SignupTask(gender);


            }
        } else {
            String error = getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }
    }

    private void SignupTask(final String gender) {
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
                String serverUrl = Constant.MAIN_URL + "signup";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("email", register_email.getText().toString());
                parameter.put("password", register_password.getText().toString());
                parameter.put("gender", gender);


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


                        String user_id = objJson.getString("user_id").toString();
                        mPref.edit().putString(Constant.user_id, user_id).commit();
                        mPref.edit().putBoolean(Constant.isLogin, true).commit();

                        finish();


                    } else {
                        // error, print message.
                        Constant.showAlertDialog(mContext, msg, false);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }


            }
        }.execute(null, null, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }

        else {

            if (resultCode == RESULT_OK) {
                Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
                if (Constant.mCurrentSession.isOpened()) {


                    accessFacebookUserInfo();
                } else {
                    //Util.showCenterToast(contxt, getResources().getString(R.string.msg_went_wrong));
                }
            } else {
               // Constant.mCurrentSession = null;
              //  FacebookUserDetails.mSessionTracker.setSession(null);
            }

        }
    }



    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Method to resolve any signin errors
     * */
    private void resolveSignInError() {

        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(LoginRegister.this,
                        RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }

        else {

            Log.i("", "mConnectionResult.hasResolution()==" + false);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        Log.i("", "onConnectionFailed");
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            resolveSignInError();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            Log.i("", "mConnectionResult===" + mConnectionResult);
            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }

    }

    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        // Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();

        // Get user's information
        getProfileInformation();

    }

    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);

                String personId = currentPerson.getId();
                String personLocation = currentPerson.getCurrentLocation();
                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String personGooglePlusProfile = currentPerson.getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                int gender = currentPerson.getGender();

                Log.e(TAG, "Name: " + personName + ", plusProfile: "
                        + personGooglePlusProfile + ", email: " + email
                        + ", Image: " + personPhotoUrl + ", PersonId: "
                        + personId + ", personLocation: " + personLocation);

                // userID="",userLocation="",;
               // userEmail = email;
               // signupType = "googleplus";
               // name = personName;
               // userImageURL = personPhotoUrl;
               // userID = personId;
               // userLocation = personLocation;
               // new SignupTask().execute();

                String first_name="VIP";
                String last_name="CUSTOMER";

                String split[]= personName.split(" ");

                if(split.length>=2)
                {
                    first_name=split[0];
                    last_name=split[1];
                }

                if(gender==0)
                {
                    SignupTaskWithSocial(email, "1",first_name,last_name);
                }

                else
                {
                    SignupTaskWithSocial(email, "2",first_name,last_name);
                }


            } else {
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();

    }

    private void signInWithGplus() {
        mSignInClicked = true;

        try {
            if (!mGoogleApiClient.isConnecting()) {

                Log.i("", "connecting");

                resolveSignInError();
            }

            else {
                Log.i("", "not connecting");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getFacebookData(GraphUser user)
    {

                String email="",gender="",first_name="",last_name="";

        try {
            email = user.asMap().get("email").toString();
             gender = user.asMap().get("gender").toString();

            last_name=user.asMap().get("last_name").toString();
            first_name=user.asMap().get("first_name").toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        if(email.equals("")||email==null)
        {

        }

        else
        {

            if(gender.equalsIgnoreCase("male"))
            {
                SignupTaskWithSocial(email, "1",first_name,last_name);
            }

            else
            {
                SignupTaskWithSocial(email, "2",first_name,last_name);
            }

        }

        Log.i("", "data recived" + email + " " + gender);

    }


    public void signInWithFacebook() {
        mSessionTracker = new SessionTracker(mContext, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {}
        }, null, false);

        String applicationId = Utility.getMetadataApplicationId(mContext);
        Constant.mCurrentSession = mSessionTracker.getSession();

        if (Constant.mCurrentSession == null || Constant.mCurrentSession.getState().isClosed()) {
            mSessionTracker.setSession(null);
            Session session = new Session.Builder(mContext).setApplicationId(applicationId).build();
            Session.setActiveSession(session);
            Constant.mCurrentSession = session;
        } else {
            mSessionTracker.setSession(null);
            Session session = new Session.Builder(mContext).setApplicationId(applicationId).build();
            Session.setActiveSession(session);
            Constant.mCurrentSession = session;
        }

        if (!Constant.mCurrentSession.isOpened()) {
            Session.OpenRequest openRequest = null;
            openRequest = new Session.OpenRequest((Activity) mContext);

            if (openRequest != null) {
                openRequest.setDefaultAudience(SessionDefaultAudience.FRIENDS);
                openRequest.setPermissions(Arrays.asList("user_birthday", "email", "user_location", "user_friends"));
                openRequest.setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK);

                Constant.mCurrentSession.openForRead(openRequest);
                accessFacebookUserInfo();
            }
        } else {
            accessFacebookUserInfo();
        }
    }

    public void accessFacebookUserInfo() {
        if (Session.getActiveSession() != null || Session.getActiveSession().isOpened()) {
            Bundle bundle = new Bundle();
            bundle.putString("fields", "picture");
            final Request request = new Request(Session.getActiveSession(), "me", bundle, HttpMethod.GET, new Request.Callback() {
                @SuppressWarnings("unused")
                @Override
                public void onCompleted(Response response) {
                    GraphObject graphObject = response.getGraphObject();
                    if (graphObject != null) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject = graphObject.getInnerJSONObject();

                            Log.i("","faacebook response==="+jsonObject.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            Request.executeBatchAsync(request);



            Request meRequest = Request.newMeRequest(Session.getActiveSession(), new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    try {
                        Constant.userDetails = user;
                        Log.i("","user======"+user);

                        if(user!=null)
                        {
                            getFacebookData(user);

                           // LoginRegister.getFacebookData(context, user);
                        }

                    } catch (Exception jex) {
                        jex.printStackTrace();
                    }
                }
            });

            RequestBatch requestBatch = new RequestBatch(meRequest);
            requestBatch.addCallback(new RequestBatch.Callback() {
                @Override
                public void onBatchCompleted(RequestBatch batch) {
                    Log.d("asdf", "asdf");
                }
            });
            requestBatch.executeAsync();
        }
    }




    private void SignupTaskWithSocial(final String email,final String gender,final String first_name,final String last_name) {
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
                String serverUrl = Constant.MAIN_URL + "social";
                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put("email", email);
                parameter.put("password", "");
                parameter.put("gender", gender);
                parameter.put("fname", first_name);
                parameter.put("lname", last_name);


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


                        String user_id = objJson.getString("user_id").toString();
                        mPref.edit().putString(Constant.user_id, user_id).commit();
                        mPref.edit().putBoolean(Constant.isLogin, true).commit();

                        finish();


                    } else {
                        // error, print message.
                        Constant.showAlertDialog(mContext, msg, false);
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
