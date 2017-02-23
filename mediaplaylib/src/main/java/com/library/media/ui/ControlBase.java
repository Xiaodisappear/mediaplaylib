package com.library.media.ui;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.library.media.core.MediaPlay;
import com.library.media.core.UIContril;
import com.library.media.core.impl.PlayControlImpl;

/**
 * Created by xinggenguo on 2/23/17.
 * <p>
 * 界面控制类
 */

public abstract class ControlBase extends FrameLayout implements UIContril {


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

    abstract PlayControlImpl initPlayInfo(MediaPlay.Params params, Activity activity);


    /**
     * 隐藏中部信息变化浮层
     */
    abstract void hideInfoCenterLayer();


    public PlayControlImpl getPlayControl() {
        return null;
    }
}
