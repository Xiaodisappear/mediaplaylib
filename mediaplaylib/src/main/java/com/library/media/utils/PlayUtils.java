package com.library.media.utils;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by xinggenguo on 2/23/17.
 */

public class PlayUtils {

    private static StringBuilder mFormatBuilder = new StringBuilder();
    private static Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    /**
     * Time format (00:00/00:00)
     *
     * @param timeMs
     * @return
     */
    public static String timeFormat(int timeMs) {
        if (timeMs <= 0) {
            return "00:00";
        }

        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);

        if (hours > 0) {
            return mFormatter.format(("%d:%02d:%02d"), hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

}
