package com.github.ayltai.newspaper;

import android.app.Application;
import android.os.StrictMode;

import com.facebook.common.logging.FLog;
import com.facebook.stetho.Stetho;
import com.github.ayltai.newspaper.util.TestUtils;
import com.letv.sarrsdesktop.blockcanaryex.jrt.BlockCanaryEx;
import com.letv.sarrsdesktop.blockcanaryex.jrt.Config;
import com.letv.sarrsdesktop.blockcanaryex.jrt.FrequentMethodInfo;
import com.letv.sarrsdesktop.blockcanaryex.jrt.MethodInfo;
import com.squareup.leakcanary.LeakCanary;

public abstract class BaseApplication extends Application {
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

            if (!BlockCanaryEx.isInSamplerProcess(this)) BlockCanaryEx.install(new Config(this) {
                private static final double FRAME_DURATION     = (long)(1000.0 / 60.0);
                private static final double BLOCK_THRESHOLD    = 3 * 2 * FRAME_DURATION;
                private static final int    FREQUENT_THRESHOLD = 5;

                @Override
                public boolean isBlock(final long startTime, final long endTime, final long startThreadTime, final long endThreadTime) {
                    return endTime - startTime > BLOCK_THRESHOLD;
                }

                @Override
                public boolean isHeavyMethod(final MethodInfo methodInfo) {
                    return methodInfo.getCostRealTimeMs() > FRAME_DURATION;
                }

                @Override
                public boolean isFrequentMethod(final FrequentMethodInfo frequentMethodInfo) {
                    return frequentMethodInfo.getTotalCostRealTimeMs() > FRAME_DURATION && frequentMethodInfo.getCalledTimes() >= FREQUENT_THRESHOLD;
                }
            });

            FLog.setMinimumLoggingLevel(FLog.WARN);
            Stetho.initializeWithDefaults(this);
        }
    }
}
