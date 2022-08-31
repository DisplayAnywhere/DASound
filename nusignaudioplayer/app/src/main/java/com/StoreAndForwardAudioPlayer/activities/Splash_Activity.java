package com.StoreAndForwardAudioPlayer.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.StoreAndForwardAudioPlayer.R;
import com.StoreAndForwardAudioPlayer.api_manager.DownloadService;
import com.StoreAndForwardAudioPlayer.api_manager.OkHttpUtil;
import com.StoreAndForwardAudioPlayer.interfaces.PlaylistLoaderListener;
import com.StoreAndForwardAudioPlayer.mediamanager.PlaylistManager;
import com.StoreAndForwardAudioPlayer.utils.AlenkaMediaPreferences;
import com.StoreAndForwardAudioPlayer.utils.ConnectivityReceiver;
import com.StoreAndForwardAudioPlayer.utils.Constants;
import com.StoreAndForwardAudioPlayer.utils.FileUtil;
import com.StoreAndForwardAudioPlayer.utils.FirebaseIDService;
import com.StoreAndForwardAudioPlayer.utils.SharedPreferenceUtil;
import com.StoreAndForwardAudioPlayer.utils.StorageUtils;
import com.StoreAndForwardAudioPlayer.utils.Utilities;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;

/**
 * Created by love on 29/5/17.
 */
public class Splash_Activity extends Activity implements ConnectivityReceiver.ConnectivityReceiverListener,
        OkHttpUtil.OkHttpResponse, PlaylistLoaderListener {

    private static final String TAG = "Splash Activity";

    private final int SPLASH_DISPLAY_LENGTH = 2000;
    private ArrayList<String> arrdeviceid = new ArrayList<String>();
    Context context = Splash_Activity.this;
    public String version;

    TextView txtTokenId,txtVer, txtCurrentTask;
    private FirebaseIDService fb=new FirebaseIDService();
    private String fbidupd="";

    ArrayList<String> permissions = new ArrayList<String>();

    CircularProgressView progressView;

    private boolean isActivityVisible;

    private String m_chosenDir = "";
    private boolean m_newFolderEnabled = true;

    // to find usb/otg
    UsbManager mUsbManager = null;
    IntentFilter filterAttached_and_Detached = null;

    //permission for access usb
   // private static final String ACTION_USB_PERMISSION = "tw.g35gtwcms.android.test.list_usb_otg.USB_PERMISSION";
    private static final String ACTION_USB_PERMISSION  = "com.alenkasmartvideoplayer.list_usb_otg.USB_PERMISSION";



    private final BroadcastReceiver mUsbReceiver  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if(device != null){
                        //
                        Log.e("1","DEATTCHED-" + device);
                    }
                }
            }
//
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        if(device != null){
                            //

                            Log.e("1","ATTACHED-" + device);
                        }
                    }
                    else {
                        PendingIntent mPermissionIntent;
                        mPermissionIntent = PendingIntent.getBroadcast(Splash_Activity.this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
                        mUsbManager.requestPermission(device, mPermissionIntent);

                    }

                }
            }
//
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        if(device != null){
                            //
                            Log.e("1","PERMISSION-" + device);
                        }
                    }

                }
            }

        }
    };

    @Override
    public void tokenUpdatedOnServer() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  moveToFront();
        setContentView(R.layout.activity_splash_);

        Logger.addLogAdapter(new AndroidLogAdapter());

        clearCache();

