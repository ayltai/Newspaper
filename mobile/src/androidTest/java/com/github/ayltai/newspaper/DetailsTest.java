package com.github.ayltai.newspaper;

import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.github.ayltai.newspaper.util.MoreTestUtils;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public final class DetailsTest extends BaseTest {
    @Test
    public void detailsTest() {
        Espresso.onView(MoreTestUtils.first(Matchers.allOf(
            ViewMatchers.withClassName(Matchers.is("com.github.ayltai.newspaper.widget.SmartRecyclerView")),
            ViewMatchers.withId(R.id.recyclerView))))
            .perform(RecyclerViewActions.actionOnItemAtPosition(2, ViewActions.click()));

        MoreTestUtils.sleep(MoreTestUtils.DURATION_LONG);

        Espresso.onView(ViewMatchers.withId(R.id.avatar))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        Espresso.onView(ViewMatchers.withId(R.id.source))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_bookmark),
            ViewMatchers.withContentDescription("Bookmark")))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_share),
            ViewMatchers.withContentDescription("Share")))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.title),
            ViewMatchers.withParent(Matchers.allOf(
                ViewMatchers.withClassName(Matchers.is("android.widget.LinearLayout")),
                ViewMatchers.withParent(ViewMatchers.withId(R.id.container))
            ))))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.description),
            ViewMatchers.withParent(ViewMatchers.withClassName(Matchers.is("android.widget.LinearLayout"))),
            ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        final Matcher<Intent> shareIntent = Matchers.allOf(
            IntentMatchers.hasAction(Intent.ACTION_CHOOSER),
            IntentMatchers.hasExtra(Matchers.is(Intent.EXTRA_INTENT), Matchers.allOf(IntentMatchers.hasAction(Intent.ACTION_SEND), IntentMatchers.hasExtraWithKey(Intent.EXTRA_TEXT), IntentMatchers.hasType("text/plain"))));

        Intents.init();
        Intents.intending(shareIntent).respondWith(new Instrumentation.ActivityResult(0, null));

        // Clicks Share button
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_share),
            ViewMatchers.withContentDescription("Share")))
            .perform(ViewActions.click());

        // Checks that the fired Intent is correct
        Intents.intended(shareIntent);
        Intents.release();

        // Clicks Text-to-Speech button
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(R.id.action_text_to_speech),
            ViewMatchers.withContentDescription("Text to speech")))
            .perform(ViewActions.click());
    }
}
