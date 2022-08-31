package com.StoreAndForwardAudioPlayer.alarm_manager;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.StoreAndForwardAudioPlayer.R;
import com.StoreAndForwardAudioPlayer.activities.HomeActivity;
import com.StoreAndForwardAudioPlayer.activities.Splash_Activity;
import com.StoreAndForwardAudioPlayer.utils.Utilities;

import java.util.List;

/**
 * Created by love on 22/7/18.
 */

public class ApplicationChecker extends Service {

    private Handler mHandler = null;

    private HandlerThread mHandlerThread = null;
    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id_7";

    private static int CHECK_TIME = 300000;

    static final String TAG = ApplicationChecker.class.getSimpleName();

    public ApplicationChecker() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // code to execute when the service is first created
        super.onCreate();
        Log.d(TAG, "Service Started.");
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent(this, ApplicationChecker.class);
        sendBroadcast(broadcastIntent);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid)
    {
        startForeground();
        mHandler.postDelayed(runnable,CHECK_TIME);
        return START_STICKY;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfo = am.getRunningAppProcesses();

            if (runningAppProcessInfo != null){
                Log.d(TAG,"Currently running activities" + runningAppProcessInfo.size());
                boolean isApplicationRunning = false;
                String appRunningStatus = "App is in state ";
                if(isAppRunning()){
                    Log.d(TAG,"App is in running state = ");
                    isApplicationRunning = true;
                    appRunningStatus += "Running";
                } else {
                    appRunningStatus += "Not Running";
                }

              //  Utilities.showToast(ApplicationChecker.this,appRunningStatus);

              if (!isApplicationRunning){
                  // Utilities.showToast(ApplicationChecker.this, "App Crashing");
                        Context ctx = ApplicationChecker.this; // or you can replace **'this'** with your **ActivityName.this**
                        Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.StoreAndForwardAudioPlayer");
                        ctx.startActivity(i);


                }
            }

            mHandler.postDelayed(runnable,CHECK_TIME);
        }
    };

    protected Boolean isAppRunning()
    {
        ActivityManager activityManager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        /*
        In case of any failure we assume the app is running.
         */
        if (tasks == null){
            return true;
        }

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (Splash_Activity.class.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()) ||
                    HomeActivity.class.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()))
                return true;
        }


        return false;
    }

    private void startForeground() {
        createnotific();
        Intent notificationIntent = new Intent(this, HomeActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.sbitlogo)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Service is running Foreground")
                .setContentIntent(pendingIntent)
                .build());


    }


    public  void createnotific()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIF_CHANNEL_ID, "SeviceNusign", importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
