package com.library.media.core;

/**
 * Created by xinggenguo on 2/24/17.
 */

public interface MediaPlayListener {

    /**
     * 开始播放
     */
    void startPlay();

    /**
     * 暂停
     */
    void pausePlay();

    /**
     * 恢复
     */
    void resumePlay();

    /**
     * 播放完成
     */
    void playComplete();

}
