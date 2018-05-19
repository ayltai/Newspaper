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
public final class FeaturedItemTest extends BaseTest {
    @Test
    public void featuredItemTest() {
        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".featuredItem", this.testRule.getActivity());

        // Checks that Featured News is displayed
        Espresso.onView(ViewMatchers.withId(R.id.featured_image))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        // Clicks Featured News
        Espresso.onView(ViewMatchers.withId(R.id.featured_image))
            .perform(ViewActions.click());

        MoreTestUtils.sleep(MoreTestUtils.DURATION_SHORT);

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".featuredItem.details", this.testRule.getActivity());

        // Checks that the news image is displayed within the toolbar
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.image),
            MoreTestUtils.childAtPosition(MoreTestUtils.childAtPosition(ViewMatchers.withId(R.id.image_container), 0), 0)))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
}
