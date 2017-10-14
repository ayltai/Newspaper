package com.github.ayltai.newspaper.util;

import org.junit.Assert;
import org.junit.Test;

import com.github.ayltai.newspaper.UnitTest;
import com.github.ayltai.newspaper.app.data.model.NewsItem;

import io.realm.RealmList;

public final class RealmListsTest extends UnitTest {
    @Test
    public void testToString() {
        final RealmList<NewsItem> items = new RealmList<>();
        items.add(new NewsItem());

        Assert.assertEquals(new NewsItem().toString(), RealmLists.toString(items));
    }
}
