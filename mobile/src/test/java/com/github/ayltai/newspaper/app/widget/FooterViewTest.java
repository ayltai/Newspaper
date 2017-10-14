package com.github.ayltai.newspaper.app.widget;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import com.github.ayltai.newspaper.UnitTest;

public final class FooterViewTest extends UnitTest {
    @Test
    public void testProperties() {
        final FooterView view = new FooterView(RuntimeEnvironment.application);

        view.setTitle("title");
        view.setDescription("description");

        Assert.assertEquals("title", view.getTitle().toString());
        Assert.assertEquals("description", view.getDescription().toString());
    }
}
