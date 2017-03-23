package com.library.media.utils;

import android.os.Bundle;
import android.util.Log;

import java.util.Set;

/**
 * feel 日志工具
 *
 * @author lipf
 */
public class FeelLog {

    private static boolean isDebug = true;

    /**
     * 调试使用的 error 输出
     *
     * @param msg
     */
    public static void de(String msg) {
        if (isDebug) {
            getMethodNames(new Throwable().getStackTrace());
            Log.e("Feel--", msg == null ? "" : createLog(msg));
        }
    }

    /**
     * 使用 error 级别日志打印 string
     *
     * @param msg
     */
    public static void e(String msg) {
        if (isDebug) {
            getMethodNames(new Throwable().getStackTrace());
            Log.e("Feel--", msg == null ? "" : createLog(msg));
        }
    }

    /**
     * 使用 error 级别日志打印 string
     *
     * @param tag
     * @param msg
     */
    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(tag, msg == null ? "" : msg);
        }
    }

    /**
     * 使用error级别日志打印 bundle
     *
     * @param bundle
     */
    public static void e(Bundle bundle) {
        if (isDebug) {
            Set<String> set = bundle.keySet();
            for (String key : set) {
                FeelLog.e(key + " = " + bundle.get(key));
            }
        }
    }

    /**
     * 打印error级别信息
     *
     * @param msg
     */
    public static void e(Object msg) {
        if (isDebug) {
            Log.e("Feel--", msg == null ? "" : msg.toString());
        }
    }

    /**
     * 线上环境用来收集错误日志的
     *
     * @param e
     */
    public static void e(Throwable e) {
        try {
            getMethodNames(new Throwable().getStackTrace());

        } catch (Throwable e2) {
        }
        try {
            String msg = e.getMessage();
            Log.e("Feel--", msg == null ? createLog(e.toString()) : createLog(msg), e);
        } catch (Throwable e2) {
        }
    }


    static String className;
    static String methodName;
    static int lineNumber;

    private static String createLog(String log) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        buffer.append(className);
        buffer.append(":");
        buffer.append(methodName);
        buffer.append(":");
        buffer.append(lineNumber);
        buffer.append("]");
        buffer.append(log);

        return buffer.toString();
    }

    private static void getMethodNames(StackTraceElement[] sElements) {
        try {
            className = sElements[1].getFileName();
            methodName = sElements[1].getMethodName();
            lineNumber = sElements[1].getLineNumber();
        } catch (Throwable e) {
        }
    }

    public static void i(String msg) {
        try {
            if (isDebug) {
                getMethodNames(new Throwable().getStackTrace());
                Log.i("Feel--", msg == null ? "" : createLog(msg));
            }
        } catch (Throwable e) {
        }
    }

    public static void i(Object msg) {
        if (isDebug) {
            Log.i("Feel--", msg == null ? "" : msg.toString());
        }
    }

    public static void w(Object msg) {
        if (isDebug) {
            Log.w("Feel--", msg == null ? "" : msg.toString());
        }
    }

    public static void d(String msg) {
        if (isDebug) {
            getMethodNames(new Throwable().getStackTrace());
            Log.i("Feel--", msg == null ? "" : createLog(msg));
        }
    }

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.i(tag, msg == null ? "" : msg);
        }
    }

    public static void d(Object msg) {
        if (isDebug) {
            Log.i("Feel--", msg == null ? "" : msg.toString());
        }
    }

    public static void i(String tag, String msg) {
        if (isDebug) {
            Log.i(tag, msg == null ? "" : msg);
        }
    }

    public static void d(String message, Object... args) {
        if (isDebug) {
            d(String.format(message, args));
        }
    }

}
