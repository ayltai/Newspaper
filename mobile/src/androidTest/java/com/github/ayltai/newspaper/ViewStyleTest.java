package com.github.ayltai.newspaper;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.android.libraries.cloudtesting.screenshots.ScreenShotter;

import com.github.ayltai.newspaper.util.MoreTestUtils;

@MediumTest
@RunWith(AndroidJUnit4.class)
public final class ViewStyleTest extends BaseTest {
    @Test
    public void viewStyleTest() {
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_more),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_settings),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withText("Settings"),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".settings.cozy", this.testRule.getActivity());

        Espresso.onView(Matchers.allOf(
            MoreTestUtils.childAtPosition(Matchers.allOf(
                ViewMatchers.withId(R.id.linearLayout),
                MoreTestUtils.childAtPosition(ViewMatchers.withParent(Matchers.allOf(
                    ViewMatchers.withId(R.id.viewPager),
                    MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(Matchers.allOf(
                        ViewMatchers.withId(R.id.design_bottom_sheet),
                        MoreTestUtils.childAtPosition(ViewMatchers.withId(R.id.coordinator), 1)), 0), 0), 1))), 0)), 0),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(MoreTestUtils.isChecked()));

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withText("Cozy layout"),
            ViewMatchers.withParent(ViewMatchers.withId(R.id.linearLayout)),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_ok),
            ViewMatchers.withText("Apply changes"),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_MEDIUM);

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_more),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_settings),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withText("Settings"),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".settings.compact", this.testRule.getActivity());

        Espresso.onView(Matchers.allOf(
            MoreTestUtils.childAtPosition(Matchers.allOf(
                ViewMatchers.withId(R.id.linearLayout),
                MoreTestUtils.childAtPosition(ViewMatchers.withParent(Matchers.allOf(
                    ViewMatchers.withId(R.id.viewPager),
                    MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(Matchers.allOf(
                        ViewMatchers.withId(R.id.design_bottom_sheet),
                        MoreTestUtils.childAtPosition(ViewMatchers.withId(R.id.coordinator), 1)), 0), 0), 1))), 0)), 0),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(MoreTestUtils.isNotChecked()));
    }
}
