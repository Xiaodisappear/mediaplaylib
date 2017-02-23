package com.library.media.ui;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lib.mediaplaylib.R;

/**
 * Created by xinggenguo on 2/23/17.
 */

public class FeelVideoControlView extends ControlBase implements Animation.AnimationListener {

    private Context mContext;

    private Animation mTopBarEnterAnim;
    private Animation mTopBarExitAnim;
    private Animation mBottomBarEnterAnim;
    private Animation mBottomBarExitAnim;

    private View rootView;

    private FrameLayout mInfoCenterLayer;
    private ProgressBar mInfoProgress;
    private RelativeLayout mPlayInfo;
    private ImageView mInfoIcon;
    private TextView mInfoTimeChanged;
    private ImageButton mFullscreen;


    public FeelVideoControlView(@NonNull Context context) {
        super(context);
    }

    public FeelVideoControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FeelVideoControlView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    void initView() {

        mContext = getContext();

        mTopBarEnterAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_enter_from_top);
        mTopBarEnterAnim.setAnimationListener(this);

        mTopBarExitAnim = AnimationUtils.loadAnimation(mContext, R.anim.anim_exit_from_top);
        mTopBarExitAnim.setAnimationListener(this);

        mBottomBarEnterAnim = AnimationUtils.loadAnimation(mContext, R.anim.anim_enter_from_bottom);
        mBottomBarEnterAnim.setAnimationListener(this);

        mBottomBarExitAnim = AnimationUtils.loadAnimation(mContext, R.anim.anim_exit_from_bottom);
        mBottomBarExitAnim.setAnimationListener(this);

        if (rootView == null) {
            rootView = LayoutInflater.from(getContext()).inflate(R.layout.controller_common, null);

            this.addView(rootView);
            mInfoCenterLayer = (FrameLayout) findViewById(R.id.info_center_layer);
            mInfoProgress = (ProgressBar) findViewById(R.id.info_progress);
            mPlayInfo = (RelativeLayout) findViewById(R.id.play_info);
            mInfoIcon = (ImageView) findViewById(R.id.info_icon);
            mInfoTimeChanged = (TextView) findViewById(R.id.info_time_changed);
            mFullscreen = (ImageButton) findViewById(R.id.fullscreen);

        }

    }

    @Override
    public void updateVolumeLayer(int percent) {

        mInfoTimeChanged.setVisibility(View.GONE);
        mInfoIcon.setImageResource(R.drawable.video_volume_bg);
        mInfoProgress.setProgress(percent);

    }

    @Override
    public void updateBrightnessLayer(int percent) {

        mInfoTimeChanged.setVisibility(View.GONE);
        mInfoIcon.setImageResource(R.drawable.video_bright_bg);
        mInfoProgress.setProgress(percent);

    }

    @Override
    public void updateProgress(int percent) {

    }

    @Override
    public void toggleController(boolean isForceVisible) {

    }


    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
