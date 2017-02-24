package com.library.media.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lib.mediaplaylib.R;
import com.library.media.core.CommControl;
import com.library.media.core.MediaPlay;
import com.library.media.core.impl.PlayControlImpl;
import com.library.media.utils.PlayUtils;


/**
 * Created by xinggenguo on 2/23/17.
 */

public class FeelVideoControlView extends ControlBase implements Animation.AnimationListener {

    private String TAG = this.getClass().getSimpleName();


    private Context mContext;

    private Animation mTopBarEnterAnim;
    private Animation mTopBarExitAnim;
    private Animation mBottomBarEnterAnim;
    private Animation mBottomBarExitAnim;

    private View rootView;

    private FrameLayout mInfoCenterLayer;
    private SeekBar mPlayProgressbar;
    private ProgressBar mInfoProgress;
    private RelativeLayout mPlayInfo;
    private RelativeLayout mControllerBottom;
    private RelativeLayout mControllerTop;
    private ImageView mInfoIcon;
    private TextView mInfoTimeChanged;
    private TextView mTimeCurrent;
    private TextView mTimeTotal;
    private ImageButton mFullscreen;
    private ImageButton mPauseResume;
    private FeelPlayWindow mFeelPlayWindow;

    protected CommControl playControl;

    private boolean isInit = false;

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
            rootView = LayoutInflater.from(getContext()).inflate(R.layout.controller_common,this,true);
            mInfoCenterLayer = (FrameLayout) findViewById(R.id.info_center_layer);
            mInfoProgress = (ProgressBar) findViewById(R.id.info_progress);
            mPlayProgressbar = (SeekBar) findViewById(R.id.play_progressbar);
            mPlayInfo = (RelativeLayout) findViewById(R.id.play_info);
            mControllerBottom = (RelativeLayout) findViewById(R.id.controller_bottom);
            mControllerTop = (RelativeLayout) findViewById(R.id.controller_top);
            mInfoIcon = (ImageView) findViewById(R.id.info_icon);
            mInfoTimeChanged = (TextView) findViewById(R.id.info_time_changed);
            mTimeCurrent = (TextView) findViewById(R.id.time_current);
            mTimeTotal = (TextView) findViewById(R.id.time_total);
            mFullscreen = (ImageButton) findViewById(R.id.fullscreen);
            mPauseResume = (ImageButton) findViewById(R.id.pause_resume);
            mFeelPlayWindow = (FeelPlayWindow) findViewById(R.id.play_contont);

