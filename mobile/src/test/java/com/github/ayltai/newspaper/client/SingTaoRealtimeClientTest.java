package com.github.ayltai.newspaper.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import android.support.annotation.NonNull;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import com.github.ayltai.newspaper.NetworkTest;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.data.model.SourceFactory;
import com.github.ayltai.newspaper.util.IOUtils;

import io.reactivex.Observable;

public final class SingTaoRealtimeClientTest extends NetworkTest {
    private static final String SING_TAO_REALTIME_URL         = "http://std.stheadline.com/instant/articles/listview/%E9%A6%99%E6%B8%AF/";
    private static final String SING_TAO_REALTIME_DETAILS_URL = "http://std.stheadline.com/instant/../instant/articles/detail/509040-%E9%A6%99%E6%B8%AF-%E8%91%B5%E6%B6%8C%E9%82%A8%E9%A9%9A%E7%8F%BE%E7%96%91%E4%BC%BC%E8%83%8E%E7%9B%A4+%E8%AD%A6%E5%8A%A0%E5%A4%A7%E6%90%9C%E7%B4%A2%E7%AF%84%E5%9C%8D";

    private SingTaoRealtimeClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn(Observable.just(SingTaoRealtimeClientTest.createHtml())).when(this.apiService).getHtml(SingTaoRealtimeClientTest.SING_TAO_REALTIME_URL);
        Mockito.doReturn(Observable.just(SingTaoRealtimeClientTest.createDetailsHtml())).when(this.apiService).getHtml(SingTaoRealtimeClientTest.SING_TAO_REALTIME_DETAILS_URL);

        this.client = new SingTaoRealtimeClient(this.httpClient, this.apiService, SourceFactory.getInstance(RuntimeEnvironment.application).getSource("星島即時"));
    }

    @Test
    public void Given_SingTaoRealtimeUrl_When_getItemsIsCalled_Then_ItemsAreReturned() {
        final List<Item> items = this.client.getItems(SingTaoRealtimeClientTest.SING_TAO_REALTIME_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 33, items.size());
        Assert.assertEquals("Incorrect item title", "葵涌邨驚現疑似胎盤 警加大搜索範圍", items.get(0).getTitle());
        Assert.assertEquals("Incorrect item link", SingTaoRealtimeClientTest.SING_TAO_REALTIME_DETAILS_URL, items.get(0).getLink());
        Assert.assertNull("Incorrect item description", items.get(0).getDescription());
    }

    @Test
    public void Given_Item_When_updateItemIsCalled_Then_ItemIsUpdated() {
        final Item item = this.client.updateItem(this.client.getItems(SingTaoRealtimeClientTest.SING_TAO_REALTIME_URL).blockingGet().get(0)).blockingGet();

        Assert.assertEquals("Incorrect item.getImages().size()", 16, item.getImages().size());
        Assert.assertEquals("Incorrect image URL", "http://static.stheadline.com/stheadline/inewsmedia/20170910/_2017091015413017852.jpg", item.getImages().get(0).getUrl());
        Assert.assertEquals("Incorrect image description", "現場留下血跡。楊偉亨攝", item.getImages().get(0).getDescription());
        Assert.assertNull("item.getVideo() is not null", item.getVideo());
        Assert.assertEquals("Incorrect item full description", "葵涌邨驚現疑似胎盤！一名保安於逸葵樓近變壓房位置發現一個懷疑胎盤物體，警方接報到場登樓調查，並派出大批動部隊人員到附近一帶搜索，惟至今未有發現。<br />\n" +
            "<br />\n" +
            "事發在早上8時許，保安途經逸", item.getDescription().substring(0, 100));
    }

    @NonNull
    private static String createHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/singtao_realtime.html"));
    }

    @NonNull
    private static String createDetailsHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/singtao_realtime_details.html"));
    }
}
