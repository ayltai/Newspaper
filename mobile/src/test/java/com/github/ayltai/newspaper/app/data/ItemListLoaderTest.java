package com.github.ayltai.newspaper.app.data;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.Robolectric;

import com.github.ayltai.newspaper.app.MainActivity;
import com.github.ayltai.newspaper.data.DataTest;

public final class ItemListLoaderTest extends DataTest {
    @Test
    public void testCreate() {
        Assert.assertEquals(0, new ItemListLoader.Builder(Robolectric.buildActivity(MainActivity.class).get())
            .addCategory("港聞")
            .addSource("蘋果日報")
            .build()
            .blockingSingle()
            .size());
    }
}
