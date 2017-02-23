package com.library.media.ui;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.library.media.core.UIContril;

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
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }

    abstract void initView();

}
