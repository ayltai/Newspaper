package com.github.ayltai.newspaper.util;

import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.util.Log;

public final class VmPolicyFactory {
    private VmPolicyFactory() {
    }

    @NonNull
    public static StrictMode.VmPolicy newVmPolicy() {
        final StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectLeakedRegistrationObjects()
            .detectLeakedSqlLiteObjects()
            .penaltyLog();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) builder.detectFileUriExposure();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) builder.detectContentUriWithoutPermission().detectUntaggedSockets();

        final StrictMode.VmPolicy policy = builder.build();
        Log.d("ALAN", policy.toString());
        return policy;
    }
}
