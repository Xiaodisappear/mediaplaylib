package com.library.media.core;

/**
 * Created by xinggenguo on 2/24/17.
 * <p>
 * 控制器回调，不能直接调用，或者其中不能调用控制器
 */

public interface MediaPlayListener {

    /**
     * 开始播放
     */
    void startPlay();

    /**
     * 暂停
     *
     * @param activityPause 界面pause
     */
    void pausePlay(boolean activityPause);

    /**
     * 恢复
     *
     * @param activityResume 界面 resume
     */
    void resumePlay(boolean activityResume);

    /**
     * 播放完成
     */
    void playComplete();

    /**
     * 缓冲中
     */
    void onBufferStart();

    /**
     * 缓冲结束
     */
    void onBufferEnd();


    /**
     * 播放出错
     */
    void onPlayEerror();
}
