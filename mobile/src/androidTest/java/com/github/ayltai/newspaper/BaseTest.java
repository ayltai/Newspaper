package com.github.ayltai.newspaper;

import java.io.File;

import android.support.annotation.CallSuper;
import android.support.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import com.github.ayltai.newspaper.app.MainActivity;

public class BaseTest {
    public static final int DURATION_SHORT  = 6;
    public static final int DURATION_MEDIUM = 12;
    public static final int DURATION_LONG   = 18;

    @Rule
    public ActivityTestRule<MainActivity> testRule = new ActivityTestRule<>(MainActivity.class);

    @CallSuper
    @Before
    public void setUp() {
        final File file = new File("/sdcard/tmp/code-coverage/connected");
        if (!file.exists()) file.mkdirs();

        BaseTest.sleep(BaseTest.DURATION_SHORT);
    }

    @CallSuper
    @After
    public void tearDown() {
    }

    protected static void sleep(final int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }
}
