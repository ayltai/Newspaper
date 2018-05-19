package com.github.ayltai.newspaper;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import com.github.ayltai.newspaper.util.MoreTestUtils;
import com.google.android.libraries.cloudtesting.screenshots.ScreenShotter;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public final class ViewStyleTest extends BaseTest {
    @Test
    public void viewStyleTest() {
        // Clicks More button
        Espresso.onView(ViewMatchers.withId(R.id.action_more))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        // Clicks Settings button
        Espresso.onView(ViewMatchers.withId(R.id.action_settings))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        // Clicks Settings tab
        Espresso.onView(ViewMatchers.withText("Settings"))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".settings.cozy", this.testRule.getActivity());

        // Checks that Cozy view style is checked
        Espresso.onView(MoreTestUtils.childAtPosition(Matchers.allOf(
            ViewMatchers.withId(R.id.linearLayout),
            MoreTestUtils.childAtPosition(ViewMatchers.withParent(Matchers.allOf(
                ViewMatchers.withId(R.id.viewPager),
                MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(Matchers.allOf(
                    ViewMatchers.withId(R.id.design_bottom_sheet),
                    MoreTestUtils.childAtPosition(ViewMatchers.withId(R.id.coordinator), 1)), 0), 0), 1))), 0)), 0))
            .check(ViewAssertions.matches(MoreTestUtils.isChecked()));

        // De-selects Cozy view style
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withText("Cozy layout"),
            ViewMatchers.withParent(ViewMatchers.withId(R.id.linearLayout))))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        // Clicks Apply Changes
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_ok),
            ViewMatchers.withText("Apply changes")))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_MEDIUM);

        // Clicks More button
        Espresso.onView(ViewMatchers.withId(R.id.action_more))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        // Clicks Settings button
        Espresso.onView(ViewMatchers.withId(R.id.action_settings))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        // Clicks Settings tab
        Espresso.onView(ViewMatchers.withText("Settings"))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".settings.compact", this.testRule.getActivity());

        // Checks that Cozy view style is unchecked
        Espresso.onView(MoreTestUtils.childAtPosition(Matchers.allOf(
            ViewMatchers.withId(R.id.linearLayout),
            MoreTestUtils.childAtPosition(ViewMatchers.withParent(Matchers.allOf(
                ViewMatchers.withId(R.id.viewPager),
                MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(Matchers.allOf(
                    ViewMatchers.withId(R.id.design_bottom_sheet),
                    MoreTestUtils.childAtPosition(ViewMatchers.withId(R.id.coordinator), 1)), 0), 0), 1))), 0)), 0))
            .check(ViewAssertions.matches(MoreTestUtils.isNotChecked()));

        // Restores to Cozy view style
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withText("Cozy layout"),
            ViewMatchers.withParent(ViewMatchers.withId(R.id.linearLayout))))
            .perform(ViewActions.scrollTo(), ViewActions.click());
    }
}
