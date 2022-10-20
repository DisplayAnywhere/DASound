package com.StoreAndForwardAudioPlayer.receiver;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.StoreAndForwardAudioPlayer.R;
import com.StoreAndForwardAudioPlayer.activities.Splash_Activity;
import com.StoreAndForwardAudioPlayer.alarm_manager.ApplicationChecker;

import java.util.Objects;

/**
 * Created by patas tech on 10/5/2016.
 */
public class LaunchReceiver extends BroadcastReceiver{
    public static final String TAG_NOTIFICATION = "NOTIFICATION_MESSAGE";
    public static final String CHANNEL_ID = "channel_1111";
    public static final int NOTIFICATION_ID = 111111;
    private static final String TAG = "Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
      // if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {

            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                Toast.makeText(context, "Launch receiver received", Toast.LENGTH_SHORT).show();
                // scheduleJob(context);
             /*   Intent notifyIntent = new Intent(context, Splash_Activity.class);
// Set the Activity to start in a new, empty task
                notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
// Create the PendingIntent
                PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                        context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
                );

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
                builder.setContentIntent(notifyPendingIntent);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(NOTIFICATION_ID, builder.build());*/
                Toast.makeText(context, "Launch receiver received upto Androi9", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(context, Splash_Activity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);





                //startActivityNotification(context, NOTIFICATION_ID, "NusignAudioPlayer", "OnBootCompleted");
            }
      //  }


        else {
           /* if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
          Toast.makeText(context, "Launch receiver received upto Androi9", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(context, Splash_Activity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }*/
        }


    }

    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context,RebootJobService.class );
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setPersisted(true);
        //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
       // builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());

    }


    public static void startActivityNotification(Context context, int notificationID,
                                                 String title, String message) {

        NotificationManager mNotificationManager =
                (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
        //Create GPSNotification builder
        NotificationCompat.Builder mBuilder;

        //Initialise ContentIntent
        Intent ContentIntent = new Intent(context, Splash_Activity.class);

        PendingIntent ContentPendingIntent = PendingIntent.getActivity(context,
                0,
                ContentIntent, 0);

        mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.logonusign)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setFullScreenIntent(ContentPendingIntent,true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,
                    "Activity Opening Notification",
                    NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription("Activity opening notification");

            mBuilder.setChannelId(CHANNEL_ID);

           Objects.requireNonNull(mNotificationManager).createNotificationChannel(mChannel);
        }

     Objects.requireNonNull(mNotificationManager).notify(TAG_NOTIFICATION,notificationID,
               mBuilder.build());

         // or you can replace **'this'** with your **ActivityName.this**


    }




}



