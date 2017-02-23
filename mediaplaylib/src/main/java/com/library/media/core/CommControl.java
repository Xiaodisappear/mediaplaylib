package com.library.media.core;

import android.view.MotionEvent;

/**
 * Created by xinggenguo on 2/22/17.
 * <p>
 * 播放器通用接口
 */

public interface CommControl {


    /**
     * 控制条显示隐藏
     *
     * @param isForceVisible
     */
    void toggleController(boolean isForceVisible);


    /**
     * 切换暂停继续
     */
    void togglePlayPause();


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
     * 声音调节
     */
    void onVolumeAdjust(int volume);


    /**
     * 亮度调节
     *
     * @param brightness 0-255
     */
    void onBrightnessAdjust(int brightness);
}
