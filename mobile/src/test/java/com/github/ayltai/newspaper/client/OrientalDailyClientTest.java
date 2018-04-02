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

public final class OrientalDailyClientTest extends NetworkTest {
    private static final String ORIENTAL_DAILY_URL         = "http://orientaldaily.on.cc/rss/news.xml";
    private static final String ORIENTAL_DAILY_DETAILS_URL = "http://orientaldaily.on.cc/cnt/news/20170909/00174_001.html?pubdate=20170909";
    private static final String ERROR_URL                  = "error 1";
    private static final String ERROR_DETAILS_URL          = "error 2";

    private OrientalDailyClient client;

    @CallSuper
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn(Observable.just(createFeed())).when(this.apiService).getFeed(OrientalDailyClientTest.ORIENTAL_DAILY_URL);
        Mockito.doReturn(Observable.just(createHtml())).when(this.apiService).getHtml(OrientalDailyClientTest.ORIENTAL_DAILY_DETAILS_URL);
        Mockito.doReturn(Observable.just(createVideoHtml())).when(this.apiService).getHtml("http://orientaldaily.on.cc/cnt/keyinfo/20170909/videolist.xml");
        Mockito.doReturn(Observable.error(new RuntimeException("Fake error 1"))).when(this.apiService).getFeed(OrientalDailyClientTest.ERROR_URL);
        Mockito.doReturn(Observable.error(new RuntimeException("Fake error 2"))).when(this.apiService).getHtml(OrientalDailyClientTest.ERROR_DETAILS_URL);

        this.client = new OrientalDailyClient(this.httpClient, this.apiService, SourceFactory.getInstance(RuntimeEnvironment.application).getSource("東方日報"));
    }

    @Test
    public void Given_orientalDailyUrl_When_getItemsIsCalled_Then_itemsAreReturned() {
        final List<NewsItem> items = this.client.getItems(OrientalDailyClientTest.ORIENTAL_DAILY_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 20, items.size());
        Assert.assertEquals("Incorrect item title", "大學教育教出雙毒", items.get(0).getTitle());
        Assert.assertEquals("Incorrect item link", OrientalDailyClientTest.ORIENTAL_DAILY_DETAILS_URL, items.get(0).getLink());
        Assert.assertEquals("Incorrect item description", "專責培育下一代教師的香港教育大學，竟有人在教育局副局長蔡若蓮長子墮樓離世後，於校內民主牆張貼奚落蔡若蓮喪子的字句，令社會嘩然，涉事兩名年輕男子的外貌昨日曝光！本報獲得事發時的閉路電視畫面截圖，可見兩人張貼有關冷血字句後帶笑離開。多個團體昨日先後發表聲明及到教大請願，促校方徹查及嚴懲涉事者。有學者指近日本港多間大學校園湧現違法「港獨」標語，現再出現這種「歹毒」言行，「播港獨心腸又毒」，反映大學教育出了問題，部分學生已無仁義道德及禮教可言。", items.get(0).getDescription());
    }

    @Test
    public void Given_errorUrl_When_getItemsIsCalled_Then_noItemsAreReturned() {
        final List<NewsItem> items = this.client.getItems(OrientalDailyClientTest.ERROR_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 0, items.size());
    }

    @Test
    public void Given_item_When_updateItemIsCalled_Then_itemIsUpdated() {
        final Item item = this.client.updateItem(this.client.getItems(OrientalDailyClientTest.ORIENTAL_DAILY_URL).blockingGet().get(0)).blockingGet();

        Assert.assertEquals("Incorrect item.getImages().size()", 4, item.getImages().size());
        Assert.assertEquals("Incorrect image URL", "http://orientaldaily.on.cc/cnt/news/20170909/photo/0909-00176-010b2.jpg", item.getImages().get(1).getUrl());
        Assert.assertEquals("Incorrect image description", "周竪峰情緒激動，與疑似內地生對罵期間企圖衝前，似有所行動。", item.getImages().get(1).getDescription());
        Assert.assertNotNull("item.getVideo() is null", item.getVideo());
        Assert.assertEquals("Incorrect video URL", "http://video.cdn.on.cc/Video/201709/ONS170908-14006-77-M_ipad.mp4", item.getVideo().getVideoUrl());
        Assert.assertEquals("Incorrect item full description", "繼青年新政游蕙禎和梁頌恆宣誓引發辱華風波後，昨日再爆出中文大學學生辱華事件。中大日前出現「香港獨立」橫額及海報後，惹來反對港獨團體前日到中大文化廣場示威，並與支持港獨的學生爆發衝突。其後網上流傳數段影", item.getDescription().substring(0, 100));
    }

    @Test
    public void Given_orientalDailyDetailsErrorUrl_When_updateItemIsCalled_noItemIsUpdated() {
        final NewsItem newsItem = new NewsItem();
        newsItem.setLink(OrientalDailyClientTest.ERROR_DETAILS_URL);

        final NewsItem item = this.client.updateItem(newsItem).blockingGet();

        Assert.assertEquals("Item is updated", newsItem, item);
    }

    @NonNull
    private static RssFeed createFeed() throws Exception {
        return new Persister().read(RssFeed.class, new FileInputStream("src/debug/assets/oriental_daily.xml"));
    }

    @NonNull
    private static String createHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/oriental_daily_details.html"));
    }

    @NonNull
    private static String createVideoHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/oriental_daily_video.xml"));
    }
}
