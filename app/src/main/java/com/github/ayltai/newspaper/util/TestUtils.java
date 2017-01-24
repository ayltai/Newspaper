package com.github.ayltai.newspaper.util;

import java.util.concurrent.atomic.AtomicBoolean;

public final class TestUtils {
    private static AtomicBoolean isRunningTest;

    private TestUtils() {
    }

    public static synchronized boolean isRunningTest() {
        if (TestUtils.isRunningTest == null) {
            boolean isRunningTest = false;

            try {
                Class.forName("android.support.test.espresso.Espresso");
                isRunningTest = true;
            } catch (final ClassNotFoundException e) {
                // Ignored
            }

            TestUtils.isRunningTest = new AtomicBoolean(isRunningTest);
        }

        return TestUtils.isRunningTest.get();
    }
}
