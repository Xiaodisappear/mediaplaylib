package com.library.media.core;

import android.app.Activity;
import android.view.View;

import com.library.media.ui.FeelPlayWindow;

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
     * 显示控制条，非直接调用，通过handle
     */
    void showProgress();

    /**
     * 隐藏控制条,非直接调用，通过handle
     */
    void hideProgress();

    /**
     * 显示控制条，直接调用
     */
    void show();

    /**
     * 隐藏控制条，直接调用
     */
    void hide();

    /**
     * 隐藏中部信息变化浮层
     */
    void hideInfoCenterLayer();

    /**
     * 获取控制器
     */
    CommControl getPlayControl();

    /**
     * 初始化
     */
    CommControl initPlayInfo(MediaPlay.Params params, Activity activity);

    /**
     * 初始化
     */
    CommControl initPlayInfo(MediaPlay.Params params, Activity activity, FeelPlayWindow feelPlayWindow);

    /**
     * 视频准备完毕
     */
    void onBeReady();

    /**
     * 当前视频播放完毕
     */
    void onPlayComplete();

    /**
     * 销毁界面信息
     */
    void onDestory();

    /**
     * 切换播放view.
     */
    void switchPlayWindow(FeelPlayWindow feelPlayWindow);

    /**
     * 缓冲中
     */
    void onBufferStart();

    /**
     * 缓冲结束
     */
    void onBufferEnd();

    /**
     * 检测播放页面
     */
    void checkPlayWindow();

    /**
     * 播放错误
     */
    void playError(String tip);
}
