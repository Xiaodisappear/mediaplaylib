package com.library.media.core;

/**
 * Created by xinggenguo on 2/22/17.
 */

public class MediaPlay {

    //----------------- 播放器相关状态 -----------------//
    /**
     * 错误
     */
    public static int STATE_ERROR = -1;

    /**
     * 空闲
     */
    public static int STATE_IDLE = 0;

    /**
     * 准备中
     */
    public static int STATE_PREPARING = 1;

    /**
     * 已准备好
     */
    public static int STATE_BEREADY = 2;

    /**
     * 正在播放
     */
    public static int STATE_PLAYING = 3;

    /**
     * 播放暂停
     */
    public static int STATE_PAUSED = 4;

    /**
     * 播放完成
     */
    public static int STATE_PLAYBACK_COMPLETED = 5;


    public Params newInstance(){
        return new Params();
    }

    public class Params {


        /**
         * 当前播放状态
         */
        private int playStatus = 0;

        /**
         * 调试模式
         */
        private boolean isDebug = false;

        /**
         * 播放链接
         */
        private String url;

        /**
         * 非wifi状态下是否提示
         */
        private boolean tipNoWifi = false;

        /**
         * 初始状态是竖屏
         */
        private boolean isOriginal = true;

        /**
         * 是否允许滑动切换声音
         */
        private boolean isAdjustVoice = true;

        /**
         * 是否允许滑动切换亮度
         */
        private boolean isAdjustLight = true;

        /**
         * 是否允许滑动切换进度条
         */
        private boolean isAdjustProgress = true;

        /**
         * 是否自动开始播放
         *
         * @return
         */
        private boolean isAutoPlay = true;

        public boolean isAutoPlay() {
            return isAutoPlay;
        }

        public Params setAutoPlay(boolean autoPlay) {
            isAutoPlay = autoPlay;
            return this;
        }

        public boolean isDebug() {
            return isDebug;
        }

        public Params setDebug(boolean debug) {
            isDebug = debug;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public Params setUrl(String url) {
            this.url = url;
            return this;
        }

        public boolean isTipNoWifi() {
            return tipNoWifi;
        }

        public Params setTipNoWifi(boolean tipNoWifi) {
            this.tipNoWifi = tipNoWifi;
            return this;
        }

        public boolean isOriginal() {
            return isOriginal;
        }

        public Params setOriginal(boolean original) {
            isOriginal = original;
            return this;
        }

        public boolean isAdjustVoice() {
            return isAdjustVoice;
        }

        public Params setAdjustVoice(boolean adjustVoice) {
            isAdjustVoice = adjustVoice;
            return this;
        }

        public boolean isAdjustLight() {
            return isAdjustLight;
        }

        public Params setAdjustLight(boolean adjustLight) {
            isAdjustLight = adjustLight;
            return this;
        }

        public boolean isAdjustProgress() {
            return isAdjustProgress;
        }

        public Params setAdjustProgress(boolean adjustProgress) {
            isAdjustProgress = adjustProgress;
            return this;
        }

        public Params build() {
            return new Params();
        }

        public int getPlayStatus() {
            return playStatus;
        }

        public void setPlayStatus(int playStatus) {
            this.playStatus = playStatus;
        }


    }


}
