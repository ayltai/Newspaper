package com.github.ayltai.newspaper.util;

import com.github.ayltai.newspaper.AppUnitTest;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public final class ListsTest extends AppUnitTest {
    @Test
    public void testTransform() {
        final List<String> items = new ArrayList<>();
        items.add("test");

        Assert.assertEquals(true, Lists.transform(items, item -> true).get(0));
    }
}
