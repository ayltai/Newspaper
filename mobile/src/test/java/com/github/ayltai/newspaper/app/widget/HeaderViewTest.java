package com.github.ayltai.newspaper.app.widget;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import com.github.ayltai.newspaper.UnitTest;
import com.github.ayltai.newspaper.util.DateUtils;

public final class HeaderViewTest extends UnitTest {
    @Test
    public void testProperties() {
        final HeaderView view = new HeaderView(RuntimeEnvironment.application);
        final Date       date = new Date();

        view.setSource("source");
        view.setPublishDate(date);

        Assert.assertEquals("source", view.getSource());
        Assert.assertEquals(DateUtils.toApproximateTime(RuntimeEnvironment.application, date.getTime()), view.getPublishDate());
    }
}
