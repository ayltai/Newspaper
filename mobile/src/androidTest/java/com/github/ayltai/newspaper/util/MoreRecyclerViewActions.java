package com.github.ayltai.newspaper.util;

import android.support.annotation.NonNull;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public  final class MoreRecyclerViewActions implements ViewAction {
    private final int position;

    public MoreRecyclerViewActions(final int position) {
        this.position = position;
    }

    @NonNull
    @Override
    public Matcher<View> getConstraints() {
        return Matchers.allOf(ViewMatchers.isAssignableFrom(RecyclerView.class), ViewMatchers.isDisplayed());
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
