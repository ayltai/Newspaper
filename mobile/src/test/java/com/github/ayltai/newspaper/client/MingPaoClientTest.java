package com.github.ayltai.newspaper.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import android.support.annotation.CallSuper;
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

public final class MingPaoClientTest extends NetworkTest {
    private static final String MING_PAO_URL            = "https://news.mingpao.com/rss/pns/s00001.xml";
    private static final String MING_PAO_ISSUE_LIST_URL = "https://news.mingpao.com/dat/pns/issuelist.js";
    private static final String MING_PAO_DETAILS_URL    = "https://news.mingpao.com/dat/pns/pns_web_tc/article1/20170909_9a6f9b7d2a/todaycontent_1504893031511.js";

    private MingPaoClient client;

    @CallSuper
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn(Observable.just(MingPaoClientTest.createFeed())).when(this.apiService).getFeed(MingPaoClientTest.MING_PAO_URL);
        Mockito.doReturn(Observable.just(MingPaoClientTest.createJson())).when(this.apiService).getHtml(MingPaoClientTest.MING_PAO_DETAILS_URL);
        Mockito.doReturn(Observable.just(MingPaoClientTest.createJs())).when(this.apiService).getHtml(MingPaoClientTest.MING_PAO_ISSUE_LIST_URL);

        this.client = new MingPaoClient(this.httpClient, this.apiService, SourceFactory.getInstance(RuntimeEnvironment.application).getSource("明報"));
    }

    @Test
    public void Given_HeadlineUrl_When_getItemsIsCalled_Then_ItemsAreReturned() {
        final List<Item> items = this.client.getItems(MingPaoClientTest.MING_PAO_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 7, items.size());
        Assert.assertEquals("Incorrect item title", "民主牆「恭喜」蔡若蓮  林鄭譴責濫用言論自由 張仁良：給空間教大自行處理", items.get(0).getTitle());
        Assert.assertEquals("Incorrect item link", "https://news.mingpao.com/pns/dailynews/web_tc/article/20170909/s00001/1504893031511", items.get(0).getLink());
        Assert.assertEquals("Incorrect item description", "【明報專訊】教育局副局長蔡若蓮長子前日墮樓身亡，教育大學民主牆同日一度出現「恭喜」等冒犯標語，行政長官林鄭月娥昨強烈譴責，她又將上述事件與連繫各大學近期出現港獨標語事件，指兩件涉及大學民主牆事件是「濫用言論自由的行為」，言論已超越社會底線，要求校方盡快處理，教大校長張仁良就呼籲社會給予空間讓校方了解和處理事件。", items.get(0).getDescription());
    }

    @Test
    public void Given_Item_When_updateItemIsCalled_Then_ItemIsUpdated() {
        final Item item = this.client.updateItem(this.client.getItems(MingPaoClientTest.MING_PAO_URL).blockingGet().get(0)).blockingGet();

        Assert.assertEquals("Incorrect item.getImages().size()", 3, item.getImages().size());
        Assert.assertEquals("Incorrect image URL", "https://fs.mingpao.com/pns/20170909/s00006/da238267e7281166132faaaf3132c3bc.jpg", item.getImages().get(0).getUrl());
        Assert.assertEquals("Incorrect image description", "教大校長張仁良前晚主動回應冒犯言論，昨午亦主動會見傳媒，指若張貼冒犯言論者是學生會按既定程序處理。被問到若冒犯言論非針對知名人士，校方處理手法會否不同，張表示「我可以斬釘截鐵說不會」，指希望讓學生學會尊重他人，享有言論自由的同時，不應作出人身攻擊或誹謗。（曾憲宗攝）", item.getImages().get(0).getDescription());
        Assert.assertEquals("Incorrect item full description", "<p>【明報專訊】教育局副局長蔡若蓮長子前日墮樓身亡，教育大學民主牆同日一度出現「恭喜」等冒犯標語，行政長官林鄭月娥昨強烈譴責，她又將上述事件與連繫各大學近期出現港獨標語事件，指兩件涉及大學民主牆事件", item.getDescription().substring(0, 100));
    }

    // TODO: Checks video news

    @NonNull
    private static RssFeed createFeed() throws Exception {
        return new Persister().read(RssFeed.class, new FileInputStream("src/debug/assets/mingpao.xml"));
    }

    @NonNull
    private static String createJson() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/mingpao_details.js"));
    }

    @NonNull
    private static String createJs() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/mingpao_issue_list.js"));
    }
}
