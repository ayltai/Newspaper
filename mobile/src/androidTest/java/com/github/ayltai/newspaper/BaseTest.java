package com.github.ayltai.newspaper;

import java.io.File;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import com.github.ayltai.newspaper.app.MainActivity;

public abstract class BaseTest {
    public static final int DURATION_SHORT  = 3;
    public static final int DURATION_MEDIUM = 6;
    public static final int DURATION_LONG   = 12;

    @Rule
    public ActivityTestRule<MainActivity> testRule = new ActivityTestRule<>(MainActivity.class);

    @CallSuper
    @Before
    public void setUp() {
        final File file = new File("/sdcard/tmp/code-coverage/connected");
        if (!file.exists()) file.mkdirs();

        BaseTest.sleep(BaseTest.DURATION_MEDIUM);
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

    @NonNull
    protected static Matcher<View> childAtPosition(@NonNull final Matcher<View> parentMatcher, final int position) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(@NonNull final Description description) {
                parentMatcher.describeTo(description.appendText("Child at position " + position + " in parent "));
            }

            @Override
            public boolean matchesSafely(@NonNull final View view) {
                final ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent) && view.equals(((ViewGroup)parent).getChildAt(position));
            }
        };
    }
}
