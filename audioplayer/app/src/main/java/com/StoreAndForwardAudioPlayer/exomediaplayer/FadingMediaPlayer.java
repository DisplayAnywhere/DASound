package com.StoreAndForwardAudioPlayer.exomediaplayer;

import android.content.Context;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;

import com.StoreAndForwardAudioPlayer.activities.HomeActivity;
import com.StoreAndForwardAudioPlayer.utils.MediaPlayerUtils;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.io.File;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static com.StoreAndForwardAudioPlayer.activities.HomeActivity.hm;

/**
 * Created by love on 1/3/18.
 */

public class FadingMediaPlayer{

    private static final String TAG = FadingMediaPlayer.class.getSimpleName();

    Context mContext;

    String mSongPath;

    public SimpleExoPlayer exoPlayer;

    MediaSource videoSource;

    FadingMediaPlayerListener fadingMediaPlayerListener;

    private int fadingMediaPlayerSeekCount = 0;

    DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
    DataSource.Factory dataSourceFactory = new EncryptedFileDataSourceFactory(getDecipher(), getSecretSpec(), null, bandwidthMeter);
    ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

    public FadingMediaPlayer(Context context, FadingMediaPlayerListener fadingMediaPlayerListener) {

        this.mContext = context;
        this.fadingMediaPlayerListener = fadingMediaPlayerListener;
        init();
    }

    private void init(){

        try {

            RenderersFactory renderersFactory = new DefaultRenderersFactory(this.mContext,
                    null, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);

            exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, new DefaultTrackSelector());

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Cipher getDecipher(){

        Cipher decipher = null;
        try {

            String username = "bob@google.org";
            String password = "Password1";
            String secretID = "BlahBlahBlah";
            String SALT2 = "deliciously salty";

            byte[] key = (SALT2 + username + password).getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // use only first 128 bit

            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            decipher = Cipher.getInstance("AES");
            decipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        }catch (Exception e){
            e.printStackTrace();
        }
        return decipher;
    }

    private SecretKeySpec getSecretSpec(){

        SecretKeySpec secretKeySpec = null;

        try {

            String username = "bob@google.org";
            String password = "Password1";
            String secretID = "BlahBlahBlah";
            String SALT2 = "deliciously salty";

            byte[] key = (SALT2 + username + password).getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // use only first 128 bit

            secretKeySpec = new SecretKeySpec(key, "AES");

        }catch (Exception e){
            e.printStackTrace();
        }
        return secretKeySpec;
    }

    public  void stop()
    {
        exoPlayer.stop();
    }
    public SimpleExoPlayer load(String path){

        this.mSongPath = path;

        try {

            Uri uri = Uri.fromFile(new File(mSongPath));

            videoSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);

        }catch (Exception e){
            e.printStackTrace();
        }

        return exoPlayer;
    }

    public void setvolume(float vol)
    {
      exoPlayer.setVolume(vol);
    }

    public void play(){

        if (videoSource == null){
            return;
        }

        exoPlayer.prepare(videoSource);
        exoPlayer.setPlayWhenReady(true);
        MediaPlayerUtils.fadeIn(null,exoPlayer,this.mContext);

        mHandler.postDelayed(mRunnable,1000);
    }

    Handler mHandler = new Handler();

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            fadingMediaPlayerSeekCount++;

            if (fadingMediaPlayerSeekCount == (MediaPlayerUtils.CROSSFADE_DURATION/1000) + 1){

                fadingMediaPlayerSeekCount = 0;

                mHandler.removeCallbacks(mRunnable);

                if (fadingMediaPlayerListener != null){
                    fadingMediaPlayerListener.fadingMediaPlayerCompleted(exoPlayer.getCurrentPosition());
                }

                exoPlayer.setVolume(0.70f);
                new CountDownTimer(200, 100) {

                    public void onTick(long millisUntilFinished) {


                    }

                    public void onFinish() {

                      exoPlayer.stop();
                    }
                }.start();


            }


            else {
                mHandler.postDelayed(mRunnable,1000);
            }

        }
    };
    public long getDuration()
    {
        return  exoPlayer.getDuration();
    }


    public long getCurrentpos()
    {
        return exoPlayer.getCurrentPosition();
    }


    public boolean isFadingMediaPlaying(){

        if (exoPlayer == null){
            return false;
        }

        return exoPlayer.getPlayWhenReady();
    }

    public void pause(){

        if (exoPlayer == null){
            return ;
        }

        exoPlayer.setPlayWhenReady(false);
    }

    public void resume(){

        if (exoPlayer == null){
            return ;
        }
        exoPlayer.setPlayWhenReady(true);
    }

    public interface FadingMediaPlayerListener {

        void fadingMediaPlayerCompleted(long currentPosition);
    }
}
