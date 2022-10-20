package com.StoreAndForwardAudioPlayer.receiver;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.widget.Toast;

import com.StoreAndForwardAudioPlayer.activities.Splash_Activity;

/**
 * Created by Developer on 28-05-2021.
 */

public class RebootJobService extends JobService {
    private static final String TAG = "SyncService";

    @Override
    public boolean onStartJob(JobParameters params) {
        Toast.makeText(RebootJobService.this, "Starting Job", Toast.LENGTH_SHORT).show();
        Intent service = new Intent(getApplicationContext(), Splash_Activity.class);
        getApplicationContext().startService(service);
        //LaunchReceiver.scheduleJob(getApplicationContext()); // reschedule the job
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

}