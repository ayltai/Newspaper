package com.github.ayltai.newspaper.util;

import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;

public final class ThreadPolicyFactory {
    private ThreadPolicyFactory() {
    }

    @NonNull
    public static StrictMode.ThreadPolicy newThreadPolicy() {
        final StrictMode.ThreadPolicy.Builder builder = new StrictMode.ThreadPolicy.Builder()
            .detectCustomSlowCalls()
            .detectNetwork()
            .penaltyDeath()
            .penaltyLog()
            .penaltyFlashScreen();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) builder.detectResourceMismatches();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) builder.detectUnbufferedIo();

        return builder.build();
    }
}
