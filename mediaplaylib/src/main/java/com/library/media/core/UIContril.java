package com.library.media.core;

import android.app.Activity;
import android.view.View;

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
    void updateProgress();

    /**
     * 控制条显示隐藏
     *
     * @param isForceVisible
     */
    void toggleController(boolean isForceVisible);

    /**
     * 获取跟布局
     */
    View getContentView();

    /**
     * 开始更新进度
     */
    void startUpdateProgress();

    /**
     * 开始更新进度
     */
    void clearUpdateProgress();

    /**
     * 快进
     */
    void onFastForward(int timeAppend, int realProgress);

    /**
     * 快退
     */
    void onRewind(int timeAppend, int realProgress);

    /**
     * 调整大小
     */
    void adjustFixWH(int width, int height);

    /**
     * 控制条是否显示
     */
    boolean isControllerBarShowing();

    /**
     * 显示控制条
     */
    void showProgress();

    /**
     * 隐藏控制条
     */
    void hideProgress();

    /**
     * 隐藏中部信息变化浮层
     */
    abstract void hideInfoCenterLayer();

    /**
     * 获取控制器
     */
    abstract CommControl getPlayControl();

    /**
     * 初始化
     */
    abstract CommControl initPlayInfo(MediaPlay.Params params, Activity activity);
}
