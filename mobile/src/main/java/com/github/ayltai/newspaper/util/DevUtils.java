package com.github.ayltai.newspaper.util;

import java.util.concurrent.atomic.AtomicBoolean;

import com.github.ayltai.newspaper.BuildConfig;

public final class DevUtils {
    private static AtomicBoolean isRunningUnitTest;
    private static AtomicBoolean isRunningInstrumentedTest;

    private DevUtils() {
    }

    public static boolean isLoggable() {
        return BuildConfig.DEBUG && !DevUtils.isRunningTests();
    }

    public static boolean isRunningTests() {
        return DevUtils.isRunningUnitTest() || DevUtils.isRunningInstrumentedTest();
    }

    public static synchronized boolean isRunningUnitTest() {
        if (DevUtils.isRunningUnitTest == null) {
            boolean isRunningUnitTest = false;

            try {
                Class.forName("org.robolectric.RobolectricTestRunner");
                isRunningUnitTest = true;
            } catch (final ClassNotFoundException e) {
                // Ignored
            }

            DevUtils.isRunningUnitTest = new AtomicBoolean(isRunningUnitTest);
        }

        return DevUtils.isRunningUnitTest.get();
    }

    public static synchronized boolean isRunningInstrumentedTest() {
        if (DevUtils.isRunningInstrumentedTest == null) {
            boolean isRunningInstrumentedTest = false;

            try {
                Class.forName("android.support.test.espresso.Espresso");
                isRunningInstrumentedTest = true;
            } catch (final ClassNotFoundException e) {
                // Ignored
            }

            DevUtils.isRunningInstrumentedTest = new AtomicBoolean(isRunningInstrumentedTest);
        }

        return DevUtils.isRunningInstrumentedTest.get();
    }
}
