package com.github.ayltai.newspaper.util;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import com.github.ayltai.newspaper.BuildConfig;

@SuppressWarnings("checkstyle:finalclass")
public class LogUtils {
    private static LogUtils instance;

    public static LogUtils getInstance() {
        if (instance == null) instance = new LogUtils();

        return instance;
    }

    private LogUtils() {
    }

    public void v(final String tag, final String message) {
        this.v(tag, message, null);
    }

    public void v(final String tag, final String message, final Throwable throwable) {
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

    public void d(final String tag, final String message) {
        this.d(tag, message, null);
    }

    public void d(final String tag, final String message, final Throwable throwable) {
        if (BuildConfig.DEBUG) {
            if (throwable == null) {
                Log.d(tag, message);
            } else {
                Log.d(tag, message, throwable);
            }
        } else {
            FirebaseCrash.logcat(Log.DEBUG, tag, message);
        }
    }

    public void i(final String tag, final String message) {
        this.i(tag, message, null);
    }

    public void i(final String tag, final String message, final Throwable throwable) {
        if (BuildConfig.DEBUG) {
            if (throwable == null) {
                Log.i(tag, message);
            } else {
                Log.i(tag, message, throwable);
            }
        } else {
            FirebaseCrash.logcat(Log.INFO, tag, message);
        }
    }

    public void w(final String tag, final String message) {
        this.w(tag, message, null);
    }

    public void w(final String tag, final String message, final Throwable throwable) {
        if (BuildConfig.DEBUG) {
            if (throwable == null) {
                Log.w(tag, message);
            } else {
                Log.w(tag, message, throwable);
            }
        } else {
            FirebaseCrash.logcat(Log.WARN, tag, message);
        }
    }

    public void e(final String tag, final String message) {
        this.e(tag, message, null);
    }

    public void e(final String tag, final String message, final Throwable throwable) {
        if (BuildConfig.DEBUG) {
            if (throwable == null) {
                Log.e(tag, message);
            } else {
                Log.e(tag, message, throwable);
            }
        } else {
            FirebaseCrash.logcat(Log.ERROR, tag, message);
        }
    }
}
