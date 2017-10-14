package com.github.ayltai.newspaper.widget;

import android.support.v7.widget.LinearLayoutManager;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import com.github.ayltai.newspaper.UnitTest;

public final class SmartRecyclerViewTest extends UnitTest {
    @Test(expected = IllegalArgumentException.class)
    public void test() {
        new SmartRecyclerView(RuntimeEnvironment.application).setLayoutManager(new LinearLayoutManager(RuntimeEnvironment.application));
    }
}
