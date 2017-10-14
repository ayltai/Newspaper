package com.github.ayltai.newspaper.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.ayltai.newspaper.UnitTest;

public final class ListsTest extends UnitTest {
    @Test
    public void testTransform() {
        final List<String> items = new ArrayList<>();
        items.add("test");

        Assert.assertEquals(true, Lists.transform(items, item -> true).get(0));
    }
}
