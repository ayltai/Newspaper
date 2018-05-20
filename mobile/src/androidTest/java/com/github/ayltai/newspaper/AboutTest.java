package com.github.ayltai.newspaper;

import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.libraries.cloudtesting.screenshots.ScreenShotter;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public final class AboutTest extends BaseTest {
    @Test
    public void visitWebsite() {
        // Clicks About bottom tab
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_about),
            ViewMatchers.withParent(ViewMatchers.withParent(ViewMatchers.withId(R.id.bottomNavigationView)))))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".visitWebsite", this.testRule.getActivity());

        final Matcher<Intent> intent = Matchers.allOf(IntentMatchers.hasAction(Intent.ACTION_VIEW), IntentMatchers.hasData("https://github.com/ayltai/Newspaper"));

        Intents.init();
        Intents.intending(intent).respondWith(new Instrumentation.ActivityResult(0, null));

        // Clicks Visit button
        Espresso.onView(ViewMatchers.withId(R.id.visit_container))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        // Checks that the fired Intent is correct
        Intents.intended(intent);
        Intents.release();
    }

    @Test
    public void rateApp() {
        // Clicks About bottom tab
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_about),
            ViewMatchers.withParent(ViewMatchers.withParent(ViewMatchers.withId(R.id.bottomNavigationView)))))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        ScreenShotter.takeScreenshot(this.getClass().getSimpleName() + ".rateApp", this.testRule.getActivity());

        final Matcher<Intent> intent = Matchers.allOf(IntentMatchers.hasAction(Intent.ACTION_VIEW), IntentMatchers.hasData("market://details?id=" + BuildConfig.APPLICATION_ID));

        Intents.init();
        Intents.intending(intent).respondWith(new Instrumentation.ActivityResult(0, null));

        // Clicks Rate button
        Espresso.onView(ViewMatchers.withId(R.id.rate_container))
            .perform(ViewActions.scrollTo(), ViewActions.click());

        // Checks that the fired Intent is correct
        Intents.intended(intent);
        Intents.release();
    }
}
