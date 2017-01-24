package com.github.ayltai.newspaper;

import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.ayltai.newspaper.util.SuppressFBWarnings;

@RunWith(AndroidJUnit4.class)
@LargeTest
public final class MainActivityTest {
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @SuppressWarnings("deprecation")
    @Before
    public void setUp() {
        final Activity activity = this.activityRule.getActivity();

        // Makes sure the device is unlocked
        activity.runOnUiThread(() -> activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));
    }

    @Test
    public void tapThroughAllTabs() {
        for (final String category : InstrumentationRegistry.getTargetContext().getResources().getStringArray(R.array.pref_category_entries)) {
            Espresso.onView(Matchers.allOf(ViewMatchers.withText(category), ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.tabLayout))))
                .perform(ViewActions.click())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void openFirstItem() {
        // Switches to "HK" tab
        Espresso.onView(Matchers.allOf(ViewMatchers.withText(InstrumentationRegistry.getTargetContext().getResources().getStringArray(R.array.pref_category_entries)[1]), ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.tabLayout))))
            .perform(ViewActions.click());

        // Opens first item
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.recyclerView), ViewMatchers.isDisplayed()))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));

        // Asserts that the title is expected
        Espresso.onView(ViewMatchers.withId(R.id.title))
            .check(ViewAssertions.matches(ViewMatchers.withText("馬時亨冀3月就票價機制達協議 不覺得有驚天動地改變")));
    }
}
