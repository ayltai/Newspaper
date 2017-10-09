package com.github.ayltai.newspaper.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import android.support.annotation.NonNull;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import com.github.ayltai.newspaper.net.NetworkTest;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.SourceFactory;
import com.github.ayltai.newspaper.util.IOUtils;

import io.reactivex.Observable;

public final class SingPaoClientTest extends NetworkTest {
    private static final String SING_PAO_URL         = "https://www.singpao.com.hk/index.php?fi=news1";
    private static final String SING_PAO_DETAILS_URL = "https://www.singpao.com.hk/index.php?fi=news1&id=45330";

    private SingPaoClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn(Observable.just(SingPaoClientTest.createHtml())).when(this.apiService).getHtml(SingPaoClientTest.SING_PAO_URL);
        Mockito.doReturn(Observable.just(SingPaoClientTest.createDetailsHtml())).when(this.apiService).getHtml(SingPaoClientTest.SING_PAO_DETAILS_URL);

        this.client = new SingPaoClient(this.httpClient, this.apiService, SourceFactory.getInstance(RuntimeEnvironment.application).getSource("成報"));
    }

    @Test
    public void Given_SingPaoUrl_When_getItemsIsCalled_Then_ItemsAreReturned() {
        final List<NewsItem> items = this.client.getItems(SingPaoClientTest.SING_PAO_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 20, items.size());
        Assert.assertEquals("Incorrect item title", "郵票 車票 場刊 小冊子 紙品收藏見證香港文化", items.get(0).getTitle());
        Assert.assertEquals("Incorrect item link", SingPaoClientTest.SING_PAO_DETAILS_URL, items.get(0).getLink());
        Assert.assertEquals("Incorrect item description", "【我城‧我故事】\n" +
            "紙上不只是談兵，還可以細看香港歷史的變遷。吳貴龍上世紀80年代修讀印刷，之後順理成章到印刷廠工作，自此與紙品結下不解緣。不過，令人意想不到的是，他也慢慢喜愛上收藏紙品，從郵票到電車車票，再到電影印刷品、場刊等，都是他的收藏品。吳貴龍說，收藏電影宣傳小冊子等紙品，其實是「一種香港文化的見證」。\n" +
            "記者鄧厚仁報道\n" +
            "吳貴龍..", items.get(0).getDescription());
    }

    @Test
    public void Given_Item_When_updateItemIsCalled_Then_ItemIsUpdated() {
        final Item item = this.client.updateItem(this.client.getItems(SingPaoClientTest.SING_PAO_URL).blockingGet().get(0)).blockingGet();

        Assert.assertEquals("Incorrect item.getImages().size()", 5, item.getImages().size());
        Assert.assertEquals("Incorrect image URL", "https://www.singpao.com.hk/image_upload/1504985874.jpg", item.getImages().get(0).getUrl());
        Assert.assertEquals("Incorrect image description", "實寄信封存世至今只剩15枚，價值連城。", item.getImages().get(0).getDescription());
        Assert.assertNull("item.getVideo() is not null", item.getVideo());
        Assert.assertEquals("Incorrect item full description", "【我城‧我故事】<br><br>紙上不只是談兵，還可以細看香港歷史的變遷。吳貴龍上世紀80年代修讀印刷，之後順理成章到印刷廠工作，自此與紙品結下不解緣。不過，令人意想不到的是，他也慢慢喜愛上收藏紙品，", item.getDescription().substring(0, 100));
    }

    @NonNull
    private static String createHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/singpao.html"));
    }

    @NonNull
    private static String createDetailsHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/singpao_details.html"));
    }
}
