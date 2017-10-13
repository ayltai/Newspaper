package com.github.ayltai.newspaper.app.data.model;

import java.util.Date;

import android.os.Bundle;

import org.junit.Assert;
import org.junit.Test;

import com.github.ayltai.newspaper.UnitTest;

public final class NewsItemTest extends UnitTest {
    @Test
    public void testParcelable() {
        final Bundle   bundle1 = new Bundle();
        final NewsItem item1   = new NewsItem();

        bundle1.putParcelable("key", item1);

        Assert.assertEquals(item1, bundle1.getParcelable("key"));

        final Bundle   bundle2 = new Bundle();
        final NewsItem item2   = new NewsItem();

        item2.setTitle("title");
        item2.setDescription("description");
        item2.setCategory("category");
        item2.setLastAccessedDate(new Date());
        item2.setLink("link");
        item2.setSource("source");

        bundle2.putParcelable("key", item2);

        Assert.assertEquals(item2, bundle2.getParcelable("key"));
    }
}
