package com.github.ayltai.newspaper.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.simpleframework.xml.core.Persister;

import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.SourceFactory;
import com.github.ayltai.newspaper.net.NetworkTest;
import com.github.ayltai.newspaper.rss.RssFeed;
import com.github.ayltai.newspaper.util.IOUtils;

import io.reactivex.Observable;

public final class HketClientTest extends NetworkTest {
    private static final String HKET_URL          = "http://www.hket.com/rss/hongkong";
    private static final String HKET_DETAILS_URL  = "https://topick.hket.com/article/1931411";
    private static final String ERROR_URL         = "error 1";
    private static final String ERROR_DETAILS_URL = "error 2";

    private HketClient client;

    @CallSuper
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn(Observable.just(HketClientTest.createFeed())).when(this.apiService).getFeed(HketClientTest.HKET_URL);
        Mockito.doReturn(Observable.just(HketClientTest.createDetailsHtml())).when(this.apiService).getHtml(HketClientTest.HKET_DETAILS_URL);
        Mockito.doReturn(Observable.error(new RuntimeException("Fake error 1"))).when(this.apiService).getFeed(HketClientTest.ERROR_URL);
        Mockito.doReturn(Observable.error(new RuntimeException("Fake error 2"))).when(this.apiService).getHtml(HketClientTest.ERROR_DETAILS_URL);

        this.client = new HketClient(this.httpClient, this.apiService, SourceFactory.getInstance(RuntimeEnvironment.application).getSource("經濟日報"));
    }

    @Test
    public void Given_hketUrl_When_getItemsIsCalled_Then_itemsAreReturned() {
        final List<NewsItem> items = this.client.getItems(HketClientTest.HKET_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 32, items.size());
        Assert.assertEquals("Incorrect item title", "港大民研：公民黨位列十大政團榜首", items.get(0).getTitle());
        Assert.assertEquals("Incorrect item link", HketClientTest.HKET_DETAILS_URL, items.get(12).getLink());
        Assert.assertEquals("Incorrect item description", "港大民意研究計劃公布最新十大政團調查結果，公民黨以47分位列十大政團第一位，排第二的則是得到46分的工聯會，自由黨排第三...", items.get(0).getDescription());
    }

    @Test
    public void Given_errorUrl_When_getItemsIsCalled_Then_noItemsAreReturned() {
        final List<NewsItem> items = this.client.getItems(HketClientTest.ERROR_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 0, items.size());
    }

    @Test
    public void Given_item_When_updateItemIsCalled_Then_itemIsUpdated() {
        final Item item = this.client.updateItem(this.client.getItems(HketClientTest.HKET_URL).blockingGet().get(12)).blockingGet();

        Assert.assertEquals("Incorrect item.getImages().size()", 4, item.getImages().size());
        Assert.assertEquals("Incorrect image URL", "https://topick.hket.com/res/v3/image/content/1930000/1931411/133017690_1024.jpg", item.getImages().get(0).getUrl());
        Assert.assertEquals("Incorrect image description", "中大與博愛醫院研究發現，透過腹部取穴的「腹針治療」，有效改善頸痛患者的痛楚及活動功能。（陳曉瑩攝）", item.getImages().get(0).getDescription());
        Assert.assertNotNull("item.getVideo() is null", item.getVideo());
        Assert.assertEquals("Incorrect video URL", "https://www.youtube.com/watch?v=j8YATMY0IfY", item.getVideo().getVideoUrl());
        Assert.assertEquals("Incorrect item full description", "頸痛是常見健康問題，主因工作姿勢不良血液循環欠佳或缺乏運動等，導致肌肉長期繃緊及疼痛。中大與博愛醫院一項中醫針灸研究顯示，透過腹部取穴的「腹針治療」，可有效改善頸痛患者的痛楚及活動功能，有患者接受首次", item.getDescription().substring(0, 100));
    }

    @Test
    public void Given_hketDetailsErrorUrl_When_updateItemIsCalled_noItemIsUpdated() {
        final NewsItem newsItem = new NewsItem();
        newsItem.setLink(HketClientTest.ERROR_DETAILS_URL);

        final NewsItem item = this.client.updateItem(newsItem).blockingGet();

        Assert.assertEquals("Item is updated", newsItem, item);
    }

    @NonNull
    private static RssFeed createFeed() throws Exception {
        return new Persister().read(RssFeed.class, new FileInputStream("src/debug/assets/hket.xml"));
    }

    @NonNull
    private static String createDetailsHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/hket_details.html"));
    }
}
