package com.StoreAndForwardAudioPlayer.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.StoreAndForwardAudioPlayer.activities.Splash_Activity;
import com.StoreAndForwardAudioPlayer.application.AlenkaMedia;

public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Activity activity;
    public MyExceptionHandler(Activity a) {
        activity = a;
    }
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Intent intent = new Intent(activity, Splash_Activity.class);
        ex.getCause().toString();
        intent.putExtra("crash", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(AlenkaMedia.getInstance(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) AlenkaMedia.getInstance().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 20000, pendingIntent);
        activity.finish();
        System.exit(2);
}


}
