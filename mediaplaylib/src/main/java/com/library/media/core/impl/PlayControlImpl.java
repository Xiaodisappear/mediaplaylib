package com.library.media.core.impl;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import com.lib.mediaplaylib.R;
import com.library.media.core.CommControl;
import com.library.media.core.MediaPlay;
import com.library.media.core.MediaPlayListener;
import com.library.media.core.MediaPlayWindow;
import com.library.media.core.UIContril;
import com.library.media.utils.BrightnessUtils;
import com.library.media.utils.DisplayUtils;
import com.library.media.utils.NetworkUtils;
import com.library.media.utils.SystemUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xinggenguo on 2/22/17.
 */

public class PlayControlImpl implements CommControl, MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, SurfaceHolder.Callback {

    private String TAG = this.getClass().getSimpleName();

    private int videoWidth = 0;
    private int videoHeight = 0;

    private String videoName;
    private String mVideoUri;

    private MediaPlayer mMediaPlayer;
    private MediaPlayWindow mPlayWindow;
    private MediaPlay.Params mParams;

    private WeakReference<Activity> weakReference;

    private AudioManager mAudioManager;
    private int mCurBrightness;         // 0-255之间的值,
    private int mCurVolume;             // 0-MaxVolume之间的值,
    private int mMaxVolume;

    private UIContril uiContril;

    private CommGestureControl gestureControl;
    private GestureDetector gestureDete;

    private int videoProgress = 0;

    private boolean isMobileNet = false;

    private boolean isNoNet = false;

    private boolean isOriginal;

    private Map<String, MediaPlayListener> playListenerMap;

    private BrightnessChangedObserver mBrightnessChangedObserver = new BrightnessChangedObserver();
    private BroadcastReceiver mVolumeChangedReceiver = new VolumeChangedReceiver();

