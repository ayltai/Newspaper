package com.github.ayltai.newspaper;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.github.ayltai.newspaper.util.MoreTestUtils;
import com.google.android.libraries.cloudtesting.screenshots.ScreenShotter;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public final class BookmarkedNewsTest extends BaseTest {
    @Test
    public void bookmarkNewsTest() {
        // Clicks Bookmarks bottom tab
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_bookmark),
            ViewMatchers.withParent(ViewMatchers.withParent(ViewMatchers.withId(R.id.bottomNavigationView)))))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        // Clicks More button
        Espresso.onView(ViewMatchers.withId(R.id.action_more))
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
            ViewMatchers.withText("You don't have any favorite news yet")))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Clicks News bottom tab
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_news),
            ViewMatchers.withParent(ViewMatchers.withParent(ViewMatchers.withId(R.id.bottomNavigationView)))))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_LONG);

        // Clicks Featured News
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.featured_image),
            ViewMatchers.withParent(ViewMatchers.withId(R.id.container)),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_LONG);

        // Clicks Bookmark button
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_bookmark),
            ViewMatchers.withContentDescription("Bookmark")))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        // Goes back to News list
        Espresso.pressBack();

        // Clicks Bookmarks bottom tab
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_bookmark),
            ViewMatchers.withParent(ViewMatchers.withParent(ViewMatchers.withId(R.id.bottomNavigationView)))))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".bookmark", this.testRule.getActivity());

        // Checks that there is a bookmarked news item
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.image),
            MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(ViewMatchers.withId(R.id.container), 0), 3)))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Clicks Search button
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_search),
            ViewMatchers.withContentDescription("Search")))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search", this.testRule.getActivity());

        // Types a query into the search text box
        Espresso.onView(ViewMatchers.withId(R.id.search_src_text))
            .perform(
                ViewActions.replaceText("asdfqwerzxcv"),
                ViewActions.closeSoftKeyboard());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.textBox.filled", this.testRule.getActivity());

        // Checks that no search results returned
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.image),
            MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(ViewMatchers.withId(R.id.container), 0), 3)))
            .check(ViewAssertions.doesNotExist());

        // Clears the search query
        Espresso.onView(ViewMatchers.withId(R.id.search_close_btn))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.textBox.cleared", this.testRule.getActivity());

        // Collapses the search text box
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withContentDescription("Collapse"),
            ViewMatchers.withParent(ViewMatchers.withId(R.id.toolbar))))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.collapsed", this.testRule.getActivity());

        // Checks that there is a bookmarked news item
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.image),
            MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(ViewMatchers.withId(R.id.container), 0), 3)))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
