package com.disappear.mediesimple;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.library.media.core.MediaPlay;
import com.library.media.ui.FeelVideoControlView;

public class MainActivity extends AppCompatActivity {

    private FeelVideoControlView feelVideoControlView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        feelVideoControlView = (FeelVideoControlView) this.findViewById(R.id.feel_video_control);

    }
}
