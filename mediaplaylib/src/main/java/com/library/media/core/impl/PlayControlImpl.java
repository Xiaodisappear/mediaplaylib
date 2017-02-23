package com.library.media.core.impl;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;

import com.library.media.core.CommControl;
import com.library.media.core.MediaPlay;
import com.library.media.core.MediaPlayWindow;
import com.library.media.core.UIContril;
import com.library.media.utils.BrightnessUtils;
import com.library.media.utils.SystemUtils;

import java.lang.ref.WeakReference;

/**
 * Created by xinggenguo on 2/22/17.
 */

public class PlayControlImpl implements CommControl, MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {

    private String TAG = this.getClass().getSimpleName();

    private int videoWidth = 0;
    private int videoHeight = 0;

    private MediaPlayer mMediaPlayer;
    private MediaPlayWindow mPlayWindow;
    private MediaPlay.Params mParams;

    private WeakReference<Activity> weakReference;

    private AudioManager mAudioManager;
    private int mCurBrightness;         // 0-255之间的值,
    private int mCurVolume;             // 0-MaxVolume之间的值,
    private int mMaxVolume;

    private UIContril uiContril;

    private BrightnessChangedObserver mBrightnessChangedObserver = new BrightnessChangedObserver();
    private BroadcastReceiver mVolumeChangedReceiver = new VolumeChangedReceiver();

    public PlayControlImpl(@NonNull MediaPlayWindow playWindow,
                           @NonNull MediaPlay.Params params,
                           @NonNull UIContril uiContril,
                           @NonNull Activity activity) {

        weakReference = new WeakReference<Activity>(activity);

        this.mMediaPlayer = new MediaPlayer();
        this.mPlayWindow = playWindow;
        this.mParams = params;

        mMediaPlayer.setDisplay(playWindow.getHolder());
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnInfoListener(this);

        this.uiContril = uiContril;

        mCurBrightness = BrightnessUtils.getSystemScreenBrightness(activity);
        if (params.isDebug()) {
            Log.e(TAG, "mCurBrightness = " + mCurBrightness);
        }

        listenerBrightnessChanged();

        mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        if (params.isDebug()) {
            Log.i(TAG, "mMaxVolume = " + mMaxVolume + ", mCurVolume = " + mCurVolume);
        }
        listenerVolumeChanged();
    }


    /**
     * 注册监听系统亮度改变事件
     */
    private void listenerBrightnessChanged() {

        if (!SystemUtils.isExistWeakReferenceActivity(weakReference)) {
            return;
        }

        weakReference.get().getContentResolver().registerContentObserver(Settings.System.getUriFor(
                Settings.System.SCREEN_BRIGHTNESS), true, mBrightnessChangedObserver);
    }

    /**
     * 注册监听系统声音变化
     */
    private void listenerVolumeChanged() {

        if (!SystemUtils.isExistWeakReferenceActivity(weakReference)) {
            return;
        }

        IntentFilter filter = new IntentFilter(VolumeChangedReceiver.VOLUME_CHANGED_ACTION);
        weakReference.get().registerReceiver(mVolumeChangedReceiver, filter);
    }

    private void unregisterListener() {

        if (!SystemUtils.isExistWeakReferenceActivity(weakReference)) {
            return;
        }

        weakReference.get().getContentResolver().unregisterContentObserver(mBrightnessChangedObserver);
        weakReference.get().unregisterReceiver(mVolumeChangedReceiver);
    }


    @Override
    public void toggleController(boolean isForceVisible) {
        uiContril.toggleController(isForceVisible);
    }

    @Override
    public void togglePlayPause() {

        if (mParams.getPlayStatus() >= MediaPlay.STATE_PAUSED) {
            mMediaPlayer.reset();
            mMediaPlayer.start();
            mParams.setPlayStatus(MediaPlay.STATE_PLAYING);
        } else if (mParams.getPlayStatus() == MediaPlay.STATE_PLAYING) {
            mMediaPlayer.stop();
            mParams.setPlayStatus(MediaPlay.STATE_PAUSED);
        } else if (mParams.getPlayStatus() == MediaPlay.STATE_BEREADY) {
            mMediaPlayer.start();
            mParams.setPlayStatus(MediaPlay.STATE_PLAYING);
        }

        if (mParams.getPlayStatus() == MediaPlay.STATE_PLAYING
                && videoWidth > 0 && videoHeight > 0) {

            mPlayWindow.adjustFixSize(videoWidth, videoHeight);
        }

    }

