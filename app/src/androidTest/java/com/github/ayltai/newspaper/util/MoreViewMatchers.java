package com.github.ayltai.newspaper.util;

import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.core.Is;

public final class MoreViewMatchers {
    private MoreViewMatchers() {
    }

    public static Matcher<View> withTitle(@Nullable final String title) {
        final Matcher<String> stringMatcher = Is.is(title);

        return new BoundedMatcher<View, CollapsingToolbarLayout>(CollapsingToolbarLayout.class) {
            @Override
            public void describeTo(final Description description) {
                stringMatcher.describeTo(description.appendText("with title: "));
            }

            @Override
            public boolean matchesSafely(final CollapsingToolbarLayout toolbar) {
                if (toolbar.getTitle() == null) return title == null;

                return stringMatcher.matches(toolbar.getTitle().toString());
            }
        };
    }
}
