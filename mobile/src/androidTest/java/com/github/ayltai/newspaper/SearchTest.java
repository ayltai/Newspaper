package com.github.ayltai.newspaper;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.github.ayltai.newspaper.util.MoreTestUtils;
import com.google.android.libraries.cloudtesting.screenshots.ScreenShotter;

import org.hamcrest.Matchers;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public final class SearchTest extends BaseTest {
    @Test
    public void searchWithResultsTest() {
        // Checks that Featured News is displayed
        Espresso.onView(ViewMatchers.withId(R.id.featured_image))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        // Clicks Search button
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_search),
            ViewMatchers.withContentDescription("Search")))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search", this.testRule.getActivity());

        // Checks that the search text box is displayed
        Espresso.onView(ViewMatchers.withId(R.id.search_src_text))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.textBox", this.testRule.getActivity());

        // Types a query into the search text box
        Espresso.onView(ViewMatchers.withId(R.id.search_src_text))
            .perform(
                ViewActions.scrollTo(),
                ViewActions.replaceText("香港"),
                ViewActions.closeSoftKeyboard());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.textBox.filled", this.testRule.getActivity());

        // Checks that Featured News is not displayed in the search results
        Espresso.onView(ViewMatchers.withId(R.id.featured_image))
            .check(ViewAssertions.doesNotExist());

        // Clears the search query
        Espresso.onView(ViewMatchers.withId(R.id.search_close_btn))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.textBox.cleared", this.testRule.getActivity());

        // Collapses the search text box
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withContentDescription("Collapse"),
            ViewMatchers.withParent(ViewMatchers.withId(R.id.toolbar))))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.collapsed", this.testRule.getActivity());

        // Checks that Featured News is displayed
        Espresso.onView(ViewMatchers.withId(R.id.featured_image))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @Test
    public void searchWithoutResultsTest() {
        // Checks that Featured News is displayed
        Espresso.onView(ViewMatchers.withId(R.id.featured_image))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        // Clicks Search button
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_search),
            ViewMatchers.withContentDescription("Search")))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search", this.testRule.getActivity());

        // Checks that the search text box is displayed
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.search_src_text),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.textBox", this.testRule.getActivity());

        // Types a query into the search text box
        Espresso.onView(ViewMatchers.withId(R.id.search_src_text))
            .perform(
                ViewActions.scrollTo(),
                ViewActions.replaceText("asdfqwerzxcv"),
                ViewActions.closeSoftKeyboard());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.textBox.filled", this.testRule.getActivity());

        // Checks that no search results returned
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.image),
            MoreTestUtils.childAtPosition(Matchers.allOf(
                ViewMatchers.withId(R.id.image),
                MoreTestUtils.childAtPosition(Matchers.allOf(
                    ViewMatchers.withId(R.id.container),
                    MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class), 0), 0)), 0)), 0)))
            .check(ViewAssertions.doesNotExist());

        // Clears the search query
        Espresso.onView(ViewMatchers.withId(R.id.search_close_btn))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.textBox.cleared", this.testRule.getActivity());

        // Collapses the search text box
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withContentDescription("Collapse"),
            ViewMatchers.withParent(ViewMatchers.withId(R.id.toolbar))))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".search.collapsed", this.testRule.getActivity());

        // Checks that Featured News is displayed
        Espresso.onView(ViewMatchers.withId(R.id.featured_image))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
}
