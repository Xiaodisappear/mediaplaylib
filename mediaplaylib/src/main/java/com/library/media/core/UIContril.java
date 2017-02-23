package com.library.media.core;

/**
 * Created by xinggenguo on 2/23/17.
 * <p>
 * 定义界面控制基本功能
 */

public interface UIContril {

    /**
     * 控制声音进度条
     */
    void updateVolumeLayer(int percent);

    /**
     * 控制亮度进度条
     */
    void updateBrightnessLayer(int percent);

    /**
     * 控制播放进度
     */
    void updateProgress(int percent);


    /**
     * 控制条显示隐藏
     *
     * @param isForceVisible
     */
    void toggleController(boolean isForceVisible);

}
