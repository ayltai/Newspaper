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

@LargeTest
@RunWith(AndroidJUnit4.class)
public final class BookmarkedNewsTest extends BaseTest {
    @Test
    public void bookmarkNewsTest() {
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_bookmark),
            ViewMatchers.withParent(Matchers.allOf(
                ViewMatchers.withId(R.id.bb_bottom_bar_item_container),
                ViewMatchers.withParent(Matchers.allOf(
                    ViewMatchers.withId(R.id.bb_bottom_bar_outer_container),
                    ViewMatchers.withParent(ViewMatchers.withId(R.id.bottomBar)))))),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        BaseTest.sleep(BaseTest.DURATION_SHORT);

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_more),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        BaseTest.sleep(BaseTest.DURATION_SHORT);

        Espresso.onView(ViewMatchers.withId(R.id.action_clear_all))
            .perform(ViewActions.click());

        BaseTest.sleep(BaseTest.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".bookmark.empty", this.testRule.getActivity());

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.empty_title),
            ViewMatchers.withText("Nothing here"),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_news),
            ViewMatchers.withParent(Matchers.allOf(
                ViewMatchers.withId(R.id.bb_bottom_bar_item_container),
                ViewMatchers.withParent(Matchers.allOf(
                    ViewMatchers.withId(R.id.bb_bottom_bar_outer_container),
                    ViewMatchers.withParent(ViewMatchers.withId(R.id.bottomBar)))))),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        BaseTest.sleep(BaseTest.DURATION_LONG);

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.featured_image),
            ViewMatchers.withParent(ViewMatchers.withId(R.id.container)),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        BaseTest.sleep(BaseTest.DURATION_SHORT);

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_bookmark),
            ViewMatchers.withContentDescription("Bookmark"),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        BaseTest.sleep(BaseTest.DURATION_SHORT);

        Espresso.pressBack();

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_bookmark),
            ViewMatchers.withParent(Matchers.allOf(
                ViewMatchers.withId(R.id.bb_bottom_bar_item_container),
                ViewMatchers.withParent(Matchers.allOf(
                    ViewMatchers.withId(R.id.bb_bottom_bar_outer_container),
                    ViewMatchers.withParent(ViewMatchers.withId(R.id.bottomBar)))))),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        BaseTest.sleep(BaseTest.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".bookmark", this.testRule.getActivity());

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.image),
            childAtPosition(Matchers.allOf(
                ViewMatchers.withId(R.id.image),
                childAtPosition(Matchers.allOf(
                    ViewMatchers.withId(R.id.container),
                    childAtPosition(childAtPosition(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class), 0), 0)), 0)), 0),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
