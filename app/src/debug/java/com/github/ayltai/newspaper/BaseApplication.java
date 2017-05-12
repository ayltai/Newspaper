package com.github.ayltai.newspaper;

import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;

import com.facebook.common.logging.FLog;
import com.facebook.stetho.Stetho;
import com.github.ayltai.newspaper.util.TestUtils;
import com.squareup.leakcanary.LeakCanary;

public abstract class BaseApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        if (!TestUtils.isRunningUnitTest() && !TestUtils.isRunningInstrumentalTest()) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectCustomSlowCalls()
                .detectNetwork()
                .penaltyLog()
                .penaltyDeath()
                .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectActivityLeaks()
                .detectLeakedRegistrationObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

            if (!LeakCanary.isInAnalyzerProcess(this)) LeakCanary.install(this);

            FLog.setMinimumLoggingLevel(FLog.WARN);
            Stetho.initializeWithDefaults(this);
        }
    }
}
