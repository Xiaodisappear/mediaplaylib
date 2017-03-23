package com.library.media.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.library.media.core.CommControl;
import com.library.media.core.MediaPlay;
import com.library.media.core.UIContril;

import java.lang.ref.WeakReference;

/**
 * Created by xinggenguo on 2/23/17.
 * <p>
 * 界面控制类
 */

public abstract class ControlBase extends FrameLayout implements UIContril {

    protected static final int FADE_IN = 1;                 // 渐现
    protected static final int FADE_OUT = 2;                // 渐隐
    protected static final int FADE_OUT_DELAY = 3;          // 渐隐
    protected static final int HIDE_CENTER_INFO = 4;        // 隐藏中部信息浮层
    protected static final int UPDATE_PROGRESS = 5;        // 更新进度条信息

    protected CommControl playControl;

    protected final Handler mHandler = new MsgHandler(this);

    public ControlBase(@NonNull Context context) {
        this(context, null);
    }

    public ControlBase(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlBase(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @Override
    public CommControl initPlayInfo(MediaPlay.Params params, Activity activity, FeelPlayWindow feelPlayWindow) {
        return null;
    }

    public abstract void initView();


    @Override
    public View getContentView() {
        return this;
    }

    @Override
    public CommControl getPlayControl() {
        return playControl;
    }

    /**
     * 获取样式view
     */
    public View getSkinView() {
        return null;
    }


    @Override
    public void onFastForward(int timeAppend, int realProgress) {

    }

    @Override
    public void checkPlayWindow() {

    }

    @Override
    public void onRewind(int timeAppend, int realProgress) {

    }

    @Override
    public void onBeReady() {

    }

    @Override
    public void onDestory() {

    }

    @Override
    public void switchPlayWindow(FeelPlayWindow feelPlayWindow) {

    }

    @Override
    public void onPlayComplete() {

    }

    @Override
    public void onBufferStart() {

    }

    @Override
    public void onBufferEnd() {

    }

    @Override
    public void playError(String tip) {

    }

    @Override
    public void adjustFixWH(int width, int height) {

        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
        if (layoutParams != null && width > 0 && height > 0) {
            layoutParams.height = height;
            layoutParams.width = width;
            this.setLayoutParams(layoutParams);
        }

    }


    private static class MsgHandler extends Handler {

        private final WeakReference<UIContril> mView;

        private MsgHandler(UIContril view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            final UIContril uiControl = mView.get();
            if (uiControl == null) {
                return;
            }

            switch (msg.what) {

                case HIDE_CENTER_INFO:
                    uiControl.hideInfoCenterLayer();
                    break;

                case FADE_OUT_DELAY:
                    uiControl.hide();
                    break;

                case FADE_IN: {
                    uiControl.show();
                    break;
                }
                case FADE_OUT: {
                    uiControl.hide();
                    break;
                }

                case UPDATE_PROGRESS:
                    uiControl.updateProgress();
                    if (uiControl.getPlayControl().getParams().getPlayStatus() == MediaPlay.STATE_PLAYING) {
                        msg = obtainMessage(UPDATE_PROGRESS);
                        sendMessageDelayed(msg, 1000);
                    }
                    break;
            }

        }
    }
}
