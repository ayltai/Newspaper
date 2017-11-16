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

import com.github.ayltai.newspaper.util.MoreTestUtils;

@LargeTest
@RunWith(AndroidJUnit4.class)
public final class HistoricalNewsTest extends BaseTest {
    @Test
    public void historicalNewsTest() {
        // Clicks Histories bottom tab
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_history),
            ViewMatchers.withParent(Matchers.allOf(
                ViewMatchers.withId(R.id.bb_bottom_bar_item_container),
                ViewMatchers.withParent(Matchers.allOf(
                    ViewMatchers.withId(R.id.bb_bottom_bar_outer_container),
                    ViewMatchers.withParent(ViewMatchers.withId(R.id.bottomBar)))))),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        // Clicks More button
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_more),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        // Clicks Clear All button to make sure there is no existing news
        Espresso.onView(ViewMatchers.withId(R.id.action_clear_all))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".history.empty", this.testRule.getActivity());

        // Checks that the empty placeholder is displayed
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.empty_title),
            ViewMatchers.withText("Nothing here"),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Clicks News bottom tab
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_news),
            ViewMatchers.withParent(Matchers.allOf(
                ViewMatchers.withId(R.id.bb_bottom_bar_item_container),
                ViewMatchers.withParent(Matchers.allOf(
                    ViewMatchers.withId(R.id.bb_bottom_bar_outer_container),
                    ViewMatchers.withParent(ViewMatchers.withId(R.id.bottomBar)))))),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_LONG);

        // Clicks Featured News
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.featured_image),
            ViewMatchers.withParent(ViewMatchers.withId(R.id.container)),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        // Goes back the News list
        Espresso.pressBack();

        // Clicks Histories bottom tab
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_history),
            ViewMatchers.withParent(Matchers.allOf(
                ViewMatchers.withId(R.id.bb_bottom_bar_item_container),
                ViewMatchers.withParent(Matchers.allOf(
                    ViewMatchers.withId(R.id.bb_bottom_bar_outer_container),
                    ViewMatchers.withParent(ViewMatchers.withId(R.id.bottomBar)))))),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".history", this.testRule.getActivity());

        // Checks that there is a historical news item
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.image),
            MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(ViewMatchers.withId(R.id.container), 0), 3),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Clicks Search button
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_search),
            ViewMatchers.withContentDescription("Search"),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search", this.testRule.getActivity());

        // Types a query into the search text box
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.search_src_text),
            ViewMatchers.isDisplayed()))
            .perform(
                ViewActions.replaceText("asdfqwerzxcv"),
                ViewActions.closeSoftKeyboard());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.textBox.filled", this.testRule.getActivity());

        // Checks that no search results returned
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.image),
            MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(ViewMatchers.withId(R.id.container), 0), 3),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.doesNotExist());

        // Clears the search query
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.search_close_btn),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.textBox.cleared", this.testRule.getActivity());

        // Collapses the search text box
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withContentDescription("Collapse"),
            ViewMatchers.withParent(ViewMatchers.withId(R.id.toolbar)),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.collapsed", this.testRule.getActivity());

        // Checks that there is a bookmarked news item
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.image),
            MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(ViewMatchers.withId(R.id.container), 0), 3),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
