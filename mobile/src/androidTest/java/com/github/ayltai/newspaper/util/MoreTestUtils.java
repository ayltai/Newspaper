package com.github.ayltai.newspaper.util;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public final class MoreTestUtils {
    public static final int DURATION_SHORT  = 3;
    public static final int DURATION_MEDIUM = 6;
    public static final int DURATION_LONG   = 12;

    public static void sleep(final int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public static Matcher<View> isSelected() {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(@NonNull final Description description) {
                description.appendText("matching the selected element");
            }

            @Override
            public boolean matchesSafely(@NonNull final View item) {
                return item.isSelected();
            }
        };
    }

    @NonNull
    public static Matcher<View> isNotSelected() {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(@NonNull final Description description) {
                description.appendText("matching the non-selected element");
            }

            @Override
            public boolean matchesSafely(@NonNull final View item) {
                return !item.isSelected();
            }
        };
    }

    @NonNull
    public static <T> Matcher<T> first(@NonNull final Matcher<T> matcher) {
        return new BaseMatcher<T>() {
            private boolean isFirst = true;

            @Override
            public void describeTo(@NonNull final Description description) {
                description.appendText("matching the first element");
            }

            @Override
            public boolean matches(@NonNull final Object element) {
                if (isFirst && matcher.matches(element)) {
                    isFirst = false;

                    return true;
                }

                return false;
            }
        };
    }

    @NonNull
    public static Matcher<View> childAtPosition(@NonNull final Matcher<View> parentMatcher, final int position) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(@NonNull final Description description) {
                parentMatcher.describeTo(description.appendText("Child at position " + position + " in parent "));
            }

            @Override
            public boolean matchesSafely(@NonNull final View view) {
                final ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent) && view.equals(((ViewGroup)parent).getChildAt(position));
            }
        };
    }

    @NonNull
    public static MoreRecyclerViewActions smoothScrollToPosition(final int position) {
        return new MoreRecyclerViewActions(position);
    }
}
