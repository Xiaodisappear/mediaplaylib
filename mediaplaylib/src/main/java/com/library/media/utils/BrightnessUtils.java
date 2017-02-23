package com.library.media.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by xinggenguo on 2/23/17.
 * <p>
 * 亮度调节工具
 */

public class BrightnessUtils {


    private static final String TAG = "BrightnessUtils";

    /**
     * 判断是否开启了自动亮度调节
     */
    public static boolean isAutoBrightness(ContentResolver aContentResolver) {

        boolean autoBrightness = false;

        try {
            autoBrightness = Settings.System.getInt(aContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();

        }

        return autoBrightness;
    }

    /**
     * 获取系统里屏幕的亮度
     *
     * @param context 上下文,必须是Activity
     * @return 屏幕亮度
     */
    public static int getSystemScreenBrightness(Context context) {

        int nowBrightnessValue = 0;
        ContentResolver resolver = context.getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
            if (nowBrightnessValue >= 255) {
                nowBrightnessValue = 255;
            } else if (nowBrightnessValue <= 1) {
                nowBrightnessValue = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return nowBrightnessValue;
    }

    /**
     * 获取系统里屏幕的亮度
     *
     * @param context 上下文,必须是Activity
     * @return 屏幕亮度
     */
    public static int getWindowScreenBrightness(Context context) {

        if (context != null && context instanceof Activity) {
            Window window = ((Activity) context).getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();

            final float brightnessF = lp.screenBrightness;
            if (brightnessF < 0) {
                return getSystemScreenBrightness(context);
            } else {
                final int brightness = Math.round(brightnessF * 255f);
                return brightness;
            }
        } else {
            return -1;
        }
    }

    /**
     * 设置亮度
     *
     * @param context    上下文,必须是Activity
     * @param brightness 要设置亮度
     */
    public static void setBrightness(Context context, int brightness) {

        if (brightness >= 255) {
            brightness = 255;
        } else if (brightness <= 1) {
            brightness = 1;
        }

        if (context != null && context instanceof Activity) {
            Window window = ((Activity) context).getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            if (brightness == -1) {
                lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            } else {
                lp.screenBrightness = (brightness <= 0 ? 1 : brightness) / 255f;
            }
            window.setAttributes(lp);
        } else {
        }

    }

    /**
     * 停止自动亮度调节
     *
     * @param context
     */
    public static void stopAutoBrightness(Context context) {

        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

    }
    //能开启，那自然应该能关闭了哟哟，那怎么关闭呢？很简单的：

    /**
     * 开启亮度自动调节 *
     *
     * @param context
     */


    public static void startAutoBrightness(Context context) {

        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);

    }

    /**
     * 保存亮度设置状态
     */
    public static void saveBrightness(ContentResolver resolver, int brightness) {
        Uri uri = android.provider.Settings.System.getUriFor("screen_brightness");
        android.provider.Settings.System.putInt(resolver, "screen_brightness", brightness);
        // resolver.registerContentObserver(uri, true, myContentObserver);
        resolver.notifyChange(uri, null);
    }
}
