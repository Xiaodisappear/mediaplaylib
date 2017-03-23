package com.library.media.core.impl;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.annotation.RequiresApi;
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
import com.library.media.ui.FeelPlayWindow;
import com.library.media.ui.dialog.MaterialDialog;
import com.library.media.ui.dialog.MaterialDialogBuilder;
import com.library.media.utils.BrightnessUtils;
import com.library.media.utils.DisplayUtils;
import com.library.media.utils.FeelLog;
import com.library.media.utils.NetworkUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by xinggenguo on 2/22/17.
 */

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class PlayControlImpl implements CommControl, MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, SurfaceHolder.Callback {

    private String TAG = this.getClass().getSimpleName();

    private static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    private int videoWidth = 0;
    private int videoHeight = 0;

    private String videoName;
    private String mVideoUri;

    private MediaPlayer mMediaPlayer;
    private MediaPlayWindow mPlayWindow;
    private MediaPlay.Params mParams;

    private Activity mActivity;

    private AudioManager mAudioManager;
    private int mCurBrightness;         // 0-255之间的值,
    private int mCurVolume;             // 0-MaxVolume之间的值,
    private int mMaxVolume;

    private UIContril uiContril;

    private CommGestureControl gestureControl;
    private GestureDetector gestureDete;

    private int videoProgress = 0;
    private int adjustVideoProgress = 0;

    private boolean isMobileNet = false;

    private boolean isNoNet = false;

    private boolean isOriginal;

    private boolean downAdjust = false;

    private boolean surfaceCrate = false;

    private boolean haveSurfaceDestory = false;

    private boolean haveRegister = false;

    private boolean havaTip = false;

    private Dialog mNetworkTipDialog;

    private Map<String, MediaPlayListener> playListenerMap;

    private BrightnessChangedObserver mBrightnessChangedObserver = new BrightnessChangedObserver();
    private BroadcastReceiver mVolumeChangedReceiver = new VolumeChangedReceiver();
    private BroadcastReceiver networkchangesReceiver = new NetworkchangesReceiver();

    private boolean isInit = false;

    public PlayControlImpl(@NonNull MediaPlayWindow playWindow,
                           @NonNull MediaPlay.Params params,
                           @NonNull UIContril uiContril,
                           @NonNull Activity activity) {

        mActivity = activity;

        this.mPlayWindow = playWindow;
        this.mParams = params;

        this.havaTip = false;

        isOriginal = this.mParams.isOriginal();

        this.uiContril = uiContril;
        this.haveSurfaceDestory = false;
        this.playListenerMap = new HashMap<>();
        gestureControl = new CommGestureControl(mActivity, this);
        gestureDete = new GestureDetector(mActivity, gestureControl);
        this.uiContril.getContentView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDete.onTouchEvent(event);
                return true;
            }
        });

        haveRegister = false;

        playWindow.getPlayHolder().setFixedSize(DisplayUtils.getDeviceWidth(mActivity), DisplayUtils.getDeviceWidth(mActivity) * 9 / 16);
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

        if (!isInit) {
            if (mParams.isAdjustVoice()) {
                listenerVolumeChanged();
            }

            if (mParams.isAdjustLight()) {
                listenerBrightnessChanged();
            }

            if (mParams.isTipNoWifi()) {
                listenerNetworkTransReceiver();
            }

            if (mParams.isSwitchGravity()) {
                listenerGravitySwitch();
            }
        }

        isInit = true;


    }

    private void listenerGravitySwitch() {


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

    private void sendScreenChange(boolean isOriginal) {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction("SCREEN_CHANGE_ACTION");
        mActivity.sendBroadcast(intent);
    }

    private boolean needResume = false;

    @Override
    public void onResume() {

        uiContril.checkPlayWindow();

        if ((mParams != null
                && mParams.getPlayStatus() == MediaPlay.STATE_PAUSED) || needResume) {
            togglePlayPause(false, false);
            needResume = false;
        }
    }

    @Override
    public void onPause() {

        if (mParams != null
                && mParams.getPlayStatus() == MediaPlay.STATE_PLAYING) {
            needResume = true;
            togglePlayPause(true, false);
        }

    }

    @Override
    public int getVideoDurationTime() {
        if (mMediaPlayer != null && mParams.getPlayStatus() >= MediaPlay.STATE_BEREADY) {
            return mMediaPlayer.getDuration();
        } else {
            return -1;
        }
    }

    @Override
    public int getVideoCurrentTime() {
        if (mMediaPlayer != null && mParams.getPlayStatus() >= MediaPlay.STATE_BEREADY) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return -1;
        }
    }

    @Override
    public void reStartPlay() {
        if (mMediaPlayer != null && mParams.getPlayStatus() >= MediaPlay.STATE_BEREADY) {
            mMediaPlayer.seekTo(0);
            mMediaPlayer.start();
            onCallStartPlay(CALL_START);
            mParams.setPlayStatus(MediaPlay.STATE_PLAYING);
            uiContril.startUpdateProgress();
            uiContril.hideProgress();
            uiContril.onBeReady();
        }
    }

    @Override
    public boolean isPlayWindowDestory() {
        return haveSurfaceDestory;
    }


    /**
     * 注册监听系统亮度改变事件
     */
    private void listenerBrightnessChanged() {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        mActivity.getContentResolver().registerContentObserver(Settings.System.getUriFor(
                Settings.System.SCREEN_BRIGHTNESS), true, mBrightnessChangedObserver);
    }

    /**
     * 注册网络广播监听
     */
    private void listenerNetworkTransReceiver() {

        if (haveRegister) {
            return;
        }

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(CONNECTIVITY_CHANGE_ACTION);
            filter.setPriority(1000);
            mActivity.registerReceiver(networkchangesReceiver, filter);
        } catch (Exception e) {
            FeelLog.e(e);
        }
        haveRegister = true;
    }

    /**
     * 注册监听系统声音变化
     */
    private void listenerVolumeChanged() {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        try {
            IntentFilter filter = new IntentFilter(VolumeChangedReceiver.VOLUME_CHANGED_ACTION);
            mActivity.registerReceiver(mVolumeChangedReceiver, filter);
        } catch (Exception e) {
            FeelLog.e(e);
        }
    }

    private void unregisterListener() {

        try {
            if (mActivity == null || mActivity.isFinishing()) {
                return;
            }

            mActivity.unregisterReceiver(networkchangesReceiver);
            mActivity.unregisterReceiver(mVolumeChangedReceiver);
            mActivity.getContentResolver().unregisterContentObserver(mBrightnessChangedObserver);
        } catch (Exception e) {
            FeelLog.e(e);
        }
    }


    @Override
    public void pausePlay() {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        if (getParams().getPlayStatus() < MediaPlay.STATE_BEREADY) {
            return;
        }


        if (getParams().getPlayStatus() == MediaPlay.STATE_PLAYING) {
            togglePlayPause(true, false);
        }

    }

    @Override
    public void resumePlay() {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        if (getParams().getPlayStatus() < MediaPlay.STATE_BEREADY) {
            return;
        }

        if (getParams().getPlayStatus() == MediaPlay.STATE_PAUSED
                || getParams().getPlayStatus() == MediaPlay.STATE_PLAYBACK_COMPLETED) {
            togglePlayPause(false, true);
        }

    }

    @Override
    public void toggleController(boolean isForceVisible) {

        if (uiContril != null) {
            uiContril.toggleController(isForceVisible);
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
    public void togglePlayPause(boolean isForcePause, boolean isForceResume) {

        if (mMediaPlayer == null) {
            return;
        }

        if (isForcePause) {
            onCallStartPlay(CALL_PAUSE);
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
            mParams.setPlayStatus(MediaPlay.STATE_PAUSED);
            uiContril.clearUpdateProgress();
            return;
        }

        if (isForceResume) {
            mMediaPlayer.start();
            mParams.setPlayStatus(MediaPlay.STATE_PLAYING);
            uiContril.startUpdateProgress();
            uiContril.hideProgress();
            onCallStartPlay(CALL_RESUME);
            return;
        }

        if (mParams.getPlayStatus() == MediaPlay.STATE_PLAYBACK_COMPLETED) {
            mMediaPlayer.seekTo(0);
            mMediaPlayer.start();
            onCallStartPlay(CALL_START);
            mParams.setPlayStatus(MediaPlay.STATE_PLAYING);
            uiContril.startUpdateProgress();
            uiContril.hideProgress();
        } else if (mParams.getPlayStatus() == MediaPlay.STATE_PLAYING) {
            onCallStartPlay(CALL_PAUSE);
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
            mParams.setPlayStatus(MediaPlay.STATE_PAUSED);
            uiContril.clearUpdateProgress();
        } else if (mParams.getPlayStatus() == MediaPlay.STATE_BEREADY) {
            mMediaPlayer.start();
            onCallStartPlay(CALL_START);
            mParams.setPlayStatus(MediaPlay.STATE_PLAYING);
            uiContril.startUpdateProgress();
            uiContril.hideProgress();
        } else if (mParams.getPlayStatus() == MediaPlay.STATE_PAUSED) {
            mMediaPlayer.start();
            mParams.setPlayStatus(MediaPlay.STATE_PLAYING);
            uiContril.startUpdateProgress();
            uiContril.hideProgress();
            onCallStartPlay(CALL_RESUME);
        }

        if (mParams.getPlayStatus() == MediaPlay.STATE_PLAYING
                && videoWidth > 0 && videoHeight > 0) {
            adjustFixSize();
        }
    }

    private synchronized void adjustFixSize() {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        if (isOriginal) {
            adjustOriginalSize();
        } else {
            adjustLandscapeSize();
        }

    }

    public void adjustOriginalSize() {

        int width = videoWidth;
        int height = videoHeight;

        int widthScreen = DisplayUtils.getDeviceWidth(mActivity);
        int heightScreen = 0;

        float scal = (float) widthScreen / (float) width;
        heightScreen = (int) ((float) height * scal);

        mPlayWindow.adjustFixSize(widthScreen, heightScreen);
        uiContril.adjustFixWH(widthScreen, heightScreen);

    }

    public void adjustLandscapeSize() {

        int height = DisplayUtils.getDeviceHeight(mActivity);

        int widthScreen = DisplayUtils.getDeviceWidth(mActivity);
        int heightScreen = 0;

        if (mParams.isTitleBarVisible() && height == DisplayUtils.getDeviceHeight(mActivity)) {
            height -= DisplayUtils.getStatusBarHeight(mActivity);
        }

        heightScreen = height;

        mPlayWindow.adjustFixSize(widthScreen, heightScreen);
        uiContril.adjustFixWH(widthScreen, heightScreen);


    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    public void onHorizontalScroll(MotionEvent event, float delta) {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        if (event.getPointerCount() == 1 && mParams.isAdjustProgress()) {
            Log.i(TAG, "onHorizontalScroll delta = " + delta);
            final int timeAppend = Math.round(delta * 1000 / CommGestureControl.SLIDE_HORIZONTAL_THRESHOLD_LIMITE);
            togglePlayPause(true, false);
            if (delta > 0) {
                Log.i(TAG, ">>>>>FastForward"); // 快进

                adjustVideoProgress = videoProgress + timeAppend;
//                videoProgress += timeAppend;

                if (adjustVideoProgress > 0 && adjustVideoProgress >= mMediaPlayer.getDuration()) {
                    adjustVideoProgress = mMediaPlayer.getDuration();
                }
                if (uiContril != null) {
                    uiContril.onFastForward(Math.abs(timeAppend), adjustVideoProgress);
//                    uiContril.onFastForward(Math.abs(timeAppend), videoProgress);
                }
            } else {
                Log.i(TAG, "<<<<<Rewind"); // 快退

//                videoProgress -= Math.abs(timeAppend);
                adjustVideoProgress = videoProgress - Math.abs(timeAppend);

                if (adjustVideoProgress < 1000) {
                    adjustVideoProgress = 0;
                }

                if (uiContril != null) {
                    uiContril.onRewind(Math.abs(timeAppend), adjustVideoProgress);
                }
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    public void onVerticalScroll(MotionEvent motionEvent, float delta, int direction) {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        if (motionEvent.getPointerCount() == 1) {
            Log.d(TAG, "onVerticalScroll delta = " + delta);
            if (direction == CommGestureControl.SLIDE_LEFT) {
                final int brightness = mCurBrightness + Math.round(delta / DisplayUtils.getDeviceHeight(mActivity) * 255f);
                Log.i(TAG, "updateBrightness brightness = " + brightness); // 亮度变化
                onBrightnessAdjust(brightness);
            } else {
                final int volume = mCurVolume + (int) (delta * mMaxVolume / DisplayUtils.getDeviceHeight(mActivity));
                Log.i(TAG, "updateVolume volume = " + volume); // 音量变化
                onVolumeAdjust(volume);
            }
        }
    }

    @Override
    public void onDestroy() {

        try {
            isInit = false;
            unregisterListener();
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.setDisplay(null);
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            if (mActivity != null) {
                mActivity = null;
                return;
            }

            if (uiContril != null) {
                uiContril.onDestory();
            }

            if (mParams != null) {
                mParams = null;
            }

            clearMediaplayListener();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

    }

    @Override
    public void onCtrlStart() {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        updateCtrlInfo();
        if (!downAdjust && mMediaPlayer != null) {
            downAdjust = false;
            videoProgress = mMediaPlayer.getCurrentPosition();
            adjustVideoProgress = 0;
        }
        downAdjust = true;
    }

    @Override
    public void onCtrlEnd() {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        updateCtrlInfo();

        if (getParams().getPlayStatus() == MediaPlay.STATE_PAUSED) {
            try {
                mMediaPlayer.seekTo(adjustVideoProgress);
                mMediaPlayer.start();
                mParams.setPlayStatus(MediaPlay.STATE_PLAYING);
                uiContril.startUpdateProgress();
            } catch (Exception e) {
                Log.e(TAG, "onCtrlEnd", e);
                e.printStackTrace();
            }
        }

        downAdjust = false;
        videoProgress = 0;
        adjustVideoProgress = 0;
    }

    private void updateCtrlInfo() {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        // 更新为当前亮度值
        mCurBrightness = BrightnessUtils.getWindowScreenBrightness(mActivity);
        // 更新为当前音量值

        if (mAudioManager != null) {
            mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
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

            if (isResumePlay) {
                isResumePlay = false;
                long duration = System.currentTimeMillis() - resumeDuration;
                if (duration + resumeProgress < mp.getDuration()) {
                    mp.seekTo((int) (resumeProgress + duration));
                } else {
                    mp.seekTo(resumeProgress);
                }
                if (mParams.getPlayStatus() == MediaPlay.STATE_PLAYING) {
                    mp.start();
                } else {
                    if (mp.isPlaying()) {
                        mp.pause();
                    }
                }
                resumeDuration = 0;
                return;
            }

            if (errorResmePosition > 0) {
                mp.seekTo(errorResmePosition);
                if (mParams.getPlayStatus() == MediaPlay.STATE_PLAYING) {
                    mp.start();
                } else {
                    if (mp.isPlaying()) {
                        mp.pause();
                    }
                }
                errorResmePosition = 0;
                uiContril.onBufferEnd();
                return;
            }

            mParams.setPlayStatus(MediaPlay.STATE_BEREADY);

            if (uiContril != null) {
                uiContril.onBeReady();
            }

            dealNetChange();

        }
    }

    private void dealNetChange() {

        if (showMobileNetHint() && !havaTip) {
            havaTip = true;
        } else {
            if (mParams.isAutoPlay()) {
                togglePlayPause(false, false);
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        mParams.setPlayStatus(MediaPlay.STATE_PLAYBACK_COMPLETED);
        uiContril.clearUpdateProgress();
        uiContril.showProgress();
        onCallStartPlay(CALL_COMPLETE);
        uiContril.onPlayComplete();
    }

    private int errorResumeCount = 0;
    private int errorResmePosition = 0;

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                FeelLog.i("发生未知错误");
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                FeelLog.i("媒体服务器死机");
                break;
            default:
                FeelLog.i("onWhatError+" + what);
                break;
        }
        switch (extra) {
            case MediaPlayer.MEDIA_ERROR_IO:
                //io读写错误
                FeelLog.i("文件或网络相关的IO操作错误");
                errorResmePosition = mp.getCurrentPosition();
                errorResumePlay();
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                //文件格式不支持
                FeelLog.i("比特流编码标准或文件不符合相关规范");
                uiContril.playError("比特流编码标准或文件不符合相关规范");
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                //一些操作需要太长时间来完成,通常超过3 - 5秒。
                FeelLog.i("操作超时");
                uiContril.playError("操作超时");
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                //比特流编码标准或文件符合相关规范,但媒体框架不支持该功能
                FeelLog.i("比特流编码标准或文件符合相关规范,但媒体框架不支持该功能");
                uiContril.playError("比特流编码标准或文件符合相关规范,但媒体框架不支持该功能");
                break;
            default:
                FeelLog.i("onExtraError:" + extra);
                uiContril.playError("其他错误");
//                errorResmePosition = mp.getCurrentPosition();
//                errorResumePlay();
                break;
        }
        return true;
    }

    private void errorResumePlay() {

        if (errorResmePosition <= 0) {
            return;
        }

        if (errorResumeCount <= 5) {
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(mVideoUri);
                mMediaPlayer.prepareAsync();
                uiContril.onBufferStart();
            } catch (Exception e) {
                FeelLog.e(e);
            }
        } else {
            errorResumeCount = 0;
            if (mVideoUri.contains("v.feelapp.cc")) {
                mVideoUri = mVideoUri.replace("v.feelapp.cc", "7xjwjg.media1.z0.glb.clouddn.com"); //替换视频备用地址域名
                errorResumePlay();
                return;
            }
            uiContril.playError("恢复错误");
            return;
        }

        errorResumeCount++;

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == mp.MEDIA_INFO_BUFFERING_START) {
            if (mParams.isDebug()) {
                Log.i(TAG, "开始缓冲");
            }
            uiContril.onBufferStart();
            onCallStartPlay(CALL_BUFFER_START);
            return true;
        } else if (what == mp.MEDIA_INFO_BUFFERING_END) {
            if (mParams.isDebug()) {
                Log.i(TAG, "缓冲结束");
            }
            uiContril.onBufferEnd();
            onCallStartPlay(CALL_BUFFER_END);
            return true;
        }
        return false;
    }


    @Override
    public void onVolumeAdjust(int volume) {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

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

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        if (!mParams.isAdjustLight()) {
            return;
        }

        if (mCurBrightness != brightness) {
            BrightnessUtils.setBrightness(mActivity, brightness);
        }
        final int percent = (int) (brightness * 100 / 255f);
        uiContril.updateBrightnessLayer(percent);
    }

    @Override
    public void startPlay(String videoName, String videoUri) {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        this.videoName = videoName;
        this.mVideoUri = videoUri;

        if (!surfaceCrate) {
            return;
        }

        if (mParams.isDebug()) {
            Log.d(TAG, "tryPlayVideo");
        }
        if (mVideoUri == null || mPlayWindow == null) {
            if (mParams.isDebug()) {
                Log.d(TAG, "tryPlayVideo return");
            }
            return;
        }

        errorResmePosition = 0;
        errorResumeCount = 0;
        downAdjust = false;
        release();
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(videoUri);
            mMediaPlayer.prepareAsync();
            uiContril.onBufferStart();
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
        } catch (IllegalStateException ex) {
            if (mParams.isDebug()) {
                Log.e(TAG, "Unable to open content: " + mVideoUri, ex);
            }
        }

    }

    @Override
    public synchronized void switchOriginal(boolean isCallSys) {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        if (isCallSys) {
            sendScreenChange(false);
        }

        if (isOriginal) {
            isOriginal = false;
            mPlayWindow.full();
            // 竖屏--->横屏
            if (DisplayUtils.isScreenPortrait(mActivity) || !isCallSys) {
                if (isCallSys) {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }

                adjustFixSize();
            }
        } else {
            if (DisplayUtils.isScreenLandscape(mActivity) || !isCallSys) {
                isOriginal = true;
                mPlayWindow.original();
                if (isCallSys) {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }

                adjustFixSize();
            }
        }

    }


    @Override
    public void switchPlayWindow(final FeelPlayWindow feelPlayWindow) {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        if (feelPlayWindow != null
                && mMediaPlayer != null
                && mParams.getPlayStatus() >= MediaPlay.STATE_BEREADY) {
            this.mPlayWindow = feelPlayWindow;
            haveSurfaceDestory = false;
            mMediaPlayer.setDisplay(null);
            feelPlayWindow.getPlayHolder().setFixedSize(DisplayUtils.getDeviceWidth(mActivity), DisplayUtils.getDeviceWidth(mActivity) * 9 / 16);
            feelPlayWindow.getPlayHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    resumeDuration = System.currentTimeMillis();
                    surfaceCrate = true;
                    if (mMediaPlayer != null) {
                        mMediaPlayer.setDisplay(feelPlayWindow.getPlayHolder());
                    }

                    if (isNeedAutoPlaying && resumeProgress > 0) {
                        isNeedAutoPlaying = false;
                        isResumePlay = true;
                        try {
                            mMediaPlayer.reset();
                            mMediaPlayer.setDataSource(mVideoUri);
                            mMediaPlayer.prepareAsync();
                        } catch (Exception e) {
                            FeelLog.e(e);
                        }
                    }

                    adjustFixSize();
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    FeelLog.i("销毁surcaceView");
                    surfaceCrate = false;
                    haveSurfaceDestory = true;

                    if (mParams.getPlayStatus() >= MediaPlay.STATE_BEREADY) {
                        isNeedAutoPlaying = true;
                        resumeProgress = mMediaPlayer.getCurrentPosition();
                        mMediaPlayer.stop();
                    }
                }
            });

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

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        if (mMediaPlayer != null
                && mParams.getPlayStatus() >= MediaPlay.STATE_BEREADY) {
            mMediaPlayer.seekTo(progress);
        }

    }

    private void release() {
        mParams.setPlayStatus(MediaPlay.STATE_IDLE);
    }

    public boolean showMobileNetHint() {

        if (havaTip || !mParams.isTipNoWifi()) {
            return false;
        }

        if (mParams.isDebug()) {
            Log.d(TAG, "before isMobileNet:" + mParams.isTipNoWifi());
        }

        if (mActivity == null || mActivity.isFinishing()) {
            if (mParams.isDebug()) {
                Log.d(TAG, "当前对象不存在");
            }
            return false;
        }


        if (mMediaPlayer != null && NetworkUtils.isNetworkAvailable(mActivity)) {
            if (NetworkUtils.isMobileNetwork(mActivity)) { // mobile
                isMobileNet = true;
            } else {
                isMobileNet = false;
            }
            if (mParams.isTipNoWifi() && isMobileNet) {

                if (mParams.getPlayStatus() == MediaPlay.STATE_PLAYING) {
                    togglePlayPause(true, false);
                }

                mNetworkTipDialog = MaterialDialogBuilder.getBuilder(mActivity)
                        .content(R.string.not_wifi_remind)
                        .positiveText(R.string.dont_play)
                        .negativeText(R.string.never_mind)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                if (mNetworkTipDialog != null) {
                                    mNetworkTipDialog.dismiss();
                                    mNetworkTipDialog = null;
                                }
                                havaTip = true;
                                mActivity.finish();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                try {
                                    havaTip = true;
                                    mNetworkTipDialog.dismiss();
                                    dealNetChange();
                                } catch (Throwable e) {
                                    FeelLog.e(e);
                                }
                            }
                        })
                        .build();

                mNetworkTipDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        havaTip = true;
                    }
                });
                mNetworkTipDialog.show();
                return true;
            }
        } else {
            isMobileNet = false;
            isNoNet = true;
        }
        if (mParams.isDebug()) {
            Log.d(TAG, "after isMobileNet:" + isMobileNet + " | netuseable:" + isNoNet);
        }
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        initMediaPlayer();
        surfaceCrate = true;
        if (!TextUtils.isEmpty(mVideoUri)
                && mParams.getPlayStatus() != MediaPlay.STATE_PLAYING) {
            startPlay(videoName, mVideoUri);
        }

    }

    private void initMediaPlayer() {

        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();

                mMediaPlayer.setDisplay(mPlayWindow.getPlayHolder());
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setOnBufferingUpdateListener(this);
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.setOnErrorListener(this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                    mMediaPlayer.setOnInfoListener(this);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    private boolean isNeedAutoPlaying = false;
    private boolean isResumePlay = false;
    private int resumeProgress = 0;
    private long resumeDuration = 0;

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        FeelLog.i("销毁surcaceView");

        if (mParams.getPlayStatus() >= MediaPlay.STATE_BEREADY) {
            isNeedAutoPlaying = true;
            resumeProgress = mMediaPlayer.getCurrentPosition();
            mMediaPlayer.stop();
        }

        surfaceCrate = false;
        haveSurfaceDestory = true;
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
     * 监听网络变化
     */
    public class NetworkchangesReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (!mParams.isTipNoWifi()) {
                return;
            }

            String action = intent.getAction();
            if (TextUtils.equals(action, CONNECTIVITY_CHANGE_ACTION)) {

                if (!NetworkUtils.isWifiValid(mActivity)) {
                    showMobileNetHint();
                    return;
                }

                if (!NetworkUtils.isNetworkAvailable(mActivity)) {
                    Toast.makeText(mActivity, "网络连接失败，请检查网络",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

            }
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

            if (mActivity == null || mActivity.isFinishing()) {
                return;
            }

            if (mParams.isDebug()) {
                Log.d(TAG, "BrightnessChangedObserver selfChange : " + selfChange);
            }

            if (selfChange) {
                // nothing to do
            } else {
                final int changedBrightness = BrightnessUtils.getWindowScreenBrightness(mActivity);
                if (lastBrightness == changedBrightness) {
                    return;
                }
                lastBrightness = changedBrightness;
                onBrightnessAdjust(changedBrightness);
            }

        }
    }

    public static final int CALL_START = 0;
    public static final int CALL_PAUSE = CALL_START + 1;
    public static final int CALL_RESUME = CALL_PAUSE + 1;
    public static final int CALL_COMPLETE = CALL_RESUME + 1;
    public static final int CALL_BUFFER_START = CALL_COMPLETE + 1;
    public static final int CALL_BUFFER_END = CALL_BUFFER_START + 1;
    public static final int CALL_PLAY_ERROR = CALL_BUFFER_END + 1;

    /**
     * 调用开始回调
     */
    private void onCallStartPlay(int type) {

        if (playListenerMap != null && !playListenerMap.isEmpty()) {

            for (Iterator ite = playListenerMap.entrySet().iterator(); ite.hasNext(); ) {
                Map.Entry entry = (Map.Entry) ite.next();
                MediaPlayListener playListener = (MediaPlayListener) entry.getValue();

                switch (type) {

                    case CALL_START:
                        playListener.startPlay();
                        break;

                    case CALL_PAUSE:
                        playListener.pausePlay(needResume);
                        break;

                    case CALL_RESUME:
                        playListener.resumePlay(needResume);
                        break;

                    case CALL_COMPLETE:
                        playListener.playComplete();
                        break;

                    case CALL_BUFFER_START:
                        playListener.onBufferStart();
                        break;

                    case CALL_BUFFER_END:
                        playListener.onBufferEnd();
                        break;

                    case CALL_PLAY_ERROR:
                        playListener.onPlayEerror();
                        break;


                }
            }
        }

    }
}