            mPauseResume.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (playControl != null) {
                        if (playControl.getParams().getPlayStatus() == MediaPlay.STATE_PLAYING) {
                            playControl.pausePlay();
                            mPauseResume.setSelected(false);
                        } else {
                            playControl.resumePlay();
                            mPauseResume.setSelected(true);
                        }
                    }

                }
            });

            mFullscreen.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (playControl != null) {
                        playControl.switchOriginal();

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateFullScreen();
                            }
                        }, 100);

                    }
                }
            });
        }

    }

    /**
     * 隐藏中部信息变化浮层
     */
    public void hideInfoCenterLayer() {
        if (mInfoCenterLayer != null) {
            mInfoCenterLayer.setVisibility(View.GONE);
        }
        if (playControl != null) {
            playControl.onCtrlEnd();
        }
    }

    @Override
    public CommControl initPlayInfo(MediaPlay.Params params, Activity activity) {
        isInit = true;
        playControl = new PlayControlImpl(mFeelPlayWindow, params, this, activity);
        return playControl;
    }

    @Override
    public CommControl getPlayControl() {
        return playControl;
    }

    @Override
    public void updateVolumeLayer(int percent) {

        if (!isInit) {
            return;
        }

        mInfoCenterLayer.setVisibility(VISIBLE);
        mInfoTimeChanged.setVisibility(View.GONE);
        mInfoProgress.setVisibility(VISIBLE);
        mInfoIcon.setImageResource(R.drawable.video_volume_bg);
        mInfoProgress.setProgress(percent);

        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(HIDE_CENTER_INFO);
            mHandler.removeMessages(HIDE_CENTER_INFO);
            mHandler.sendMessageDelayed(msg, 2000);
        }

    }

    @Override
    public void updateBrightnessLayer(int percent) {

        if (!isInit) {
            return;
        }

        mInfoCenterLayer.setVisibility(VISIBLE);
        mInfoProgress.setVisibility(VISIBLE);
        mInfoTimeChanged.setVisibility(View.GONE);
        mInfoIcon.setImageResource(R.drawable.video_bright_bg);
        mInfoProgress.setProgress(percent);


        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(HIDE_CENTER_INFO);
            mHandler.removeMessages(HIDE_CENTER_INFO);
            mHandler.sendMessageDelayed(msg, 2000);
        }

    }


    public void updateProgress() {

        if (playControl == null
                || playControl.getPlayer() == null) {
            return;
        }

        int position = playControl.getPlayer().getCurrentPosition();
        int duration = playControl.getPlayer().getDuration();
        if (mPlayProgressbar != null) {
            mPlayProgressbar.setProgress(position);
            mPlayProgressbar.setMax(duration);
        }

        if (mTimeCurrent != null) {
            mTimeTotal.setText(PlayUtils.timeFormat(duration));
        }

        if (mTimeCurrent != null) {
            mTimeCurrent.setText(PlayUtils.timeFormat(position));
        }

    }


    @Override
    public void toggleController(boolean isForceVisible) {
        Log.i(TAG, "显示控制窗口");

    }

    @Override
    public View getContentView() {
        return this;
    }

    @Override
    public void startUpdateProgress() {
        Message msg = Message.obtain();
        msg.what = UPDATE_PROGRESS;
        mHandler.sendMessage(msg);
    }

    @Override
    public void clearUpdateProgress() {
        mHandler.removeMessages(UPDATE_PROGRESS);

    }

    @Override
    public void onFastForward(int timeAppend, int realProgress) {

        if (getPlayControl() == null || !isInit) {
            return;
        }

        mInfoCenterLayer.setVisibility(VISIBLE);
        mInfoTimeChanged.setVisibility(View.VISIBLE);
        mInfoProgress.setVisibility(GONE);
        mInfoIcon.setImageResource(R.drawable.video_fast_forward);

        setRewindTime(realProgress);

        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(HIDE_CENTER_INFO);
            mHandler.removeMessages(HIDE_CENTER_INFO);
            mHandler.sendMessageDelayed(msg, 2000);
        }

    }

    private void setRewindTime(int realTime) {

        String currentTime = PlayUtils.timeFormat(realTime);
        String allTime = PlayUtils.timeFormat(playControl.getPlayer().getDuration());

        mInfoTimeChanged.setText(new StringBuffer().append(currentTime).append("/").append(allTime));

        if (mPlayProgressbar != null) {
            mPlayProgressbar.setProgress(realTime);
            mPlayProgressbar.setMax(playControl.getPlayer().getDuration());
        }
    }

    @Override
    public void onRewind(int timeAppend, int realProgress) {
        if (getPlayControl() == null) {
            return;
        }

        mInfoCenterLayer.setVisibility(VISIBLE);
        mInfoTimeChanged.setVisibility(View.VISIBLE);
        mInfoProgress.setVisibility(GONE);
        mInfoIcon.setImageResource(R.drawable.video_rewind);

        setRewindTime(realProgress);

        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(HIDE_CENTER_INFO);
            mHandler.removeMessages(HIDE_CENTER_INFO);
            mHandler.sendMessageDelayed(msg, 2000);
        }

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


    protected void removeHandlerCallback() {
        if (mHandler != null) {
            mHandler.removeMessages(FADE_IN);
            mHandler.removeMessages(FADE_OUT);
            mHandler.removeMessages(FADE_OUT_DELAY);
        }
    }

    @Override
    public boolean isControllerBarShowing() {
        return mControllerBottom.getVisibility() == VISIBLE;
    }

    @Override
    public void showProgress() {
        removeHandlerCallback();
        showBar();
        Message msg = Message.obtain();
        msg.what = FADE_OUT_DELAY;
        mHandler.sendMessageDelayed(msg, 5000);
    }

    @Override
    public void hideProgress() {

        removeHandlerCallback();
        Message msg = Message.obtain();
        msg.what = FADE_OUT;
        mHandler.sendMessageDelayed(msg, 2000);

    }

    @Override
    public void show() {
        showBar();
    }

    public void hide() {
        hideBar();
    }

    /**
     * 隐藏顶部和底部Bar
     */
    private void hideBar() {

//        if (mControllerTop != null && mControllerTop.isShown()) {
//            mControllerTop.startAnimation(mTopBarExitAnim);
//        }

        if (mControllerBottom != null && mControllerBottom.isShown()) {
            mControllerBottom.startAnimation(mBottomBarExitAnim);
        }

    }

    /**
     * 展示顶部和底部Bar(有动画)
     */
    private void showBar() {

        if (!isControllerBarShowing()) {
//            if (mControllerTop != null) {
//                if (!playControl.getParams().isTitleBarVisible()) {
//                    if (mControllerTop.getVisibility() != View.GONE) {
//                        mControllerTop.setVisibility(View.GONE);
//                    }
//                } else {
//                    mControllerTop.startAnimation(mTopBarEnterAnim);
//                }
//            }

            if (mControllerBottom != null) {
                mControllerBottom.startAnimation(mBottomBarEnterAnim);
            }
        }

        updatePauseResume();
        updateFullScreen();

    }

    /**
     * 更改全屏按钮状态
     */
    public void updateFullScreen() {
        if (playControl == null) {
            return;
        }
        if (playControl.isFullScreen()) {
            mFullscreen.setSelected(true);
        } else {
            mFullscreen.setSelected(false);
        }
    }


    /**
     * 更改播放按钮状态
     */
    public void updatePauseResume() {
        if (playControl == null) {
            return;
        }
        if (playControl.getParams().getPlayStatus() != MediaPlay.STATE_PLAYING) {
            mPauseResume.setSelected(false);
        } else {
            mPauseResume.setSelected(true);
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (animation == mTopBarEnterAnim) { // 显示TopBar
//            if (mControllerTop != null) {
//                mControllerTop.setVisibility(VISIBLE);
//            }
        } else if (animation == mBottomBarEnterAnim) {
            if (mControllerBottom != null) {
                mControllerBottom.setVisibility(VISIBLE);
            }
        } else if (animation == mTopBarExitAnim) {
//            if (mControllerTop != null) {
//                mControllerTop.setVisibility(GONE);
//            }
        } else if (animation == mBottomBarExitAnim) {
            if (mControllerBottom != null) {
                mControllerBottom.setVisibility(GONE);
            }
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }


}
