package com.github.ayltai.newspaper.app;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.android.libraries.cloudtesting.screenshots.ScreenShotter;

import com.github.ayltai.newspaper.BaseTest;
import com.github.ayltai.newspaper.R;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class FeaturedItemTest extends BaseTest {
    @Test
    public void featuredItemTest() {
        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".featuredItem", this.testRule.getActivity());

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.featured_image),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.featured_image),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        BaseTest.sleep(DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".featuredItem.details", this.testRule.getActivity());

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.image),
            BaseTest.childAtPosition(Matchers.allOf(
                ViewMatchers.withId(R.id.image),
                BaseTest.childAtPosition(BaseTest.childAtPosition(Matchers.allOf(
                    ViewMatchers.withId(R.id.image_container),
                    BaseTest.childAtPosition(ViewMatchers.withId(R.id.collapsingToolbarLayout), 0)), 0), 0)), 0),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