    public PlayControlImpl(@NonNull MediaPlayWindow playWindow,
                           @NonNull MediaPlay.Params params,
                           @NonNull UIContril uiContril,
                           @NonNull Activity activity) {

        weakReference = new WeakReference<>(activity);

        this.mPlayWindow = playWindow;
        this.mParams = params;

        isOriginal = this.mParams.isOriginal();

        this.uiContril = uiContril;
        this.playListenerMap = new HashMap<>();
        gestureControl = new CommGestureControl(weakReference.get(), this);
        gestureDete = new GestureDetector(weakReference.get(), gestureControl);
        this.uiContril.getContentView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDete.onTouchEvent(event);
                return true;
            }
        });

        playWindow.getPlayHolder().setFixedSize(DisplayUtils.getDeviceWidth(weakReference.get()), DisplayUtils.getDeviceWidth(weakReference.get()) * 9 / 16);
        playWindow.getPlayHolder().addCallback(this);

        mCurBrightness = BrightnessUtils.getSystemScreenBrightness(activity);
        if (params.isDebug()) {
            Log.e(TAG, "mCurBrightness = " + mCurBrightness);
        }

        mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        if (params.isDebug()) {
            Log.i(TAG, "mMaxVolume = " + mMaxVolume + ", mCurVolume = " + mCurVolume);
        }

        listenerVolumeChanged();
        listenerBrightnessChanged();
    }


    public void addMediaplayListener(String key, MediaPlayListener playListener) {

        if (playListenerMap == null) {
            playListenerMap = new HashMap<>();
        }

        if (playListener == null || TextUtils.isEmpty(key)) {
            return;
        }

        playListenerMap.put(key, playListener);

    }

    public void clearMediaplayListener() {

        if (playListenerMap != null) {
            playListenerMap.clear();
            playListenerMap = null;
        }
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
    public void pausePlay() {

        if (getParams().getPlayStatus() < MediaPlay.STATE_BEREADY) {
            return;
        }


        if (getParams().getPlayStatus() == MediaPlay.STATE_PLAYING) {
            togglePlayPause(true);
        }

    }

    @Override
    public void resumePlay() {

        if (getParams().getPlayStatus() < MediaPlay.STATE_BEREADY) {
            return;
        }

        if (getParams().getPlayStatus() == MediaPlay.STATE_PAUSED
                || getParams().getPlayStatus() == MediaPlay.STATE_PLAYBACK_COMPLETED) {
            togglePlayPause(false);
        }

    }

    @Override
    public void toggleController(boolean isForceVisible) {

        if (uiContril != null) {
            if (isForceVisible) {
                uiContril.showProgress();
                return;
            }
            if (uiContril.isControllerBarShowing()) {
                uiContril.hideProgress();
            } else {
                uiContril.showProgress();
            }
        }

    }

    @Override
    public void togglePlayPause(boolean isForcePause) {


        if (isForcePause) {
            mMediaPlayer.pause();
            mParams.setPlayStatus(MediaPlay.STATE_PAUSED);
            uiContril.clearUpdateProgress();
            return;
        }

        if (mParams.getPlayStatus() == MediaPlay.STATE_PLAYBACK_COMPLETED) {
            mMediaPlayer.seekTo(0);
            mMediaPlayer.start();
            mParams.setPlayStatus(MediaPlay.STATE_PLAYING);
            uiContril.startUpdateProgress();
            uiContril.hideProgress();
        } else if (mParams.getPlayStatus() == MediaPlay.STATE_PLAYING) {
            mMediaPlayer.pause();
            mParams.setPlayStatus(MediaPlay.STATE_PAUSED);
            uiContril.clearUpdateProgress();
        } else if (mParams.getPlayStatus() == MediaPlay.STATE_BEREADY) {
            mMediaPlayer.start();
            mParams.setPlayStatus(MediaPlay.STATE_PLAYING);
            uiContril.startUpdateProgress();
            uiContril.hideProgress();
        } else if (mParams.getPlayStatus() == MediaPlay.STATE_PAUSED) {
            mMediaPlayer.start();
            mParams.setPlayStatus(MediaPlay.STATE_PLAYING);
            uiContril.startUpdateProgress();
            uiContril.hideProgress();
        }

        if (mParams.getPlayStatus() == MediaPlay.STATE_PLAYING
                && videoWidth > 0 && videoHeight > 0) {
            int adjustWidth = videoWidth;
            int adjustHeight = videoHeight;
            if (!isOriginal) {
                adjustWidth = DisplayUtils.getDeviceWidth(weakReference.get());
                adjustHeight = DisplayUtils.getDeviceHeight(weakReference.get());
            }

            adjustFixSize(adjustWidth, adjustHeight);

        }
    }

    private void adjustFixSize(int width, int height) {

        int widthScreen = DisplayUtils.getDeviceWidth(weakReference.get());
        float scal = (float) widthScreen / (float) width;
        mPlayWindow.adjustFixSize(widthScreen, (int) ((float) height * scal));
        uiContril.adjustFixWH(widthScreen, (int) ((float) height * scal));

    }

    @Override
    public void setControllerEnable(boolean isEnable) {

    }

    @Override
    public void onHorizontalScroll(MotionEvent event, float delta, float distanceX) {

        if (event.getPointerCount() == 1 && mParams.isAdjustProgress()) {
            Log.i(TAG, "onHorizontalScroll delta = " + delta + ", distanceX = " + distanceX);
            final int timeAppend = Math.round(delta * 1000 / CommGestureControl.SLIDE_HORIZONTAL_THRESHOLD_LIMITE);
            togglePlayPause(true);

            if (videoProgress == 0) {
                videoProgress = mMediaPlayer.getCurrentPosition();
            }

            if (delta > 0) {
                Log.i(TAG, ">>>>>FastForward"); // 快进
                videoProgress += timeAppend;

                if (videoProgress > 0 && videoProgress >= mMediaPlayer.getDuration()) {
                    videoProgress = mMediaPlayer.getDuration();
                }
                if (uiContril != null) {
                    uiContril.onFastForward(Math.abs(timeAppend), videoProgress);
                }
            } else {
                Log.i(TAG, "<<<<<Rewind"); // 快退
                videoProgress -= Math.abs(timeAppend);

                if (videoProgress < 1000) {
                    videoProgress = 0;
                }

                if (uiContril != null) {
                    uiContril.onRewind(Math.abs(timeAppend), videoProgress);
                }
            }
        }

    }

    @Override
    public void onVerticalScroll(MotionEvent motionEvent, float delta, float distanceY, int direction) {
        if (motionEvent.getPointerCount() == 1) {
            Log.d(TAG, "onVerticalScroll delta = " + delta + ", distanceY = " + distanceY + ", delta = " + delta);
            if (direction == CommGestureControl.SLIDE_LEFT) {
                final int brightness = mCurBrightness + Math.round(delta / DisplayUtils.getDeviceHeight(weakReference.get()) * 255f);
                Log.i(TAG, "updateBrightness brightness = " + brightness); // 亮度变化
                onBrightnessAdjust(brightness);
            } else {
                final int volume = mCurVolume + (int) (delta * mMaxVolume / DisplayUtils.getDeviceHeight(weakReference.get()));
                Log.i(TAG, "updateVolume volume = " + volume); // 音量变化
                onVolumeAdjust(volume);
            }
        }
    }

    @Override
    public void onDestroy() {

        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.setDisplay(null);
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            if (weakReference != null) {
                weakReference.clear();
                weakReference = null;
            }

            clearMediaplayListener();

            unregisterListener();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

    }

    @Override
    public void onCtrlStart() {
        updateCtrlInfo();
    }

    @Override
    public void onCtrlEnd() {
        updateCtrlInfo();

        if (getParams().getPlayStatus() == MediaPlay.STATE_PAUSED) {
            try {
                mMediaPlayer.seekTo(videoProgress);
                mMediaPlayer.start();
                mParams.setPlayStatus(MediaPlay.STATE_PLAYING);
                uiContril.startUpdateProgress();
            } catch (Exception e) {
                Log.e(TAG, "onCtrlEnd", e);
                e.printStackTrace();
            }
        }

        videoProgress = 0;
    }

    private void updateCtrlInfo() {
        // 更新为当前亮度值
        mCurBrightness = BrightnessUtils.getWindowScreenBrightness(weakReference.get());
        // 更新为当前音量值
        mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (mParams.isDebug()) {
            Log.i(TAG, "updateCtrlInfo mCurBrightness = " + mCurBrightness + ", mCurVolume = " + mCurVolume);
        }
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
                togglePlayPause(false);
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mParams.setPlayStatus(MediaPlay.STATE_PLAYBACK_COMPLETED);
        uiContril.clearUpdateProgress();
        uiContril.showProgress();
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

        if (!mParams.isAdjustVoice()) {
            return;
        }

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

        if (!mParams.isAdjustLight()) {
            return;
        }

        if (mCurBrightness != brightness) {
            BrightnessUtils.setBrightness(weakReference.get(), brightness);
        }
        final int percent = (int) (brightness * 100 / 255f);
        uiContril.updateBrightnessLayer(percent);
    }

    @Override
    public void startPlay(String videoName, String videoUri) {

        this.videoName = videoName;
        this.mVideoUri = videoUri;

        if (mParams.isDebug()) {
            Log.d(TAG, "tryPlayVideo");
        }
        if (mVideoUri == null || mPlayWindow == null) {
            if (mParams.isDebug()) {
                Log.d(TAG, "tryPlayVideo return");
            }
            return;
        }
        showMobileNetHint();
        release();
        initMediaPlayer();
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(videoUri);
            mMediaPlayer.prepareAsync();
        } catch (IOException ex) {
            if (mParams.isDebug()) {
                Log.e(TAG, "IOException: " + mVideoUri, ex);
            }
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            if (mParams.isDebug()) {
                Log.e(TAG, "Unable to open content: " + mVideoUri, ex);
            }
            ex.printStackTrace();
        }

    }

    @Override
    public void switchOriginal() {

        if (isOriginal) {
            isOriginal = false;
            mPlayWindow.full();
            // 竖屏--->横屏
            if (DisplayUtils.isScreenPortrait(weakReference.get())) {
                weakReference.get().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                adjustFixSize(DisplayUtils.getDeviceWidth(weakReference.get()), DisplayUtils.getDeviceHeight(weakReference.get()));
            }
        } else {
            if (DisplayUtils.isScreenLandscape(weakReference.get())) {
                isOriginal = true;
                mPlayWindow.original();
                weakReference.get().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                adjustFixSize(videoWidth, videoHeight);
            }

        }
    }

    @Override
    public boolean isFullScreen() {
        return !isOriginal;
    }

    @Override
    public MediaPlayer getPlayer() {
        if (mMediaPlayer != null) {
            return mMediaPlayer;
        }
        return null;
    }

    @Override
    public MediaPlay.Params getParams() {
        return mParams;
    }

    @Override
    public void sekTo(int progress) {

        if (mMediaPlayer != null
                && mParams.getPlayStatus() >= MediaPlay.STATE_BEREADY) {
            mMediaPlayer.seekTo(progress);
        }

    }

    private void release() {
        mParams.setPlayStatus(MediaPlay.STATE_IDLE);
    }

    public void showMobileNetHint() {
        if (mParams.isDebug()) {
            Log.d(TAG, "before isMobileNet:" + mParams.isTipNoWifi());
        }

        if (!SystemUtils.isExistWeakReferenceActivity(weakReference)) {
            if (mParams.isDebug()) {
                Log.d(TAG, "当前对象不存在");
            }
            return;
        }

        if (mMediaPlayer != null && NetworkUtils.isNetworkAvailable(weakReference.get())) {
            if (NetworkUtils.isMobileNetwork(weakReference.get())) { // mobile
                isMobileNet = true;
            } else {
                isMobileNet = false;
            }
            if (mParams.isTipNoWifi() && isMobileNet) {
                Toast.makeText(weakReference.get(), R.string.mobile_net_hint, Toast.LENGTH_LONG).show();
            }
        } else {
            isMobileNet = false;
            isNoNet = true;
        }
        if (mParams.isDebug()) {
            Log.d(TAG, "after isMobileNet:" + isMobileNet + " | netuseable:" + isNoNet);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initMediaPlayer();
    }

    private void initMediaPlayer() {
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();

                mMediaPlayer.setDisplay(mPlayWindow.getPlayHolder());
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setOnBufferingUpdateListener(this);
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setOnInfoListener(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

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
