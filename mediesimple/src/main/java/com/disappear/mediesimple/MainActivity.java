package com.disappear.mediesimple;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.library.media.core.CommControl;
import com.library.media.core.MediaPlay;
import com.library.media.core.impl.PlayControlImpl;
import com.library.media.ui.FeelVideoControlView;

public class MainActivity extends AppCompatActivity {

    private CommControl playControl;
    private String path = "http://v.feelapp.cc/goal/0627/newreshen.mp4 ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FeelVideoControlView feelVideoControlView = (FeelVideoControlView) this.findViewById(R.id.feel_video_control);
        final MediaPlay.Params params = new MediaPlay().newInstance();
        params.setAdjustLight(true)
                .setTipNoWifi(true)
                .setOriginal(true)
                .setAdjustProgress(true)
                .setDebug(true)
                .setAutoPlay(true);
        playControl = feelVideoControlView.initPlayInfo(params, this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                playControl.startPlay("测试视频", path);
            }
        }, 500);

    }

    @Override
    protected void onResume() {
        super.onResume();
        playControl.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        playControl.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playControl.onDestroy();
    }

    public void startPlay(View view) {
        playControl.startPlay("测试", path);
    }
}
