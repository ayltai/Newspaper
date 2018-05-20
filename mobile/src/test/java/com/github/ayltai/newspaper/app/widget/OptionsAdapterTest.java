package com.github.ayltai.newspaper.app.widget;

import android.widget.FrameLayout;

import com.github.ayltai.newspaper.UnitTest;
import com.github.ayltai.newspaper.widget.SwitchOptionsView;
import com.github.ayltai.newspaper.widget.TextOptionsView;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

public final class OptionsAdapterTest extends UnitTest {
    @Test
    public void test() {
        final OptionsAdapter adapter = new OptionsAdapter(RuntimeEnvironment.application);

        Assert.assertEquals(3, adapter.getCount());
        Assert.assertEquals("Sources", adapter.getPageTitle(0));
        Assert.assertEquals("Categories", adapter.getPageTitle(1));
        Assert.assertEquals("Settings", adapter.getPageTitle(2));
        Assert.assertTrue(adapter.instantiateItem(new FrameLayout(RuntimeEnvironment.application), 0) instanceof TextOptionsView);
        Assert.assertTrue(adapter.instantiateItem(new FrameLayout(RuntimeEnvironment.application), 1) instanceof TextOptionsView);
        Assert.assertTrue(adapter.instantiateItem(new FrameLayout(RuntimeEnvironment.application), 2) instanceof SwitchOptionsView);
    }
}
