package com.github.ayltai.newspaper.util;

import java.util.concurrent.atomic.AtomicBoolean;

public final class TestUtils {
    private static AtomicBoolean isRunningUnitTest;
    private static AtomicBoolean isRunningInstrumentalTest;

    private TestUtils() {
    }

    public static synchronized boolean isRunningUnitTest() {
        if (TestUtils.isRunningUnitTest == null) {
            boolean isRunningUnitTest = false;

            try {
                Class.forName("org.robolectric.RobolectricTestRunner");
                isRunningUnitTest = true;
            } catch (final ClassNotFoundException e) {
                // Ignored
            }

            TestUtils.isRunningUnitTest = new AtomicBoolean(isRunningUnitTest);
        }

        return TestUtils.isRunningUnitTest.get();
    }

    public static synchronized boolean isRunningInstrumentalTest() {
        if (TestUtils.isRunningInstrumentalTest == null) {
            boolean isRunningInstrumentalTest = false;

            try {
                Class.forName("android.support.test.espresso.Espresso");
                isRunningInstrumentalTest = true;
            } catch (final ClassNotFoundException e) {
                // Ignored
            }

            TestUtils.isRunningInstrumentalTest = new AtomicBoolean(isRunningInstrumentalTest);
        }

        return TestUtils.isRunningInstrumentalTest.get();
    }
}
