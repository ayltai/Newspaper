package com.github.ayltai.newspaper.client;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.SourceFactory;
import com.github.ayltai.newspaper.net.NetworkTest;
import com.github.ayltai.newspaper.util.IOUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;

public final class HeadlineRealtimeClientTest extends NetworkTest {
    private static final String HEADLINE_REALTIME_URL         = "http://hd.stheadline.com/news/realtime/hk/";
    private static final String HEADLINE_REALTIME_DETAILS_URL = "http://hd.stheadline.com/news/realtime/hk/1202643/";
    private static final String ERROR_URL                     = "error 1";
    private static final String ERROR_DETAILS_URL             = "error 2";

    private HeadlineRealtimeClient client;

    @CallSuper
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn(Observable.just(HeadlineRealtimeClientTest.createHtml())).when(this.apiService).getHtml(HeadlineRealtimeClientTest.HEADLINE_REALTIME_URL);
        Mockito.doReturn(Observable.just(HeadlineRealtimeClientTest.createDetailsHtml())).when(this.apiService).getHtml(HeadlineRealtimeClientTest.HEADLINE_REALTIME_DETAILS_URL);
        Mockito.doReturn(Observable.error(new RuntimeException("Fake error 1"))).when(this.apiService).getHtml(HeadlineRealtimeClientTest.ERROR_URL);
        Mockito.doReturn(Observable.error(new RuntimeException("Fake error 2"))).when(this.apiService).getHtml(HeadlineRealtimeClientTest.ERROR_DETAILS_URL);

        this.client = new HeadlineRealtimeClient(this.httpClient, this.apiService, SourceFactory.getInstance(RuntimeEnvironment.application).getSource("頭條即時"));
    }

    @Test
    public void Given_headlineRealtimeUrl_When_getItemsIsCalled_Then_itemsAreReturned() {
        final List<NewsItem> items = this.client.getItems(HeadlineRealtimeClientTest.HEADLINE_REALTIME_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 10, items.size());
        Assert.assertEquals("Incorrect item title", "【多圖有片】大埔爆水管噴出10層樓「噴泉」夜鷺BB遭水柱擊中墮地傷", items.get(0).getTitle());
        Assert.assertEquals("Incorrect item link", HeadlineRealtimeClientTest.HEADLINE_REALTIME_DETAILS_URL, items.get(0).getLink());
        Assert.assertEquals("Incorrect item description", "大埔廣福道爆水管！大量黃泥水由地底高壓噴出，足足有10層樓高。由於現場靠近鷺鳥林位置旁，有現場人士拍攝到照片顯示，有雀鳥懷疑受驚飛走。現場亦靠近民居及商舖，附近亦有老人院及投注站，距離不足十多米，大牆被噴濕，居民報警求助未知有否造成損失。事發於下午3時左右，在廣福道進入大埔墟方向，懷疑有地下水管爆裂，路面的路牌及指示牌...", items.get(0).getDescription());
    }

    @Test
    public void Given_errorUrl_When_getItemsIsCalled_Then_noItemsAreReturned() {
        final List<NewsItem> items = this.client.getItems(HeadlineRealtimeClientTest.ERROR_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 0, items.size());
    }

    @Test
    public void Given_item_When_updateItemIsCalled_Then_itemIsUpdated() {
        final Item item = this.client.updateItem(this.client.getItems(HeadlineRealtimeClientTest.HEADLINE_REALTIME_URL).blockingGet().get(0)).blockingGet();

        Assert.assertEquals("Incorrect item.getImages().size()", 3, item.getImages().size());
        Assert.assertEquals("Incorrect image URL", "http://static.stheadline.com/stheadline/inewsmedia/20170909/_2017090915215953935.jpg", item.getImages().get(0).getUrl());
        Assert.assertEquals("Incorrect image description", "警方在場調查。林思明攝", item.getImages().get(0).getDescription());
        Assert.assertEquals("Incorrect item full description", "<p>將軍澳新都城第一期停車場冷氣機房發生爆炸，2名男工人受", item.getDescription().substring(0, 30));
    }

    @Test
    public void Given_headlineRealtimeDetailsErrorUrl_When_updateItemIsCalled_noItemIsUpdated() {
        final NewsItem newsItem = new NewsItem();
        newsItem.setLink(HeadlineRealtimeClientTest.ERROR_DETAILS_URL);

        final NewsItem item = this.client.updateItem(newsItem).blockingGet();

        Assert.assertEquals("Item is updated", newsItem, item);
    }

    @NonNull
    private static String createHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/headline_realtime.html"));
    }

    @NonNull
    private static String createDetailsHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/headline_realtime_details.html"));
    }
}
