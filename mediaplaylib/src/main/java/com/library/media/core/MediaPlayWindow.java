package com.library.media.core;

import android.view.SurfaceHolder;

/**
 * Created by xinggenguo on 2/22/17.
 */

public interface MediaPlayWindow {

    /**
     * 竖屏播放
     */
    void original();

    /**
     * 全屏播放
     */
    void full();


    /**
     * 按照视频比例调整大小
     */
    void adjustFixSize(int width, int height);


    /**
     * 获取播放holder
     */
    SurfaceHolder getPlayHolder();
}
