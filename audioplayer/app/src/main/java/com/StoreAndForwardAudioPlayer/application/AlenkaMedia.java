package com.StoreAndForwardAudioPlayer.application;

import android.app.Application;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.StoreAndForwardAudioPlayer.BuildConfig;
import com.StoreAndForwardAudioPlayer.utils.ConnectivityReceiver;
import com.StoreAndForwardAudioPlayer.utils.LoggingExceptionHandler;
import com.bugfender.sdk.Bugfender;

import java.io.File;

/**
 * Created by love on 29/5/17.
 */
public class AlenkaMedia extends Application {

    public static final String TAG = "AlenkaMedia";

    String device_id;

    static SharedPreferences prefs;

    SharedPreferences.Editor editor;

    private static AlenkaMedia mInstance;

    public static int playlistStatus = -12;

    public static String currentPlaylistId = "";

    public static File globalDocumentFile = null;

    public boolean isUpdateInProgress = false;

    public boolean isDownloadServiceRunning = false;

    public boolean lastConnectedState = false;

    /*
    When update publish token becomes 1 playlists are downloaded from server and the callback
     */
    public boolean isCheckingPlaylistForFirstTime = true;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        new LoggingExceptionHandler(this);

        //startService(new Intent(AlenkaMedia.this,ApplicationChecker.class));

        Bugfender.init(this, "faYo4K2rJwP3UCNNHxUvwZgVCS7Sa3bk", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();
//        Bugfender.enableLogcatLogging();
//        Bugfender.enableUIEventLogging(this);
    }

    public static synchronized AlenkaMedia getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Toast.makeText(AlenkaMedia.this, "On terminate called.", Toast.LENGTH_SHORT).show();
        isUpdateInProgress = false;
    }
}
