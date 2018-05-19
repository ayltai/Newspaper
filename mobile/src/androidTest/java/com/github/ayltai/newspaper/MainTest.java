package com.github.ayltai.newspaper;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import com.github.ayltai.newspaper.util.MoreTestUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public final class MainTest extends BaseTest {
    @Test
    public void mainTest() {
        Espresso.onView(ViewMatchers.withId(R.id.action_more))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        Espresso.onView(ViewMatchers.withId(R.id.action_refresh))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        Espresso.onView(ViewMatchers.withId(R.id.action_more))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        Espresso.onView(ViewMatchers.withId(R.id.action_refresh))
            .check(ViewAssertions.doesNotExist());

        Espresso.onView(ViewMatchers.withId(R.id.action_more))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        Espresso.onView(ViewMatchers.withId(R.id.action_refresh))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_LONG);

        Espresso.onView(ViewMatchers.withId(R.id.action_refresh))
            .check(ViewAssertions.doesNotExist());
    }
}
