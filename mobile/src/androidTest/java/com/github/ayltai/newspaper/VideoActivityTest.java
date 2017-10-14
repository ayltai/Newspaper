package com.github.ayltai.newspaper;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.ayltai.newspaper.app.VideoActivity;

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

    @Test
    public void test() {
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withClassName(Matchers.is("com.google.android.exoplayer2.ui.SimpleExoPlayerView")),
            ViewMatchers.withId(R.id.video),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
