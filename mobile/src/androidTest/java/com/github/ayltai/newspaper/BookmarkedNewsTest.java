package com.github.ayltai.newspaper;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.hamcrest.Matchers;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.android.libraries.cloudtesting.screenshots.ScreenShotter;

import com.github.ayltai.newspaper.util.MoreTestUtils;

@LargeTest
@RunWith(AndroidJUnit4.class)
public final class BookmarkedNewsTest extends BaseTest {
    @Test
    public void bookmarkNewsTest() {
        // Clicks Bookmarks bottom tab
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_bookmark),
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

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".bookmark.empty", this.testRule.getActivity());

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

        // Clicks Bookmark button
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_bookmark),
            ViewMatchers.withContentDescription("Bookmark"),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        // Goes back to News list
        Espresso.pressBack();

        // Clicks Bookmarks bottom tab
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_bookmark),
            ViewMatchers.withParent(Matchers.allOf(
                ViewMatchers.withId(R.id.bb_bottom_bar_item_container),
                ViewMatchers.withParent(Matchers.allOf(
                    ViewMatchers.withId(R.id.bb_bottom_bar_outer_container),
                    ViewMatchers.withParent(ViewMatchers.withId(R.id.bottomBar)))))),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".bookmark", this.testRule.getActivity());

        // Checks that there is a bookmarked news item
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.image),
            MoreTestUtils.childAtPosition(Matchers.allOf(
                ViewMatchers.withId(R.id.image),
                MoreTestUtils.childAtPosition(Matchers.allOf(
                    ViewMatchers.withId(R.id.container),
                    MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class), 0), 0)), 0)), 0),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
