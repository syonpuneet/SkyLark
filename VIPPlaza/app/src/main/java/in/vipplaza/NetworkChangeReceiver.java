package in.vipplaza;

/**
 * Created by manish on 09-09-2015.
 */

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import in.vipplaza.utills.ConnectionDetector;
import in.vipplaza.utills.Constant;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static boolean firstConnect = true;

    @Override
    public void onReceive(final Context context, final Intent intent) {


        Log.i("", "on recieve===");

        int status = NetworkUtil.getConnectivityStatusString(context);

        if (status == 0) {
           // Toast.makeText(context, context.getString(R.string.alert_no_internet), Toast.LENGTH_LONG).show();

            Log.i("","isAppRunning===="+isAppRunning(context));

            if(isAppRunning(context)) {
                Intent i = new Intent(context, InternetAlertActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }

        }
        //


    }




    public static boolean isAppRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        String packageName = activityManager.getRunningTasks(1).get(0).topActivity
                .getPackageName();

        if (packageName.equals(context.getPackageName())) {
            return true;
        }

//        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
//                .getRunningAppProcesses();
//        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
//
//            Log.i("","appProcess.processName===="+appProcess.processName);
//            Log.i("","context.getPackageName()===="+context.getPackageName());
//            if (appProcess.processName.equals(context.getPackageName())) {
//                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE) {
//                    return true;
//                }
//            }
//        }
        return false;
    }


}