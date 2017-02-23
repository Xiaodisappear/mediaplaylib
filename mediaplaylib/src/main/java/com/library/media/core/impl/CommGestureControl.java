package com.library.media.core.impl;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.library.media.core.CommControl;
import com.library.media.utils.DisplayUtils;

/**
 * Created by xinggenguo on 2/23/17.
 */

public class CommGestureControl extends GestureDetector.SimpleOnGestureListener {


    private static final String TAG = "CommonGestureListener";

    public static final int SLIDE_HORIZONTAL_THRESHOLD = 60;
    public static final int SLIDE_VERTICAL_THRESHOLD = 50;
    public static final int SLIDE_LEFT = 1;
    public static final int SLIDE_RIGHT = 2;

    private Context mContext;
    private CommControl mCommonCtrl;


    public CommGestureControl(Context context, CommControl control) {
        mContext = context;
        mCommonCtrl = control;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // lockFlag = FLAG_NONE;
        if (mCommonCtrl != null) {
            mCommonCtrl.onCtrlStart();
            return true;
        }
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mCommonCtrl != null) {
            mCommonCtrl.toggleController(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (mCommonCtrl != null) {
            mCommonCtrl.togglePlayPause();
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        float deltaX = e2.getX() - e1.getX();
        float deltaY = e2.getY() - e1.getY();
        if (Math.abs(deltaX) > Math.abs(deltaY)) { //横向滚动

            if (Math.abs(deltaX) > SLIDE_HORIZONTAL_THRESHOLD) {
                mCommonCtrl.onHorizontalScroll(e2, deltaX, distanceX);
            }

            return true;
        } else {

            if (Math.abs(deltaY) > SLIDE_VERTICAL_THRESHOLD) {
                if (e1.getX() < DisplayUtils.getDeviceWidth(mContext) * 1.0 / 5) { // left edge
                    deltaY = -deltaY * 0.5f;
                    mCommonCtrl.onVerticalScroll(e2, deltaY, distanceY, SLIDE_LEFT);
                } else if (e1.getX() > DisplayUtils.getDeviceWidth(mContext) * 4.0 / 5) { // right edge
                    deltaY = -deltaY * 0.8f;
                    mCommonCtrl.onVerticalScroll(e2, deltaY, distanceY, SLIDE_RIGHT);
                }

                return true;
            }
        }

        return false;

    }

}
