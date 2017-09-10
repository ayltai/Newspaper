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

public final class RthkClientTest extends NetworkTest {
    private static final String RTHK_URL         = "http://rthk.hk/rthk/news/rss/c_expressnews_clocal.xml";
    private static final String RTHK_DETAILS_URL = "http://news.rthk.hk/rthk/ch/component/k2/1353287-20170910.htm";

    private RthkClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn(Observable.just(RthkClientTest.createFeed())).when(this.apiService).getFeed(RthkClientTest.RTHK_URL);
        Mockito.doReturn(Observable.just(RthkClientTest.createDetailsHtml())).when(this.apiService).getHtml(RthkClientTest.RTHK_DETAILS_URL);

        this.client = new RthkClient(this.httpClient, this.apiService, SourceFactory.getInstance(RuntimeEnvironment.application).getSource("香港電台"));
    }

    @Test
    public void Given_RthkUrl_When_getItemsIsCalled_Then_ItemsAreReturned() {
        final List<Item> items = this.client.getItems(RthkClientTest.RTHK_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 20, items.size());
        Assert.assertEquals("Incorrect item title", "議員薪津歸還限期將屆滿　游蕙禎稍後回覆立法會秘書處", items.get(0).getTitle());
        Assert.assertEquals("Incorrect item link", "http://news.rthk.hk/rthk/ch/component/k2/1353290-20170910.htm", items.get(0).getLink());
        Assert.assertEquals("Incorrect item description", "青年新政梁頌恆與游蕙禎就宣誓案的終極上訴被拒後，立法會秘書處向兩人發信，各追討約93萬元議員薪津，限期後日屆滿，游蕙禎指稍後會回覆信件。\n" +
            "\n" +
            "她指回信是希望向立法會秘書處了解法律理據與詳情，之後才知道下一步如何做。\n" +
            "\n" +
            "她又說，當日遷出辦公室時，曾想歸還以薪金購買的器材，但秘書處拒絕接收。", items.get(0).getDescription());
    }

    @Test
    public void Given_Item_When_updateItemIsCalled_Then_ItemIsUpdated() {
        final Item item = this.client.updateItem(this.client.getItems(RthkClientTest.RTHK_URL).blockingGet().get(2)).blockingGet();

        Assert.assertEquals("Incorrect item.getImages().size()", 1, item.getImages().size());
        Assert.assertEquals("Incorrect image URL", "http://newsstatic.rthk.hk/images/mfile_1353287_1_20170910151424.jpg", item.getImages().get(0).getUrl());
        Assert.assertEquals("Incorrect image description", "蘇敬恆說，是否出任政治助理，仍未有最新消息公布。（黃億文攝）", item.getImages().get(0).getDescription());
        Assert.assertNotNull("item.getVideo() is null", item.getVideo());
        Assert.assertEquals("Incorrect video URL", "http://newsstatic.rthk.hk/videos/vfile_1353287_1_20170910151631.mp4", item.getVideo().getVideoUrl());
        Assert.assertEquals("Incorrect item full description", "本台節目《城市論壇》暑假暫停後，今日復播。早前被傳或會出任食物及衞生局局長政治助理的主持蘇敬恆，今日繼續主持節目。<br />\r\n" +
            "<br />\r\n" +
            "被問到會否出任政治助理一職，蘇敬恆說，現時未有最新消息", item.getDescription().substring(0, 100));
    }

    @NonNull
    private static RssFeed createFeed() throws Exception {
        return new Persister().read(RssFeed.class, new FileInputStream("src/debug/assets/rthk.xml"));
    }

    @NonNull
    private static String createDetailsHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/rthk_details.html"));
    }
}
