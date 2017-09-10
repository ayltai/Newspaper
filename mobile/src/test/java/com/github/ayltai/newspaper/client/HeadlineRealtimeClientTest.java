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

public final class HeadlineRealtimeClientTest extends NetworkTest {
    private static final String HEADLINE_REALTIME_URL         = "http://hd.stheadline.com/news/realtime/hk/";
    private static final String HEADLINE_REALTIME_DETAILS_URL = "http://hd.stheadline.com/news/realtime/hk/1008479/";

    private HeadlineRealtimeClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn(Observable.just(HeadlineRealtimeClientTest.createHtml())).when(this.apiService).getHtml(HeadlineRealtimeClientTest.HEADLINE_REALTIME_URL);
        Mockito.doReturn(Observable.just(HeadlineRealtimeClientTest.createDetailsHtml())).when(this.apiService).getHtml(HeadlineRealtimeClientTest.HEADLINE_REALTIME_DETAILS_URL);

        this.client = new HeadlineRealtimeClient(this.httpClient, this.apiService, SourceFactory.getInstance(RuntimeEnvironment.application).getSource("頭條即時"));
    }

    @Test
    public void Given_HeadlineRealtimeUrl_When_getItemsIsCalled_Then_ItemsAreReturned() {
        final List<Item> items = this.client.getItems(HeadlineRealtimeClientTest.HEADLINE_REALTIME_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 10, items.size());
        Assert.assertEquals("Incorrect item title", "將軍澳冷氣機房爆炸 19歲工人6成皮膚燒傷", items.get(0).getTitle());
        Assert.assertEquals("Incorrect item link", HeadlineRealtimeClientTest.HEADLINE_REALTIME_DETAILS_URL, items.get(0).getLink());
        Assert.assertEquals("Incorrect item description", "將軍澳新都城第一期停車場冷氣機房發生爆炸，2名工人受傷，其中1人身體6成皮膚被燒傷，送院治理。消息指，有人疑在冷氣機房內點煙肇禍，消防正調查爆炸原因。事發在中午12時許，2名工人在冷氣機房清洗冷氣機，期間房內突然發生爆炸，2人走避不及，慘遭燒傷。消防接報趕至，迅速將火救熄。其中一名19歲工人6成皮膚被燒傷，由救護車送往...", items.get(0).getDescription());
    }

    @Test
    public void Given_Item_When_updateItemIsCalled_Then_ItemIsUpdated() {
        final Item item = this.client.updateItem(this.client.getItems(HeadlineRealtimeClientTest.HEADLINE_REALTIME_URL).blockingGet().get(0)).blockingGet();

        Assert.assertEquals("Incorrect item.getImages().size()", 3, item.getImages().size());
        Assert.assertEquals("Incorrect image URL", "http://static.stheadline.com/stheadline/inewsmedia/20170909/_2017090915215953935.jpg", item.getImages().get(0).getUrl());
        Assert.assertEquals("Incorrect image description", "警方在場調查。林思明攝", item.getImages().get(0).getDescription());
        Assert.assertEquals("Incorrect item full description", "<p>將軍澳新都城第一期停車場冷氣機房發生爆炸，2名男工人受", item.getDescription().substring(0, 30));
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
