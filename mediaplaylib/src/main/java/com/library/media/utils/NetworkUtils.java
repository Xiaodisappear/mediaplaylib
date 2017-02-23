package com.library.media.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by xinggenguo on 2/23/17.
 */

public class NetworkUtils {

    public static boolean hasInternetPermission(Context context) {
        return context != null ? context.checkCallingOrSelfPermission("android.permission.INTERNET") == PackageManager.PERMISSION_GRANTED : true;
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context != null) {
            NetworkInfo info = getActiveNetworkInfo(context);
            return info != null && info.isConnected();
        } else {
            return false;
        }
    }

    public static boolean isWifiValid(Context context) {
        if (context != null) {
            NetworkInfo info = getActiveNetworkInfo(context);
            return info != null && 1 == info.getType() && info.isConnected();
        } else {
            return false;
        }
    }

    public static boolean isMobileNetwork(Context context) {
        if (context != null) {
            NetworkInfo info = getActiveNetworkInfo(context);
            return info == null ? false : info != null && info.getType() == 0 && info.isConnected();
        } else {
            return false;
        }
    }

    public static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivity.getActiveNetworkInfo();
    }

    public static NetworkInfo getNetworkInfo(Context context, int networkType) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(networkType);
    }


}