    @Override
    public void setControllerEnable(boolean isEnable) {

    }

    @Override
    public void onHorizontalScroll(MotionEvent event, float delta, float distanceX) {

    }

    @Override
    public void onVerticalScroll(MotionEvent motionEvent, float delta, float distanceY, int direction) {

    }

    @Override
    public void onDestroy() {

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }

        if (weakReference != null) {
            weakReference.clear();
            weakReference = null;
        }

        unregisterListener();

    }

    @Override
    public void onCtrlStart() {

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        int height = mp.getVideoHeight();
        int width = mp.getVideoWidth();

        if (height > 0 && width > 0) {
            videoHeight = height;
            videoWidth = width;

            mParams.setPlayStatus(MediaPlay.STATE_BEREADY);

            if (mParams.isAutoPlay()) {
                togglePlayPause();
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mParams.setPlayStatus(MediaPlay.STATE_PLAYBACK_COMPLETED);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        if (mParams.isDebug()) {
            Log.i(TAG, "播放错误:" + String.valueOf(extra) + what);
        }
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                break;
            case MediaPlayer.MEDIA_ERROR_IO:
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                break;
        }
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == mp.MEDIA_INFO_BUFFERING_START) {
            if (mParams.isDebug()) {
                Log.i(TAG, "开始缓冲");
            }
            return true;
        } else if (what == mp.MEDIA_INFO_BUFFERING_END) {
            if (mParams.isDebug()) {
                Log.i(TAG, "缓冲结束");
            }
            return true;
        }
        return false;
    }


    @Override
    public void onVolumeAdjust(int volume) {

        if (volume > mMaxVolume) {
            volume = mMaxVolume;
        }

        if (volume < 0) {
            volume = 0;
        }

        if (mCurVolume != volume) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        }
        final int percent = Math.round((volume * 1.0f / mMaxVolume) * 100);
        uiContril.updateVolumeLayer(percent);
    }

    @Override
    public void onBrightnessAdjust(int brightness) {

        if (mCurBrightness != brightness) {
            BrightnessUtils.setBrightness(weakReference.get(), brightness);
        }
        final int percent = (int) (brightness * 100 / 255f);
        uiContril.updateBrightnessLayer(percent);
    }

    /**
     * 系统音量变化事件
     */
    private class VolumeChangedReceiver extends BroadcastReceiver {

        public final static String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";

        private int lastVolume = 0;

        @Override
        public void onReceive(Context context, Intent intent) {

            if (mParams.isDebug()) {
                Log.d(TAG, "VolumeChangedReceiver Action : " + intent.getAction());
            }

            final int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (lastVolume == curVolume) {
                return;
            }
            lastVolume = curVolume;
            onVolumeAdjust(curVolume);

        }

    }

    /**
     * 时刻监听系统亮度改变事件
     */
    private class BrightnessChangedObserver extends ContentObserver {

        public BrightnessChangedObserver() {
            super(new Handler());
        }

        private int lastBrightness = 0;

        @Override
        public void onChange(boolean selfChange) {

            if (!SystemUtils.isExistWeakReferenceActivity(weakReference)) {
                return;
            }

            if (mParams.isDebug()) {
                Log.d(TAG, "BrightnessChangedObserver selfChange : " + selfChange);
            }

            if (selfChange) {
                // nothing to do
            } else {
                final int changedBrightness = BrightnessUtils.getWindowScreenBrightness(weakReference.get());
                if (lastBrightness == changedBrightness) {
                    return;
                }
                lastBrightness = changedBrightness;
                onBrightnessAdjust(changedBrightness);
            }

        }
    }
}
