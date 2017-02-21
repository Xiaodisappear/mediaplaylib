package com.library.media;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.lib.mediaplaylib.R;

/**
 * Created by xinggenguo on 2/21/17.
 */

public class MediaPlayFrameLayout extends FrameLayout {


    public MediaPlayFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public MediaPlayFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaPlayFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {

        LayoutInflater.from(getContext()).inflate(R.layout.layout_mediaplay_root, this);
        this.findViewById(R.id.video_sv_play_view);

    }

}
