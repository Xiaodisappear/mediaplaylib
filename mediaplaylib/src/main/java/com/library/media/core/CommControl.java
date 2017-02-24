package com.library.media.core;

import android.media.MediaPlayer;
import android.view.MotionEvent;

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
    void togglePlayPause(boolean isForcePause);


    /**
     * 控制条可用/不可用
     *
     * @param isEnable
     */
    void setControllerEnable(boolean isEnable);


    /**
     * Horizontal scroll to control progress of video
     *
     * @param event
     * @param delta
     */
    void onHorizontalScroll(MotionEvent event, float delta, float distanceX);


    /**
     * vertical scroll listen
     *
     * @param motionEvent
     * @param delta
     * @param direction   left or right edge for control brightness or volume
     */
    void onVerticalScroll(MotionEvent motionEvent, float delta, float distanceY, int direction);


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
    void switchOriginal();


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
}
