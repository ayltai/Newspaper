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
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.data.model.SourceFactory;
import com.github.ayltai.newspaper.rss.RssFeed;
import com.github.ayltai.newspaper.util.IOUtils;

import io.reactivex.Observable;

public final class OrientalDailyClientTest extends NetworkTest {
    private static final String ORIENTAL_DAILY_URL         = "http://orientaldaily.on.cc/rss/news.xml";
    private static final String ORIENTAL_DAILY_DETAILS_URL = "\n                http://orientaldaily.on.cc/cnt/news/20170909/00176_010.html?pubdate=20170909\n            ";

    private OrientalDailyClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn(Observable.just(createFeed())).when(this.apiService).getFeed(OrientalDailyClientTest.ORIENTAL_DAILY_URL);
        Mockito.doReturn(Observable.just(createHtml())).when(this.apiService).getHtml(OrientalDailyClientTest.ORIENTAL_DAILY_DETAILS_URL);
        Mockito.doReturn(Observable.just(createVideoHtml())).when(this.apiService).getHtml("http://orientaldaily.on.cc/cnt/keyinfo/20170909/videolist.xml");

        this.client = new OrientalDailyClient(this.httpClient, this.apiService, SourceFactory.getInstance(RuntimeEnvironment.application).getSource("東方日報"));
    }

    @Test
    public void Given_HeadlineUrl_When_getItemsIsCalled_Then_ItemsAreReturned() {
        final List<Item> items = this.client.getItems(OrientalDailyClientTest.ORIENTAL_DAILY_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 20, items.size());
        Assert.assertEquals("Incorrect item title", "\n                中大周竪峰辱華罵內地生「支那人」\n            ", items.get(0).getTitle());
        Assert.assertEquals("Incorrect item link", OrientalDailyClientTest.ORIENTAL_DAILY_DETAILS_URL, items.get(0).getLink());
        Assert.assertEquals("Incorrect item description", "\n" +
            "                \n" +
            "                <div style=\"float:left;padding-right:10px;\">\n" +
            "                        <div><a title=\"中大周竪峰辱華罵內地生「支那人」\" href=\"http://orientaldaily.on.cc/cnt/news/20170909/00176_010.html?pubdate=20170909\"><img style=\"border-color:#B3B3B3;border-width:0 1px 1px;border-style:none solid solid;\" height=\"200\" width=\"200\" title=\"中大周竪峰辱華罵內地生「支那人」\" alt=\"中大周竪峰辱華罵內地生「支那人」\" src=\"http://orientaldaily.on.cc/cnt/news/20170909/photo/0909-00176-010p1g1.jpg\"/></a></div>\n" +
            "                    </div>\n" +
            "                <div style=\"float:left;\">\n" +
            "                    繼青年新政游蕙禎和梁頌恆宣誓引發辱華風波後，昨日再爆出中文大學學生辱華事件。中大日前出現「香港獨立」橫額及海報後，惹來反對港獨團體前日到中大文化廣場示威，並與支持港獨的學生爆發衝突。其後網上流傳數段影片，看到中大學生會前會長周竪峰以粗言穢語辱罵內地學生，更以帶有侮辱性的「支那人」來稱呼對方。周竪峰昨日承認曾指罵他人，但未有為事件道歉。中大校方表示將會展開調查，嚴肅處理事件。\n" +
            "                </div>\n" +
            "            ", items.get(0).getDescription());
    }

    @Test
    public void Given_Item_When_updateItemIsCalled_Then_ItemIsUpdated() {
        final Item item = this.client.updateItem(this.client.getItems(OrientalDailyClientTest.ORIENTAL_DAILY_URL).blockingGet().get(0)).blockingGet();

        Assert.assertEquals("Incorrect item.getImages().size()", 4, item.getImages().size());
        Assert.assertEquals("Incorrect image URL", "http://orientaldaily.on.cc/cnt/news/20170909/photo/0909-00176-010b2.jpg", item.getImages().get(1).getUrl());
        Assert.assertEquals("Incorrect image description", "周竪峰情緒激動，與疑似內地生對罵期間企圖衝前，似有所行動。", item.getImages().get(1).getDescription());
        Assert.assertNotNull("item.getVideo() is null", item.getVideo());
        Assert.assertEquals("Incorrect video URL", "http://video.cdn.on.cc/Video/201709/OBK170908-14357-17-M_ipad.mp4", item.getVideo().getVideoUrl());
        Assert.assertEquals("Incorrect item full description", "繼青年新政游蕙禎和梁頌恆宣誓引發辱華風波後，昨日再爆出中文大學學生辱華事件。中大日前出現「香港獨立」橫額及海報後，惹來反對港獨團體前日到中大文化廣場示威，並與支持港獨的學生爆發衝突。其後網上流傳數段影", item.getDescription().substring(0, 100));
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
