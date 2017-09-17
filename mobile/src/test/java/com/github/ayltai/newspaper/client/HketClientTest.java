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
import com.github.ayltai.newspaper.data.model.NewsItem;
import com.github.ayltai.newspaper.data.model.SourceFactory;
import com.github.ayltai.newspaper.util.IOUtils;

import io.reactivex.Observable;

public final class HketClientTest extends NetworkTest {
    private static final String HKET_URL         = "https://topick.hket.com/srat006/%E6%96%B0%E8%81%9E";
    private static final String HKET_DETAILS_URL = "https://topick.hket.com/article/1899848/全球獨有1.6億元的陀飛輪　手工鑲嵌260卡寶石?mtc=10012";

    private HketClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn(Observable.just(HketClientTest.createHtml())).when(this.apiService).getHtml(HketClientTest.HKET_URL);
        Mockito.doReturn(Observable.just(HketClientTest.createDetailsHtml())).when(this.apiService).getHtml(HketClientTest.HKET_DETAILS_URL);

        this.client = new HketClient(this.httpClient, this.apiService, SourceFactory.getInstance(RuntimeEnvironment.application).getSource("經濟日報"));
    }

    @Test
    public void Given_HketUrl_When_getItemsIsCalled_Then_ItemsAreReturned() {
        final List<NewsItem> items = this.client.getItems(HketClientTest.HKET_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 30, items.size());
        Assert.assertEquals("Incorrect item title", "保良局蔡繼有小一簡介會    總校長：成績表不設名次與分數", items.get(0).getTitle());
        Assert.assertEquals("Incorrect item link", "https://topick.hket.com/article/1900393/保良局蔡繼有小一簡介會    總校長：成績表不設名次與分數?mtc=10012", items.get(0).getLink());
        Assert.assertNull("Incorrect item description", items.get(0).getDescription());
    }

    @Test
    public void Given_Item_When_updateItemIsCalled_Then_ItemIsUpdated() {
        final Item item = this.client.updateItem(this.client.getItems(HketClientTest.HKET_URL).blockingGet().get(18)).blockingGet();

        Assert.assertEquals("Incorrect item.getImages().size()", 3, item.getImages().size());
        Assert.assertEquals("Incorrect image URL", "https://topick.hket.com/res/v3/image/content/1895000/1899848/carn0907026_1024.jpg", item.getImages().get(1).getUrl());
        Assert.assertEquals("Incorrect image description", "傑克寶的Astronomia Solar陀飛輪腕錶，以太陽系為設計理念。(車耀開攝)", item.getImages().get(1).getDescription());
        Assert.assertNotNull("item.getVideo() is null", item.getVideo());
        Assert.assertEquals("Incorrect video URL", "https://www.youtube.com/watch?v=UCcCvub76WA", item.getVideo().getVideoUrl());
        Assert.assertEquals("Incorrect item full description", "手錶向來是個人身份象徵。但一隻高達1.6億元的陀飛輪腕錶，到底有幾珍貴？<br>這隻名為Billionaire陀飛輪腕錶，是由美國品牌傑克寶Jacob &amp; Co.所製造，售價為1.6億元，全球", item.getDescription().substring(0, 100));
    }

    @NonNull
    private static String createHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/hket.html"));
    }

    @NonNull
    private static String createDetailsHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/hket_details.html"));
    }
}
