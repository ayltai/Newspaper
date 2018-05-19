package com.github.ayltai.newspaper.client;

import com.github.ayltai.newspaper.AppUnitTest;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

public final class ClientFactoryTest extends AppUnitTest {
    @Test
    public void test() {
        final ClientFactory factory = ClientFactory.getInstance(RuntimeEnvironment.application);

        Assert.assertTrue(factory.getClient("蘋果日報") instanceof AppleDailyClient);
        Assert.assertTrue(factory.getClient("東方日報") instanceof OrientalDailyClient);
        Assert.assertTrue(factory.getClient("星島日報") instanceof SingTaoClient);
        Assert.assertTrue(factory.getClient("星島即時") instanceof SingTaoRealtimeClient);
        Assert.assertTrue(factory.getClient("經濟日報") instanceof HketClient);
        Assert.assertTrue(factory.getClient("成報") instanceof SingPaoClient);
        Assert.assertTrue(factory.getClient("明報") instanceof MingPaoClient);
        Assert.assertTrue(factory.getClient("頭條日報") instanceof HeadlineClient);
        Assert.assertTrue(factory.getClient("頭條即時") instanceof HeadlineRealtimeClient);
        Assert.assertTrue(factory.getClient("晴報") instanceof SkyPostClient);
        Assert.assertTrue(factory.getClient("信報") instanceof HkejClient);
        Assert.assertTrue(factory.getClient("香港電台") instanceof RthkClient);
    }
}
