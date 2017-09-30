package com.github.ayltai.newspaper.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import android.support.annotation.NonNull;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.simpleframework.xml.core.Persister;

import com.github.ayltai.newspaper.NetworkTest;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.SourceFactory;
import com.github.ayltai.newspaper.rss.RssFeed;
import com.github.ayltai.newspaper.util.IOUtils;

import io.reactivex.Observable;

public final class SkyPostClientTest extends NetworkTest {
    private static final String SKY_POST_URL         = "http://skypost.ulifestyle.com.hk/rss/sras001";
    private static final String SKY_POST_DETAILS_URL = "http://skypost.ulifestyle.com.hk/article/1899134/港人首置上車盤 或市價一半";

    private SkyPostClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn(Observable.just(SkyPostClientTest.createFeed())).when(this.apiService).getFeed(SkyPostClientTest.SKY_POST_URL);
        Mockito.doReturn(Observable.just(SkyPostClientTest.createDetailsHtml())).when(this.apiService).getHtml(SkyPostClientTest.SKY_POST_DETAILS_URL);

        this.client = new SkyPostClient(this.httpClient, this.apiService, SourceFactory.getInstance(RuntimeEnvironment.application).getSource("晴報"));
    }

    @Test
    public void Given_SkyPostUrl_When_getItemsIsCalled_Then_ItemsAreReturned() {
        final List<NewsItem> items = this.client.getItems(SkyPostClientTest.SKY_POST_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 49, items.size());
        Assert.assertEquals("Incorrect item title", "港人首置上車盤 或市價一半", items.get(0).getTitle());
        Assert.assertEquals("Incorrect item link", SkyPostClientTest.SKY_POST_DETAILS_URL, items.get(0).getLink());
        Assert.assertEquals("Incorrect item description", "特首林鄭月娥下月發表的施政報告，將公布港人首置上車盤計劃，土地供應專責小組主席黃遠輝透露，定價會按申請家庭的負擔能力釐定...", items.get(0).getDescription());
    }

    @Test
    public void Given_Item_When_updateItemIsCalled_Then_ItemIsUpdated() {
        final Item item = this.client.updateItem(this.client.getItems(SkyPostClientTest.SKY_POST_URL).blockingGet().get(0)).blockingGet();

        Assert.assertEquals("Incorrect item.getImages().size()", 2, item.getImages().size());
        Assert.assertEquals("Incorrect image URL", "https://resource01.ulifestyle.com.hk/res/v3/image/content/1895000/1899134/08JAA002__20170908_L.jpg", item.getImages().get(0).getUrl());
        Assert.assertEquals("Incorrect image description", "林鄭月娥曾在競選政綱指，港人首置上車盤是「居屋之上，私樓之下。」（iStock）", item.getImages().get(0).getDescription());
        Assert.assertNull("item.getVideo() is not null", item.getVideo());
        Assert.assertEquals("Incorrect item full description", "<h3>港人首置上車盤 或市價一半</h3><br><h4>轉售擬設限 只可賣予政府</h4><br>特首林鄭月娥下月發表的施政報告，將公布港人首置上車盤計劃，土地供應專責小組主席黃遠輝透露，定價會按", item.getDescription().substring(0, 100));
    }

    @NonNull
    private static RssFeed createFeed() throws Exception {
        return new Persister().read(RssFeed.class, new FileInputStream("src/debug/assets/skypost.xml"));
    }

    @NonNull
    private static String createDetailsHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/skypost_details.html"));
    }
}
