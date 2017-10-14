package com.github.ayltai.newspaper.util;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import com.github.ayltai.newspaper.UnitTest;

public final class DateUtilsTest extends UnitTest {
    @Test
    public void testToApproximateTime() {
        Assert.assertNull(DateUtils.toApproximateTime(RuntimeEnvironment.application, 0));
        Assert.assertNull(DateUtils.toApproximateTime(RuntimeEnvironment.application, System.currentTimeMillis() + 30 * 1000));
        Assert.assertEquals("Just now", DateUtils.toApproximateTime(RuntimeEnvironment.application, System.currentTimeMillis() - 30 * 1000));
        Assert.assertEquals("A minute ago", DateUtils.toApproximateTime(RuntimeEnvironment.application, System.currentTimeMillis() - 90 * 1000));
        Assert.assertEquals("30 minutes ago", DateUtils.toApproximateTime(RuntimeEnvironment.application, System.currentTimeMillis() - 30 * 60 * 1000));
        Assert.assertEquals("An hour ago", DateUtils.toApproximateTime(RuntimeEnvironment.application, System.currentTimeMillis() - 75 * 60 * 1000));
        Assert.assertEquals("5 hours ago", DateUtils.toApproximateTime(RuntimeEnvironment.application, System.currentTimeMillis() - 5 * 60 * 60 * 1000));
        Assert.assertEquals("Yesterday", DateUtils.toApproximateTime(RuntimeEnvironment.application, System.currentTimeMillis() - 36 * 60 * 60 * 1000));
        Assert.assertEquals("5 days ago", DateUtils.toApproximateTime(RuntimeEnvironment.application, System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000));
    }
}
