package com.github.ayltai.newspaper.app.widget;

import com.github.ayltai.newspaper.AppUnitTest;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

public final class AboutViewTest extends AppUnitTest {
    @Test
    public void testProperties() {
        final AboutView view = new AboutView(RuntimeEnvironment.application);

        view.setAppName("name");
        view.setAppVersion("1.0");

        Assert.assertEquals("name", view.getAppName());
        Assert.assertEquals("Version 1.0", view.getAppVersion());
    }
}
