package com.github.ayltai.newspaper;

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
