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

@MediumTest
@RunWith(AndroidJUnit4.class)
public final class SettingsDialogTest extends BaseTest {
    @Test
    public void settingsDialogTest() {
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_more),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        BaseTest.sleep(BaseTest.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".moreAction", this.testRule.getActivity());

        Espresso.onView(ViewMatchers.withId(R.id.action_settings))
            .perform(ViewActions.click());

        BaseTest.sleep(BaseTest.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".settingsDialog", this.testRule.getActivity());

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_ok),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
