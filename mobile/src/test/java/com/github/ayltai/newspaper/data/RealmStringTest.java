package com.github.ayltai.newspaper.data;

import org.junit.Assert;
import org.junit.Test;

import com.github.ayltai.newspaper.UnitTest;

public final class RealmStringTest extends UnitTest {
    @Test
    public void test() {
        Assert.assertEquals("value", new RealmString("value").getValue());
    }
}
