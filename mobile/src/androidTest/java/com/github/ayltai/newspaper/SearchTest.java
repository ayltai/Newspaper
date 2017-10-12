package com.github.ayltai.newspaper;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.android.libraries.cloudtesting.screenshots.ScreenShotter;

@LargeTest
@RunWith(AndroidJUnit4.class)
public final class SearchTest extends BaseTest {
    @Test
    public void searchTest() {
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.featured_image),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_search),
            ViewMatchers.withContentDescription("Search"),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        BaseTest.sleep(BaseTest.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search", this.testRule.getActivity());

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.search_src_text),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        BaseTest.sleep(BaseTest.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.textBox", this.testRule.getActivity());

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.search_src_text),
            ViewMatchers.isDisplayed()))
            .perform(
                ViewActions.replaceText("香港"),
                ViewActions.closeSoftKeyboard());

        BaseTest.sleep(BaseTest.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.textBox.filled", this.testRule.getActivity());

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.featured_image),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.doesNotExist());

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.search_close_btn),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        BaseTest.sleep(BaseTest.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.textBox.cleared", this.testRule.getActivity());

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withContentDescription("Collapse"),
            ViewMatchers.withParent(ViewMatchers.withId(R.id.toolbar)),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        BaseTest.sleep(BaseTest.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.collapsed", this.testRule.getActivity());

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.featured_image),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
