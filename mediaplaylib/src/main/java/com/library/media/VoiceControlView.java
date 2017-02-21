package com.library.media;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lib.mediaplaylib.R;

/**
 * Created by xinggenguo on 2/21/17.
 * <p>
 * voice control.
 */

public class VoiceControlView extends FrameLayout {

    private SeekBar mVolumeSeekBar;
    private AudioManager mAudioManager;
    private int syscurrenvolume;
    private boolean mSeekTouchDown;
    private int mLastVolume;
    private FrameLayout mVolumeFL;
    private TextView mVolumeTV;
    private int mDelta = 20;

    public VoiceControlView(@NonNull Context context) {
        this(context, null);
    }

    public VoiceControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoiceControlView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    private void initView() {
        if (isInEditMode()) {
            TextView tv = new TextView(getContext());
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            tv.setText("音量\n50%");
            tv.setTextColor(Color.WHITE);
            tv.setGravity(Gravity.CENTER);
            addView(tv, params);
            return;
        }


        LayoutInflater.from(getContext()).inflate(R.layout.layout_volume_control, this, true);
        mVolumeSeekBar = (SeekBar) findViewById(R.id.seek_bar_volume);
        mVolumeFL = (FrameLayout) findViewById(R.id.volume_fl);
        mVolumeTV = (TextView) findViewById(R.id.volume_tv);

        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        //取得最大音量
        final int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //取得当前音量
        syscurrenvolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // 进度条绑定最大音量，最大音量
        mVolumeSeekBar.setMax(1000);
        // 进度条绑定当前音量
        mVolumeSeekBar.setProgress(500);
        mVolumeSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mLastVolume = 500;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mSeekTouchDown = false;
                        mLastVolume = syscurrenvolume;
                        mVolumeFL.animate().alpha(0).setDuration(2000).start();
                        break;
                }
                return false;
            }
        });
        mVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (!mSeekTouchDown) {
                    mSeekTouchDown = true;
                    return;
                }

                if (Math.abs(seekBar.getProgress() - mLastVolume) < mDelta) {
                    return;
                }

                mVolumeFL.animate().cancel();
                mVolumeFL.setAlpha(1);

                // 取得当前进度
                int tmpInt = (seekBar.getProgress() - mLastVolume) > 0 ? 1 : -1;
                mLastVolume = seekBar.getProgress();

                tmpInt = tmpInt + syscurrenvolume;
                if (tmpInt > maxVolume) {
                    tmpInt = maxVolume;
                } else if (tmpInt < 0) {
                    tmpInt = 0;
                }

                mVolumeFL.setAlpha(1);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, tmpInt, 0);
                syscurrenvolume = tmpInt;
                mVolumeFL.setVisibility(View.VISIBLE);
                mVolumeTV.setText(((int) (tmpInt * 1f / maxVolume * 100f) + "%"));
            }
        });
        //调节音量--end----------------
    }
}
