package com.library.media.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
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
import com.library.media.core.MediaPlay;
import com.library.media.core.impl.PlayControlImpl;
import com.library.media.utils.PlayUtils;

import java.lang.ref.WeakReference;

/**
 * Created by xinggenguo on 2/23/17.
 */

public class FeelVideoControlView extends ControlBase implements Animation.AnimationListener {

    protected static final int HIDE_CENTER_INFO = 4;        // 隐藏中部信息浮层
    protected static final int UPDATE_PROGRESS = 5;        // 更新进度条信息

    private Context mContext;

    private Animation mTopBarEnterAnim;
    private Animation mTopBarExitAnim;
    private Animation mBottomBarEnterAnim;
    private Animation mBottomBarExitAnim;

    private View rootView;

    private FrameLayout mInfoCenterLayer;
    private ProgressBar mPlayProgressbar;
    private ProgressBar mInfoProgress;
    private RelativeLayout mPlayInfo;
    private ImageView mInfoIcon;
    private TextView mInfoTimeChanged;
    private TextView mTimeCurrent;
    private TextView mTimeTotal;
    private ImageButton mFullscreen;
    private FeelPlayWindow mFeelPlayWindow;

    protected PlayControlImpl playControl;

    private boolean isInit = false;

    protected final Handler mHandler = new MsgHandler(this);

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
            mPlayProgressbar = (ProgressBar) findViewById(R.id.play_progressbar);
            mPlayInfo = (RelativeLayout) findViewById(R.id.play_info);
            mInfoIcon = (ImageView) findViewById(R.id.info_icon);
            mInfoTimeChanged = (TextView) findViewById(R.id.info_time_changed);
            mTimeCurrent = (TextView) findViewById(R.id.time_current);
            mTimeTotal = (TextView) findViewById(R.id.time_total);
            mFullscreen = (ImageButton) findViewById(R.id.fullscreen);
            mFeelPlayWindow = (FeelPlayWindow) findViewById(R.id.play_contont);
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
    public PlayControlImpl initPlayInfo(MediaPlay.Params params, Activity activity) {
        isInit = true;
        playControl = new PlayControlImpl(mFeelPlayWindow, params, this, activity);
        return playControl;
    }

    @Override
    public PlayControlImpl getPlayControl() {
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
        mInfoIcon.setImageResource(R.drawable.video_volume_bg);

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
        mInfoIcon.setImageResource(R.drawable.video_volume_bg);

        setRewindTime(realProgress);

        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(HIDE_CENTER_INFO);
            mHandler.removeMessages(HIDE_CENTER_INFO);
            mHandler.sendMessageDelayed(msg, 2000);
        }

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


    private static class MsgHandler extends Handler {

        private final WeakReference<ControlBase> mView;

        private MsgHandler(ControlBase view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            final ControlBase view = mView.get();
            if (view == null) {
                return;
            }

            switch (msg.what) {
                case HIDE_CENTER_INFO:
                    view.hideInfoCenterLayer();
                    break;

                case UPDATE_PROGRESS:
                    view.updateProgress();

                    if (view.getPlayControl().getParams().getPlayStatus() == MediaPlay.STATE_PLAYING) {
                        msg = obtainMessage(UPDATE_PROGRESS);
                        sendMessageDelayed(msg, 1000);
                    }
                    break;
            }

        }
    }

}
