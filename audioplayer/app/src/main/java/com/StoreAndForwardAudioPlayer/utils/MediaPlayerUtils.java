package com.StoreAndForwardAudioPlayer.utils;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

import com.StoreAndForwardAudioPlayer.activities.HomeActivity;
import com.StoreAndForwardAudioPlayer.exomediaplayer.FadingMediaPlayer;
import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.google.android.exoplayer2.SimpleExoPlayer;

/**
 * Created by love on 28/2/18.
 */

public class MediaPlayerUtils {

    private static final String TAG = MediaPlayerUtils.class.getSimpleName();

  public final static long CROSSFADE_DURATION =14000;

    public static void fadeIn(final VideoView videoView, final SimpleExoPlayer exoPlayer,final Context context) {

        final float deviceVolume = 1.0f;

        final Handler h = new Handler();

            h.postDelayed(new Runnable() {

                private float time = 1f;
                private float volume = 1f;

                @Override
                public void run() {

                    boolean isPlayerMuted = false;

                    if (context instanceof HomeActivity){

                        HomeActivity homeActivity = (HomeActivity)context;
                        isPlayerMuted = homeActivity.btnVolume.isSelected();
                    }

                    if (isPlayerMuted){

                        if (videoView != null)
                            videoView.setVolume(0);

                        if (exoPlayer != null)
                            exoPlayer.setVolume(0);


                    }else {

                        time += 1350;
                        volume=1;
                        volume = (deviceVolume * time) / CROSSFADE_DURATION;

                        //Log.e(TAG,"Setting volume for fade in ->" + volume);
                        //Log.e(TAG,"Time value for fade in ->" + time);

                        if (videoView != null)
                            videoView.setVolume(volume);

                        if (exoPlayer != null)
                            exoPlayer.setVolume(volume);

                    }

                    if (time < CROSSFADE_DURATION)
                        h.postDelayed(this, 100);
                }
            }, 100);
    }

    public static void fadeOutForVideoPlayer(final VideoView videoView, final SimpleExoPlayer exoPlayer,final Context context) {
        {

            final float deviceVolume = 1.0f;

            final Handler h = new Handler();

            h.postDelayed(new Runnable() {
                private float time = CROSSFADE_DURATION;
                private float volume = 0.0f;

                @Override
                public void run() {

                    boolean isPlayerMuted = false;

                    if (context instanceof HomeActivity){

                        HomeActivity homeActivity = (HomeActivity)context;
                        isPlayerMuted = homeActivity.btnVolume.isSelected();
                    }

                    if (isPlayerMuted){

                        if (videoView != null)
                            videoView.setVolume(0);

                        if (exoPlayer != null)
                            exoPlayer.setVolume(0);
                    } else {

                        time -= 1500;
                        volume = (deviceVolume * time) / CROSSFADE_DURATION;

                        //Log.e(TAG,"Setting volume for fade out ->" + volume);

                        if (videoView != null)
                            videoView.setVolume(volume);

                        if (exoPlayer != null)
                            exoPlayer.setVolume(volume);
                    }


                    if (time > 0)
                        h.postDelayed(this, 400);  // 1 second delay (takes millis)
                }
            }, 100);

        }
    }


    public static void fadeOutForFadingVideoPlayer(final VideoView videoView, final FadingMediaPlayer exoPlayer, final Context context) {
        {

            final float deviceVolume = 1.0f;

            final Handler h = new Handler();

            h.postDelayed(new Runnable() {
                private float time = CROSSFADE_DURATION;
                private float volume = 0.0f;

                @Override
                public void run() {

                    boolean isPlayerMuted = false;

                    if (context instanceof HomeActivity){

                        HomeActivity homeActivity = (HomeActivity)context;
                        isPlayerMuted = homeActivity.btnVolume.isSelected();
                    }

                    if (isPlayerMuted){

                        if (videoView != null)
                            videoView.setVolume(0);

                        if (exoPlayer != null)
                            exoPlayer.setvolume(0);
                    } else {

                        time -= 1500;
                        volume = (deviceVolume * time) / CROSSFADE_DURATION;

                        //Log.e(TAG,"Setting volume for fade out ->" + volume);

                        if (videoView != null)
                            videoView.setVolume(volume);

                        if (exoPlayer != null)
                            exoPlayer.setvolume(volume);
                    }


                    if (time > 0)
                        h.postDelayed(this, 400);  // 1 second delay (takes millis)
                }
            }, 100);

        }
    }


    public static void fadeOut(final VideoView videoView, final SimpleExoPlayer exoPlayer,final Context context) {
        {

            final float deviceVolume = getDeviceVolume(context);

            final Handler h = new Handler();

                h.postDelayed(new Runnable() {
                    private float time = CROSSFADE_DURATION;
                    private float volume = 0.0f;

                    @Override
                    public void run() {

                        boolean isPlayerMuted = false;

                        if (context instanceof HomeActivity){

                            HomeActivity homeActivity = (HomeActivity)context;
                            isPlayerMuted = homeActivity.btnVolume.isSelected();
                        }

                        if (isPlayerMuted){

                            if (videoView != null)
                                videoView.setVolume(0);

                            if (exoPlayer != null)
                                exoPlayer.setVolume(0);
                        } else {

                            time -= 100;
                            volume = (deviceVolume * time) / CROSSFADE_DURATION;

                            Log.e(TAG,"Setting volume for fade out ->" + volume);

                            if (videoView != null)
                                videoView.setVolume(volume);

                            if (exoPlayer != null)
                                exoPlayer.setVolume(volume);
                        }

                        if (time > 0)
                            h.postDelayed(this, 100);  // 1 second delay (takes millis)
                    }
                }, 100);

            }
    }

    public static float getDeviceVolume(Context context) {


        if (true){

            AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            int volumeLevel = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

            return (float)volumeLevel;
        }

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        int volumeLevel = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        return (float) volumeLevel / maxVolume;
    }
}
