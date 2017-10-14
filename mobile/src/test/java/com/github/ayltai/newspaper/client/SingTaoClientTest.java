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

public final class SingTaoClientTest extends NetworkTest {
    private static final String SING_TAO_URL         = "http://std.stheadline.com/daily/section-list.php?cat=12";
    private static final String SING_TAO_DETAILS_URL = "http://std.stheadline.com/daily/news-content.php?id=1662528&target=2";
    private static final String ERROR_URL            = "error 1";
    private static final String ERROR_DETAILS_URL    = "error 2";

    private SingTaoClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn(Observable.just(SingTaoClientTest.createHtml())).when(this.apiService).getHtml(SingTaoClientTest.SING_TAO_URL);
        Mockito.doReturn(Observable.just(SingTaoClientTest.createDetailsHtml())).when(this.apiService).getHtml(SingTaoClientTest.SING_TAO_DETAILS_URL);
        Mockito.doReturn(Observable.error(new RuntimeException("Fake error 1"))).when(this.apiService).getHtml(SingTaoClientTest.ERROR_URL);
        Mockito.doReturn(Observable.error(new RuntimeException("Fake error 2"))).when(this.apiService).getHtml(SingTaoClientTest.ERROR_DETAILS_URL);

        this.client = new SingTaoClient(this.httpClient, this.apiService, SourceFactory.getInstance(RuntimeEnvironment.application).getSource("星島日報"));
    }

    @Test
    public void Given_singTaoUrl_When_getItemsIsCalled_Then_itemsAreReturned() {
        final List<NewsItem> items = this.client.getItems(SingTaoClientTest.SING_TAO_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 13, items.size());
        Assert.assertEquals("Incorrect item title", "教大生傳被永不錄用 楊潤雄：未有聽聞", items.get(0).getTitle());
        Assert.assertEquals("Incorrect item link", SingTaoClientTest.SING_TAO_DETAILS_URL, items.get(0).getLink());
        Assert.assertEquals("Incorrect item description", "教育大學民主牆日前出現「恭喜」教育局副局長蔡若蓮長子自殺標語後，各界高調譴責。風波未平，教大學生會會長黎曉晴昨引述，日前參與教大內部師生會議時校方曾匯報接獲大量聯署投訴，有其他校長稱「永不錄用」教大學生或近幾屆畢業生等，又指有十名學生因而被取消實習機會。教育局局長楊潤雄昨表示，未有聽聞校長表示對教大生永不錄用的說法。\n" +
            "　　教大學生會會長黎曉晴昨引述校方表示，民主牆事件發生後接", items.get(0).getDescription());
    }

    @Test
    public void Given_errorUrl_When_getItemsIsCalled_Then_noItemsAreReturned() {
        final List<NewsItem> items = this.client.getItems(SingTaoClientTest.ERROR_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 0, items.size());
    }

    @Test
    public void Given_item_When_updateItemIsCalled_Then_itemIsUpdated() {
        final Item item = this.client.updateItem(this.client.getItems(SingTaoClientTest.SING_TAO_URL).blockingGet().get(0)).blockingGet();

        Assert.assertEquals("Incorrect item.getImages().size()", 1, item.getImages().size());
        Assert.assertEquals("Incorrect image URL", "http://static.stheadline.com/stheadline/news_res/2017/09/10/248310/i_491x369_704530316.jpg", item.getImages().get(0).getUrl());
        Assert.assertEquals("Incorrect image description", "教大民主牆被貼冷血大字報風波持續，有傳個別校長表明永不錄用教大生。", item.getImages().get(0).getDescription());
        Assert.assertNull("item.getVideo() is not null", item.getVideo());
        Assert.assertEquals("Incorrect item full description", "　　(星島日報報道)教育大學民主牆日前出現「恭喜」教育局副局長蔡若蓮長子自殺標語後，各界高調譴責。風", item.getDescription().substring(0, 50));
    }

    @Test(expected = RuntimeException.class)
    public void Given_singTaoDetailsErrorUrl_When_updateItemIsCalled_noItemIsUpdated() {
        final NewsItem newsItem = new NewsItem();
        newsItem.setLink(SingTaoClientTest.ERROR_DETAILS_URL);

        this.client.updateItem(newsItem).blockingGet();
    }

    @NonNull
    private static String createHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/singtao.html"));
    }

    @NonNull
    private static String createDetailsHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/singtao_details.html"));
    }
}
