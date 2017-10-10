package com.github.ayltai.newspaper.app.widget;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import com.github.ayltai.newspaper.UnitTest;

public final class AboutViewTest extends UnitTest {
    @Test
    public void testProperties() {
        final AboutView view = new AboutView(RuntimeEnvironment.application);

        view.setAppName("name");
        view.setAppVersion("1.0");

        Assert.assertEquals("name", view.getAppName());
        Assert.assertEquals("Version 1.0", view.getAppVersion());
    }
}
