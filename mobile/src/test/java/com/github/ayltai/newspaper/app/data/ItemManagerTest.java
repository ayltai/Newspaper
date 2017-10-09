package com.github.ayltai.newspaper.app.data;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.data.DaggerDataComponent;
import com.github.ayltai.newspaper.data.DataModule;
import com.github.ayltai.newspaper.data.DataTest;
import com.github.ayltai.newspaper.util.Irrelevant;

import io.realm.Realm;

public final class ItemManagerTest extends DataTest {
    @Test
    public void testCreate() {
        final Realm realm = DaggerDataComponent.builder()
            .dataModule(new DataModule(RuntimeEnvironment.application))
            .build()
            .realm();

        Assert.assertEquals(realm, ItemManager.create(realm).getRealm());
        Assert.assertEquals(realm, ItemManager.create(RuntimeEnvironment.application).blockingGet().getRealm());
    }

    @Test
    public void testGetItems() {
        final ItemManager manager = ItemManager.create(DaggerDataComponent.builder()
            .dataModule(new DataModule(RuntimeEnvironment.application))
            .build()
            .realm());

        final List<NewsItem> items = manager.getItems(new String[] { "source" }, new String[] { "category" }).blockingGet();
        Assert.assertEquals(0, items.size());

        final List<NewsItem> queryItems = manager.getItems("query", new String[] { "source" }, new String[] { "category" }).blockingGet();
        Assert.assertEquals(0, queryItems.size());
    }

    @Test
    public void testGetHistoricalItems() {
        final ItemManager manager = ItemManager.create(DaggerDataComponent.builder()
            .dataModule(new DataModule(RuntimeEnvironment.application))
            .build()
            .realm());

        final List<NewsItem> items = manager.getHistoricalItems(new String[] { "source" }, new String[] { "category" }).blockingGet();
        Assert.assertEquals(0, items.size());

        final List<NewsItem> queryItems = manager.getHistoricalItems("query", new String[] { "source" }, new String[] { "category" }).blockingGet();
        Assert.assertEquals(0, queryItems.size());
    }

    @Test
    public void testGetBookmarkedItems() {
        final ItemManager manager = ItemManager.create(DaggerDataComponent.builder()
            .dataModule(new DataModule(RuntimeEnvironment.application))
            .build()
            .realm());

        final List<NewsItem> items = manager.getBookmarkedItems(new String[] { "source" }, new String[] { "category" }).blockingGet();
        Assert.assertEquals(0, items.size());

        final List<NewsItem> queryItems = manager.getBookmarkedItems("query", new String[] { "source" }, new String[] { "category" }).blockingGet();
        Assert.assertEquals(0, queryItems.size());
    }

    @Test
    public void testPutItems() {
        final ItemManager manager = ItemManager.create(DaggerDataComponent.builder()
            .dataModule(new DataModule(RuntimeEnvironment.application))
            .build()
            .realm());

        final List<NewsItem> items = new ArrayList<>();
        final NewsItem item = new NewsItem();
        item.setLink("link");
        items.add(item);

        Assert.assertEquals(1, manager.putItems(items).blockingGet().size());
    }

    @Test
    public void testClearHistories() {
        final ItemManager manager = ItemManager.create(DaggerDataComponent.builder()
            .dataModule(new DataModule(RuntimeEnvironment.application))
            .build()
            .realm());

        Assert.assertEquals(Irrelevant.INSTANCE, manager.clearHistories().blockingGet());
    }

    @Test
    public void testClearBookmarks() {
        final ItemManager manager = ItemManager.create(DaggerDataComponent.builder()
            .dataModule(new DataModule(RuntimeEnvironment.application))
            .build()
            .realm());

        Assert.assertEquals(Irrelevant.INSTANCE, manager.clearBookmarks().blockingGet());
    }
}
