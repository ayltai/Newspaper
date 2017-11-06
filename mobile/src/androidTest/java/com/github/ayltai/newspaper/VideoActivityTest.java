package com.github.ayltai.newspaper;

import java.io.File;

import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.ayltai.newspaper.app.VideoActivity;
import com.github.ayltai.newspaper.util.MoreTestUtils;

@SmallTest
@RunWith(AndroidJUnit4.class)
public final class VideoActivityTest {
    @Rule
    public ActivityTestRule<VideoActivity> testRule = new ActivityTestRule<VideoActivity>(VideoActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            return VideoActivity.createIntent(InstrumentationRegistry.getInstrumentation().getTargetContext(), "http://techslides.com/demos/sample-videos/small.mp4", false, 0);
        }
    };

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @CallSuper
    @Before
    public void setUp() {
        final File file = new File("/sdcard/tmp/code-coverage/connected");
        if (!file.exists()) file.mkdirs();

        MoreTestUtils.sleep(MoreTestUtils.DURATION_MEDIUM);
    }

    @CallSuper
    @After
    public void tearDown() {
        MoreTestUtils.sleep(MoreTestUtils.DURATION_MEDIUM);
    }

    @Test
    public void test() {
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withClassName(Matchers.is("com.google.android.exoplayer2.ui.SimpleExoPlayerView")),
            ViewMatchers.withId(R.id.video),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
