package com.github.ayltai.newspaper.util;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import com.github.ayltai.newspaper.BuildConfig;

public final class LogUtils {
    private LogUtils() {
    }

    public static void v(final String tag, final String message) {
        LogUtils.v(tag, message, null);
    }

    public static void v(final String tag, final String message, final Throwable throwable) {
        if (BuildConfig.DEBUG) {
            if (throwable == null) {
                Log.v(tag, message);
            } else {
                Log.v(tag, message, throwable);
            }
        } else {
            FirebaseCrash.logcat(Log.VERBOSE, tag, message);
        }
    }

    public static void d(final String tag, final String message) {
        LogUtils.v(tag, message, null);
    }

    public static void d(final String tag, final String message, final Throwable throwable) {
        if (BuildConfig.DEBUG) {
            if (throwable == null) {
                Log.v(tag, message);
            } else {
                Log.v(tag, message, throwable);
            }
        } else {
            FirebaseCrash.logcat(Log.DEBUG, tag, message);
        }
    }

    public static void i(final String tag, final String message) {
        LogUtils.v(tag, message, null);
    }

    public static void i(final String tag, final String message, final Throwable throwable) {
        if (BuildConfig.DEBUG) {
            if (throwable == null) {
                Log.v(tag, message);
            } else {
                Log.v(tag, message, throwable);
            }
        } else {
            FirebaseCrash.logcat(Log.INFO, tag, message);
        }
    }

    public static void w(final String tag, final String message) {
        LogUtils.v(tag, message, null);
    }

    public static void w(final String tag, final String message, final Throwable throwable) {
        if (BuildConfig.DEBUG) {
            if (throwable == null) {
                Log.v(tag, message);
            } else {
                Log.v(tag, message, throwable);
            }
        } else {
            FirebaseCrash.logcat(Log.WARN, tag, message);
        }
    }

    public static void e(final String tag, final String message) {
        LogUtils.v(tag, message, null);
    }

    public static void e(final String tag, final String message, final Throwable throwable) {
        if (BuildConfig.DEBUG) {
            if (throwable == null) {
                Log.v(tag, message);
            } else {
                Log.v(tag, message, throwable);
            }
        } else {
            FirebaseCrash.logcat(Log.ERROR, tag, message);
        }
    }
}
