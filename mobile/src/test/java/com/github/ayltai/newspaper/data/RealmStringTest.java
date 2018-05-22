package com.github.ayltai.newspaper.data;

import com.github.ayltai.newspaper.UnitTest;

import org.junit.Assert;
import org.junit.Test;

public final class RealmStringTest extends UnitTest {
    @Test
    public void test() {
        Assert.assertEquals("value", new RealmString("value").getValue());
    }
}
