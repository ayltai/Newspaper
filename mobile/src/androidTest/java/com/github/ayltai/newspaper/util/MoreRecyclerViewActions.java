package com.github.ayltai.newspaper.util;

import android.support.annotation.NonNull;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.allOf;

public  final class MoreRecyclerViewActions implements ViewAction {
    private final int position;

    public MoreRecyclerViewActions(int position) {
        this.position = position;
    }

    @NonNull
    @Override
    public Matcher<View> getConstraints() {
        return allOf(isAssignableFrom(RecyclerView.class), isDisplayed());
    }

    @NonNull
    @Override
    public String getDescription() {
        return "smooth scroll RecyclerView to position: " + position;
    }

    @Override
    public void perform(final UiController uiController, final View view) {
        ((RecyclerView)view).smoothScrollToPosition(position);
    }
}
