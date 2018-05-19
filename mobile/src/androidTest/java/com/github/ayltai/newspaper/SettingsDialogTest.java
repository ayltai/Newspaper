package com.github.ayltai.newspaper;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.github.ayltai.newspaper.util.MoreTestUtils;
import com.google.android.libraries.cloudtesting.screenshots.ScreenShotter;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public final class SettingsDialogTest extends BaseTest {
    @Test
    public void settingsDialogTest() {
        // Clicks More button
        Espresso.onView(ViewMatchers.withId(R.id.action_more))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".moreAction", this.testRule.getActivity());

        // Clicks Settings tab
        Espresso.onView(ViewMatchers.withId(R.id.action_settings))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".settingsDialog", this.testRule.getActivity());

        // Checks that Apply button is displayed
        Espresso.onView(ViewMatchers.withId(R.id.action_ok))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        // Clicks Categories tab
        Espresso.onView(ViewMatchers.withText("Categories"))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".settingsDialog.category.selected", this.testRule.getActivity());

        // De-selects a category
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withText("兩岸"),
            ViewMatchers.withParent(ViewMatchers.withId(R.id.flowLayout))))
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

        // Clicks Categories tab
        Espresso.onView(ViewMatchers.withText("Categories"))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".settingsDialog.notSelected", this.testRule.getActivity());

        // Checks if the specific category is de-selected
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withText("兩岸"),
            ViewMatchers.withParent(ViewMatchers.withId(R.id.flowLayout))))
            .check(ViewAssertions.matches(MoreTestUtils.isNotSelected()));
    }
}
