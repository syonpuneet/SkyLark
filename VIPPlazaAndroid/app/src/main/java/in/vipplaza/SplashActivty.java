package in.vipplaza;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;


public class SplashActivty extends AppCompatActivity {
    private static final int SPLASH_DURATION = 2000; // 2 seconds

    Context mContext;
    ProgressBar progressBar;
    ConnectionDetector cd;
    int myProgress=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_activty);

        mContext=SplashActivty.this;
        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        cd=new ConnectionDetector(mContext);

        Log.i("","sangna"+Constant.printKeyHash(SplashActivty.this));

//        Handler handler = new Handler();
//
//        handler.postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                // TODO Auto-generated method stub
//
//                finish();
//
//              startActivity(new Intent(SplashActivty.this, DashBoardActivity.class));
//                // activity
//            }
//        }, SPLASH_DURATION);


    }


    @Override
    protected void onResume() {
        super.onResume();


        if(cd.isConnectingToInternet())
        {

            GetDrawerCategory();
        }

        else
        {
            String error=getString(R.string.alert_no_internet);
            Constant.showAlertDialog(mContext, error, false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash_activty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void GetDrawerCategory() {
        new AsyncTask<Void, Integer, String>() {

            int status;
            String msg = "";

            protected void onPreExecute() {

                super.onPreExecute();

                progressBar.setIndeterminate(false);
                progressBar.setMax(100);
               // progressBar.getProgressDrawable().setColorFilter(R.color.home_bottom_strip, PorterDuff.Mode.SRC_IN);


            };

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
            protected void onProgressUpdate(Integer... progress) {
                Log.d("", "Progress Update: " + progress[0].toString());

                super.onProgressUpdate(progress[0]);
                progressBar.setProgress(progress[0]);
               // teSecondsProgressedM.setText( progress[0].toString());
            }


            @Override
            protected String doInBackground(Void... params) {


                while(myProgress<100){
                    myProgress++;
                    publishProgress(myProgress);
                    SystemClock.sleep(50);
                }

                String result = "";
                String serverUrl = Constant.MAIN_URL+"categories";
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

               finish();

                Intent intent=new Intent(mContext, DashBoardActivity.class);
                intent.putExtra("response", result);
               startActivity(intent);

            }
        }.execute(null, null, null);
    }
}
