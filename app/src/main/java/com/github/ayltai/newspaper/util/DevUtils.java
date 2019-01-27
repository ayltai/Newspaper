package com.github.ayltai.newspaper.util;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import android.os.Build;
import android.os.StrictMode;

import androidx.annotation.NonNull;

import com.github.ayltai.newspaper.BuildConfig;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DevUtils {
    private AtomicBoolean isRunningUnitTest;
    private AtomicBoolean isRunningInstrumentedTest;

    public boolean isLoggable() {
        return BuildConfig.DEBUG && !DevUtils.isRunningTests();
    }

    public boolean isRunningTests() {
        return DevUtils.isRunningUnitTest() || DevUtils.isRunningInstrumentedTest();
    }

    public synchronized boolean isRunningUnitTest() {
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

    public synchronized boolean isRunningInstrumentedTest() {
        if (DevUtils.isRunningInstrumentedTest == null) {
            boolean isRunningInstrumentedTest = false;

            try {
                Class.forName("androidx.test.espresso.Espresso");
                isRunningInstrumentedTest = true;
            } catch (final ClassNotFoundException e) {
                // Ignored
            }

            DevUtils.isRunningInstrumentedTest = new AtomicBoolean(isRunningInstrumentedTest);
        }

        return DevUtils.isRunningInstrumentedTest.get();
    }

    @Nonnull
    @NonNull
    public StrictMode.ThreadPolicy newThreadPolicy() {
        final StrictMode.ThreadPolicy.Builder builder = new StrictMode.ThreadPolicy.Builder()
            .detectCustomSlowCalls()
            .detectNetwork()
            .penaltyLog()
            .penaltyFlashScreen();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) builder.detectResourceMismatches();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) builder.detectUnbufferedIo();

        return builder.build();
    }

    @Nonnull
    @NonNull
    public StrictMode.VmPolicy newVmPolicy() {
        final StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectLeakedClosableObjects()
            .detectLeakedRegistrationObjects()
            .detectLeakedSqlLiteObjects()
            .penaltyLog();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) builder.detectFileUriExposure();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) builder.detectContentUriWithoutPermission();

        return builder.build();
    }
}