//        Fabric.with(this, new Crashlytics());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      //  RequestPermission();
        // final ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
        txtTokenId = (TextView) findViewById(R.id.txtTokenId);
        txtTokenId.setTypeface(Utilities.getApplicationTypeface(context));
        txtVer = (TextView) findViewById(R.id.txtVer);
        txtVer.setTypeface(Utilities.getApplicationTypeface(context));
        txtCurrentTask = (TextView) findViewById(R.id.txtCurrentProgress);
        txtCurrentTask.setTypeface(Utilities.getApplicationTypeface(context));

        progressView = (CircularProgressView) findViewById(R.id.progress_view);

        String token = SharedPreferenceUtil.getStringPreference(context, Constants.TOKEN_ID);
        try
        {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(
                    context.getPackageName(), 0);
             version = info.versionName;
        }
        catch (Exception e)
        {
            e.getCause();
        }

       final ActivityManager mActivityManager =(ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);

        if (token.length() > 0){
            txtTokenId.setText("Token ID : " + token);
            txtVer.setText("Version : " +version);
        } else {
            txtTokenId.setText("");
            txtVer.setText("Version : " + version);
        }

//        SharedPreferenceUtil.setBooleanPreference(this.context,Constants.IS_UPDATE_IN_PROGRESS,false);
//        Toast.makeText(Splash_Activity.this, getAndroidVersion(), Toast.LENGTH_LONG).show();

        if (false){


          String pathuuu =  Environment.getExternalStorageDirectory().getAbsolutePath();

            List<StorageUtils.StorageInfo> list = StorageUtils.getStorageList();

            if (list.size() > 1){

                try
                {
                    File root = new File(list.get(1).path, "Notes");
                    if (!root.exists()) {
                        root.mkdirs();
                    }
                    File gpxfile = new File(root, "LOve");
                    FileWriter writer = new FileWriter(gpxfile);
                    writer.append("This is a test file");
                    writer.flush();
                    writer.close();

                   // Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }


                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

                Uri uri = Uri.parse(list.get(1).path);

                intent.setType("video/mp4");
//                intent.setData(uri);
                intent.putExtra(Intent.EXTRA_TITLE, "LOVE");
                startActivityForResult(intent, 11);

                File[] pathsss = getExternalFilesDirs(null);

                if (pathsss.length > 1){

                    File usbDrive = pathsss[1];

                    String pathToUsb = usbDrive.getAbsolutePath();

                    String[] components = pathToUsb.split("/");

                    if (components.length > 2){

                        String storage = components[1];
                        String driveName = components[2];

                       String finalPath = "/" + storage + "/" + driveName + "/" + "AlenkaMedia";
                        File file = new File(finalPath);

                        if (file.exists()){
                            Log.e(TAG,file.getAbsolutePath());
                        }

                    }

                }


                String m_sdcardDirectory = list.get(1).path;

                Log.e(TAG,m_sdcardDirectory);

                String path = m_sdcardDirectory +"/AlenkaMedia";

                Log.e("Files", "Path: " + path);

                File directory = new File(path);

                File[] files = directory.listFiles();

                Log.d("Files", "Size: "+ files.length);

                for (int i = 0; i < files.length; i++)
                {
                    Log.d("Files", "FileName:" + files[i].getName());
                }
            }


            return;
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

       //new AdditionalSongsRemovalTask(Splash_Activity.this).execute();

       stopService(new Intent(Splash_Activity.this, DownloadService.class));
       // RequestPermission();
        if (checkPermissions().size() > 0){
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]),100);
        } else {
            checkUserRights();
        }
    }

    private void RequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent,2323);
            } else {
                //Permission Granted-System will work
            }
        }
    }


    private void clearCache(){
        try {
            File dir = context.getCacheDir();
            FileUtil.deleteDir(dir);
        } catch (Exception e) {}
    }

    public void generateNoteOnSD(String sFileName, String sBody){
        try
        {
            File root = new File(getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
           // Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }


    public String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android SDK: " + sdkVersion + " (" + release +")";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       if (requestCode == 111) {

            String folderLocation = data.getExtras().getString("data");
            Log.i( "folderLocation", folderLocation );
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String [] proj      = {MediaStore.Images.Media.DATA};
        Cursor cursor       = getContentResolver().query( contentUri, proj, null, null,null);
        if (cursor == null) return null;
        int column_index    = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }

    private ArrayList<String> checkPermissions(){

        boolean hasWritePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        boolean hasReadPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasWritePermission){
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!hasReadPermission){
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        return permissions;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions1, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions1, grantResults);

        permissions.clear();

        if (checkPermissions().size() > 0){

            ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]),100);

        }else {
            checkUserRights();
        }

    }

    private void checkUserRights(){

      /*  new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(Splash_Activity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);*/



       if (Utilities.isConnected()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    checkDeviceIdOnServer();

                }

            }, SPLASH_DISPLAY_LENGTH);


        } else {

            String deviceID = SharedPreferenceUtil.getStringPreference(context,AlenkaMediaPreferences.DEVICE_ID);
            if (deviceID.equals("")) {

                showDialogBox(false);

            } else {

                /*Start the app in offline mode.*/
              new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent intent = new Intent(Splash_Activity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, SPLASH_DISPLAY_LENGTH);

            }
        }
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    //res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "";
    }

    public static String getSerialNumber() {
        String serialNumber;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);

            // (?) Lenovo Tab (https://stackoverflow.com/a/34819027/1276306)
            serialNumber = (String) get.invoke(c, "gsm.sn1");

            if (serialNumber.equals(""))
                // Samsung Galaxy S5 (SM-G900F) : 6.0.1
                // Samsung Galaxy S6 (SM-G920F) : 7.0
                // Samsung Galaxy Tab 4 (SM-T530) : 5.0.2
                // (?) Samsung Galaxy Tab 2 (https://gist.github.com/jgold6/f46b1c049a1ee94fdb52)
                serialNumber = (String) get.invoke(c, "ril.serialnumber");

            if (serialNumber.equals(""))
                // Archos 133 Oxygen : 6.0.1
                // Google Nexus 5 : 6.0.1
                // Hannspree HANNSPAD 13.3" TITAN 2 (HSG1351) : 5.1.1
                // Honor 5C (NEM-L51) : 7.0
                // Honor 5X (KIW-L21) : 6.0.1
                // Huawei M2 (M2-801w) : 5.1.1
                // (?) HTC Nexus One : 2.3.4 (https://gist.github.com/tetsu-koba/992373)
                serialNumber = (String) get.invoke(c, "ro.serialno");

            if (serialNumber.equals(""))
                // (?) Samsung Galaxy Tab 3 (https://stackoverflow.com/a/27274950/1276306)
                serialNumber = (String) get.invoke(c, "sys.serialnumber");

            if (serialNumber.equals(""))
                // Archos 133 Oxygen : 6.0.1
                // Hannspree HANNSPAD 13.3" TITAN 2 (HSG1351) : 5.1.1
                // Honor 9 Lite (LLD-L31) : 8.0
                // Xiaomi Mi 8 (M1803E1A) : 8.1.0
                serialNumber = Build.SERIAL;

            // If none of the methods above worked
            if (serialNumber.equals(""))
                serialNumber = "";
        } catch (Exception e) {
            e.printStackTrace();
            serialNumber = null;
        }

        return serialNumber;
    }

    private void checkDeviceIdOnServer(){

        progressView.setVisibility(View.VISIBLE);
        progressView.startAnimation();
        txtCurrentTask.setText("Checking device ID");
        try {
            JSONArray jsondeviceid = new JSONArray();
            arrdeviceid.add(Utilities.getDeviceID(context));
            arrdeviceid.add(Splash_Activity.getMacAddr());
            arrdeviceid.add(Splash_Activity.getSerialNumber());
            for(int i=0;i<arrdeviceid.size();i++)
            {
                JSONObject json = new JSONObject();
                json.put("DeviceId",arrdeviceid.get(i));
                jsondeviceid.put(json);
            }
            //jsondeviceid.put(json);
           // Log.e(TAG,Utilities.getDeviceID(context));

         /*   new OkHttpUtil(context,Constants.CHECK_USER_RIGHTS,json.toString(),
                    Splash_Activity.this,false,
                    Constants.CHECK_USER_RIGHTS_TAG).callRequest();*/

            new OkHttpUtil(context,Constants.CHECK_USER_RIGHTS,jsondeviceid.toString(),
                    Splash_Activity.this,false,
                    Constants.CHECK_USER_RIGHTS_TAG).
                    execute();

        } catch (JSONException e) {
            e.printStackTrace();
            checkDeviceIdOnServer();
        }
    }

    @Override
    public void onResponse(String response,int tag) {

        if (response == null){
           // Utilities.showToast(Splash_Activity.this,"Response returned null");
            return;
        }

        switch (tag){

            case Constants.CHECK_USER_RIGHTS_TAG:{

                handleCheckDeviceIdResponse(response);
            }break;
        }

    }

    @Override
    public void onError(Exception e,int tag) {
        try {

            String f=e.getMessage().toString();
            if (tag == Constants.CHECK_USER_RIGHTS_TAG) {

                String deviceID = SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.DEVICE_ID);
                if (deviceID.equals("")) {

                    showDialogBox(false);

                } else {

                    /*Start the app in offline mode.*/
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Intent intent = new Intent(Splash_Activity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, SPLASH_DISPLAY_LENGTH);

                }

//            checkDeviceIdOnServer();
            }
        }catch (Exception f)
        {
            checkDeviceIdOnServer();
        }
        e.printStackTrace();
    }

    private void updateFirebaseid()
    {
        JSONObject json = new JSONObject();
        try {

            json.put("TokenId",SharedPreferenceUtil.
                    getStringPreference(Splash_Activity.this.context,Constants.TOKEN_ID));
            json.put("fcmId", SharedPreferenceUtil.getStringPreference(Splash_Activity.this,
                    AlenkaMediaPreferences.Firebase_ID));

          //  Log.e(TAG,Utilities.getDeviceID(context));

         /*   new OkHttpUtil(context,Constants.CHECK_USER_RIGHTS,json.toString(),
                    Splash_Activity.this,false,
                    Constants.CHECK_USER_RIGHTS_TAG).callRequest();*/

            new OkHttpUtil(context,Constants.UpdateFcm,json.toString(),
                    Splash_Activity.this,false,
                    Constants.UpdateFcm_TAG).
                    execute();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private void handleCheckDeviceIdResponse(String response){

        try{

            if(response.equals("[]")){

                Intent intent = new Intent(Splash_Activity.this, HomeActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            String Response = new JSONArray(response).getJSONObject(0).getString("Response");
            String Left_Days = new JSONArray(response).getJSONObject(0).getString("LeftDays");
            String TokenID = new JSONArray(response).getJSONObject(0).getString("TokenId");

            int left_days = Integer.parseInt(Left_Days);

            if (Response.equals("1")){

                String Cityid = new JSONArray(response).getJSONObject(0).getString("Cityid");
                String CountryId = new JSONArray(response).getJSONObject(0).getString("CountryId");
                String Schdtype = new JSONArray(response).getJSONObject(0).getString("scheduleType");
                String StateId = new JSONArray(response).getJSONObject(0).getString("StateId");
                String dfClientId = new JSONArray(response).getJSONObject(0).getString("dfClientId");
                String isStopControl = new JSONArray(response).getJSONObject(0).getString("IsStopControl");
                String Firebaseid=new JSONArray(response).getJSONObject(0).getString("FcmId");
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.DEVICE_ID,Utilities.getDeviceID(context));
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.DFCLIENT_ID,dfClientId);
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.TOKEN_ID,TokenID);
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.City_ID,Cityid);
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.Country_ID,CountryId);
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.State_Id,StateId);
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.Is_Stop_Control,isStopControl);
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.Sch_Type, Schdtype);


                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Tokenid",SharedPreferenceUtil.
                            getStringPreference(Splash_Activity.this.context,Constants.TOKEN_ID));

                    new OkHttpUtil(context, Constants.CHECK_TOKEN_PUBLISH,jsonObject.toString(),
                            Splash_Activity.this,false,
                            Constants.CHECK_TOKEN_PUBLISH_TAG).
                            callRequest();


                }catch (Exception e){
                    e.printStackTrace();
                }

                fbidupd=SharedPreferenceUtil.getStringPreference(Splash_Activity.this,
                        AlenkaMediaPreferences.Firebase_ID);
                if(!Firebaseid.equals(fbidupd))
                {
                    updateFirebaseid();
                }
                if (left_days == 1) {
                    new PlaylistManager(context, Splash_Activity.this).getPlaylistsFromServer();

                }
                else if (left_days == 0) {
                    new PlaylistManager(context, Splash_Activity.this).getPlaylistsFromServer();

                }
                else {

                    /*After device id verified, we fetch the playlist and songs and upon completion
                     * it calls finishedGettingSongs() in this activity which takes us to Home screen */

                    new PlaylistManager(context, Splash_Activity.this).getPlaylistsFromServer();
                }
            } else if (Response.equals("0")) {
                if (left_days < 0) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);

                    // set title
                    alertDialogBuilder.setTitle(getResources().getString(R.string.app_name));

                    // set dialog message
                    alertDialogBuilder
                            .setMessage(" Music Player is Expired. Please connect your vendor !!. Your player id: " + TokenID)
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                } else {

                    SharedPreferenceUtil.setStringPreference(context,AlenkaMediaPreferences.TOKEN_ID,"");
                    progressView.setVisibility(View.GONE);
                    progressView.stopAnimation();
                    txtTokenId.setText("");
                    Intent login = new Intent(context,
                            LoginActivity.class);
                    startActivity(login);
                    finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            checkDeviceIdOnServer();

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        isActivityVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityVisible = false;
    }

    private void showDialogBox(boolean isConnected) {

        if (!isConnected) {

            Handler handler = new Handler(Splash_Activity.this.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    AlertDialog alertDialog = new AlertDialog.Builder(
                            Splash_Activity.this).create();

                    // Setting Dialog Title
                    alertDialog.setTitle("Internet Connection Error");

                    // Setting Dialog Message
                    alertDialog.setMessage("Please connect to working Internet connection!");

                    // Setting OK Button
                    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Write your code here to execute after dialog closed
                            finish();
                        }
                    });

                    // Showing Alert Message
                    alertDialog.show();
                }
            });


        }

    }

    @Override
    public void startedGettingPlaylist() {

        try {

            Handler mainHandler = new Handler(context.getMainLooper());

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    progressView.setVisibility(View.VISIBLE);
                    progressView.startAnimation();
                    txtCurrentTask.setText("Syncing songs...");

                }
            };
            mainHandler.post(myRunnable);
        }
        catch (Exception e)
        {
            startedGettingPlaylist();
        }
    }

    @Override
    public void finishedGettingPlaylist() {
        try {

            Handler mainHandler = new Handler(context.getMainLooper());

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    progressView.setVisibility(View.INVISIBLE);
                    progressView.stopAnimation();
                    txtCurrentTask.setText("");

                    startActivity(new Intent(Splash_Activity.this, HomeActivity.class));
                }
            };
            mainHandler.post(myRunnable);
        }
        catch (Exception e)
        {
            startedGettingPlaylist();
        }

    }

    @Override
    public void errorInGettingPlaylist(final Exception e) {

      //  Utilities.showToast(Splash_Activity.this, e.getLocalizedMessage());
        String deviceID = SharedPreferenceUtil.getStringPreference(context,AlenkaMediaPreferences.DEVICE_ID);
        if (deviceID.equals("")) {
            Utilities.showToast(Splash_Activity.this, "Device id Empty");

           // showDialogBox(false);

        } else {

            /*Start the app in offline mode.*/
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent intent = new Intent(Splash_Activity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, SPLASH_DISPLAY_LENGTH);

        }
    }

    @Override
    public void recordSaved(boolean isSaved) {

    }

}
