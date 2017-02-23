package com.library.media.utils;

import android.app.Activity;

import java.lang.ref.WeakReference;

/**
 * Created by xinggenguo on 2/23/17.
 */

public class SystemUtils {



    /**
     *
     * 判断若引用的 activity 是否存在
     * @param weakReference
     * @return
     */
    public static boolean isExistWeakReferenceActivity(WeakReference<? extends Activity> weakReference) {

        if (weakReference == null || weakReference.get() == null || weakReference.get().isFinishing()) {
            return false;
        }

        return true;
    }

}
