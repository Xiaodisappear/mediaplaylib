package com.library.media.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
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
    protected static final int HIDE_CENTER_INFO = 4;        // 隐藏中部信息浮层
    protected static final int UPDATE_PROGRESS = 5;        // 更新进度条信息

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

    abstract void initView();




    private static class MsgHandler extends Handler {

        private final WeakReference<UIContril> mView;

        private MsgHandler(UIContril view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            final UIContril uiContril = mView.get();
            if (uiContril == null) {
                return;
            }

            switch (msg.what) {

                case HIDE_CENTER_INFO:
                    uiContril.hideInfoCenterLayer();
                    break;

                case FADE_IN: {
                    uiContril.showProgress();
                    break;
                }
                case FADE_OUT: {
                    uiContril.hideProgress();
                    break;
                }

                case UPDATE_PROGRESS:
                    uiContril.updateProgress();

                    if (uiContril.getPlayControl().getParams().getPlayStatus() == MediaPlay.STATE_PLAYING) {
                        msg = obtainMessage(UPDATE_PROGRESS);
                        sendMessageDelayed(msg, 1000);
                    }
                    break;
            }

        }
    }
}
