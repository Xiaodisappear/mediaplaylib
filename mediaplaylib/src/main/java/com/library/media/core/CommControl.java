package com.library.media.core;

import android.media.MediaPlayer;
import android.view.MotionEvent;

import com.library.media.ui.FeelPlayWindow;

/**
 * Created by xinggenguo on 2/22/17.
 * <p>
 * 播放器通用接口
 */

public interface CommControl {


    /**
     * 暂停播放
     */
    void pausePlay();

    /**
     * 恢复播放
     */
    void resumePlay();

    /**
     * 控制条显示隐藏
     *
     * @param isForceVisible
     */
    void toggleController(boolean isForceVisible);

    /**
     * 切换暂停继续
     */
    void togglePlayPause(boolean isForcePause, boolean isForceResume);

    /**
     * Horizontal scroll to control progress of video
     *
     * @param event
     * @param delta
     */
    void onHorizontalScroll(MotionEvent event, float delta);

    /**
     * vertical scroll listen
     *
     * @param motionEvent
     * @param delta
     * @param direction   left or right edge for control brightness or volume
     */
    void onVerticalScroll(MotionEvent motionEvent, float delta, int direction);

    /**
     * 销毁控制器
     */
    void onDestroy();

    /**
     * 开始调节声音/进度/亮度
     */
    void onCtrlStart();

    /**
     * 调节结束
     */
    void onCtrlEnd();

    /**
     * 声音调节
     */
    void onVolumeAdjust(int volume);


    /**
     * 亮度调节
     *
     * @param brightness 0-255
     */
    void onBrightnessAdjust(int brightness);

    /**
     * 设置播放
     *
     * @param videoName
     * @param videoUri
     */
    void startPlay(String videoName, String videoUri);

    /**
     * 切换横竖屏
     */
    void switchOriginal(boolean isCallSys);

    /**
     * 切换playWindow
     */
    void switchPlayWindow(FeelPlayWindow feelPlayWindow);


    /**
     * 是否是全屏
     */
    boolean isFullScreen();

    /**
     * 返回播放器
     *
     * @return
     */
    MediaPlayer getPlayer();


    /**
     * 返回播放器设置参数
     */
    MediaPlay.Params getParams();


    /**
     * 调整播放进度
     */
    void sekTo(int progress);

    /**
     * 添加播放器监听
     */
    void addMediaplayListener(String key, MediaPlayListener playListener);

    /**
     * 清除播放器监听
     */
    void clearMediaplayListener();

    /**
     * onResume 时调用 处理播放问题.
     */
    void onResume();

    /**
     * onPause 时调用 处理播放问题.
     */
    void onPause();

    /**
     * 获取视频总时长
     */
    int getVideoDurationTime();


    /**
     * 获取视频当前播放时长
     */
    int getVideoCurrentTime();

    /**
     * 重新播放视频
     */
    void reStartPlay();

    /**
     * 判断播放载体是否被销毁掉
     */
    boolean isPlayWindowDestory();
}
