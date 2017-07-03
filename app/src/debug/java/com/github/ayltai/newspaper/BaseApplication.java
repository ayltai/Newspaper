package com.github.ayltai.newspaper;

import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.multidex.MultiDexApplication;

import com.facebook.common.logging.FLog;
import com.facebook.stetho.Stetho;
import com.github.ayltai.newspaper.util.TestUtils;
import com.squareup.leakcanary.LeakCanary;

public abstract class BaseApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        if (!TestUtils.isRunningUnitTest() && !TestUtils.isRunningInstrumentedTest()) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectCustomSlowCalls()
                .detectNetwork()
                .penaltyLog()
                .penaltyDeath()
                .build());

            StrictMode.setVmPolicy(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createThreadPolicy26() : Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? createThreadPolicy18() : createThreadPolicy());

            if (!LeakCanary.isInAnalyzerProcess(this)) LeakCanary.install(this);

            FLog.setMinimumLoggingLevel(FLog.WARN);
            Stetho.initializeWithDefaults(this);
        }
    }

    private static StrictMode.VmPolicy createThreadPolicy() {
        return new StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectLeakedRegistrationObjects()
            .detectLeakedSqlLiteObjects()
            .penaltyLog()
            .penaltyDeath()
            .build();
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static StrictMode.VmPolicy createThreadPolicy18() {
        return new StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectFileUriExposure()
            .detectLeakedRegistrationObjects()
            .detectLeakedSqlLiteObjects()
            .penaltyLog()
            .penaltyDeath()
            .build();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private static StrictMode.VmPolicy createThreadPolicy26() {
        return new StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectContentUriWithoutPermission()
            .detectFileUriExposure()
            .detectLeakedRegistrationObjects()
            .detectLeakedSqlLiteObjects()
            .penaltyLog()
            .penaltyDeath()
            .detectUntaggedSockets()
            .build();
    }
}
