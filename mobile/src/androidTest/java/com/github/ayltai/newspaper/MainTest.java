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

import com.github.ayltai.newspaper.util.MoreTestUtils;

@MediumTest
@RunWith(AndroidJUnit4.class)
public final class MainTest extends BaseTest {
    @Test
    public void mainTest() {
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_more),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_refresh),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_more),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_refresh),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.doesNotExist());

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_more),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_refresh),
            ViewMatchers.isDisplayed()))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_LONG);

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_refresh),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.doesNotExist());
    }
}
