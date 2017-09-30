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
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.SourceFactory;
import com.github.ayltai.newspaper.rss.RssFeed;
import com.github.ayltai.newspaper.util.IOUtils;

import io.reactivex.Observable;

public final class HkejClientTest extends NetworkTest {
    private static final String HKEJ_URL         = "http://www.hkej.com/rss/onlinenews.xml";
    private static final String HKEJ_DETAILS_URL = "http://www2.hkej.com/instantnews/china/article/1653531";

    private HkejClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn(Observable.just(HkejClientTest.createFeed())).when(this.apiService).getFeed(HkejClientTest.HKEJ_URL);
        Mockito.doReturn(Observable.just(HkejClientTest.createHtml())).when(this.apiService).getHtml(HkejClientTest.HKEJ_DETAILS_URL);

        this.client = new HkejClient(this.httpClient, this.apiService, SourceFactory.getInstance(RuntimeEnvironment.application).getSource("信報"));
    }

    @Test
    public void Given_HkejUrl_When_getItemsIsCalled_Then_ItemsAreReturned() {
        final List<NewsItem> items = this.client.getItems(HkejClientTest.HKEJ_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 31, items.size());
        Assert.assertEquals("Incorrect item title", "官媒:央企重組不設時間表", items.get(0).getTitle());
        Assert.assertEquals("Incorrect item link", HkejClientTest.HKEJ_DETAILS_URL, items.get(0).getLink());
        Assert.assertEquals("Incorrect item description", "《人民日報》海外版發文指，中共十八大以來，國資委已先後完成18組34家企業的重組，中央企業由117戶調整至98戶。專家指出，在央企重組的過程中，關注央企數量...<br/><br/><a href='http://www2.hkej.com/instantnews/china/article/1653531'>全文：官媒:央企重組不設時間表</a><br/><br/><br/><font size='2'>信報財經新聞有限公司版權所有，不得轉載。Copyright © 2017 Hong Kong Economic Journal Company Limited. All rights</font>", items.get(0).getDescription());
    }

    @Test
    public void Given_Item_When_updateItemIsCalled_Then_ItemIsUpdated() {
        final Item item = this.client.updateItem(this.client.getItems(HkejClientTest.HKEJ_URL).blockingGet().get(0)).blockingGet();

        Assert.assertEquals("Incorrect item.getImages().size()", 1, item.getImages().size());
        Assert.assertEquals("Incorrect image URL", "http://static.hkej.com/hkej/images/2017/09/09/1653531_2aa9220c234423b5e5e37f9e3b39de3a.jpg", item.getImages().get(0).getUrl());
        Assert.assertEquals("Incorrect image description", "《人民日報》海外版發文指，央企重組不設時間表。(路透資料圖片)", item.getImages().get(0).getDescription());
        Assert.assertEquals("Incorrect item full description", "《人民日報》海外版發文指，中共十八大以來，國資委已先後完成18組34家企業的重組，中央企業由117戶調整至98戶。專家指出，在央企重組的過程中，關注央企數量變動的同時，更需特別重視質量、效益及競爭力方面的變化。未來，央企兼併重組力度仍將加大，提高質量效益和核心競爭力，會繼續成為重點。<br>\n" +
            "<br>文章指出，從央企效益提升的原因來看，一方面今年以來經濟的持續向好發展，為央企效益提升創造了良好環境；另一方面央企重組以後，效率的提升功不可沒，特別是在重組之後，央企核心競爭力的提升、資產負債率的下降，對央企利潤的增長提供了重要支撐。<br>\n" +
            "<br>下一步，國資委將繼續堅持以新發展理念為引領，以推進供給側結構性改革為主線，以提高質量效益和核心競爭力為中心，努力做好中央企業重組各項工作，推動國有資本布局優化和結構調整實現突破、取得成效。<br>\n" +
            "<br>總體來看，未來央企重組的大方向、路線圖都是明確的，且煤電行業、重型製造裝備行業、鋼鐵行業等依舊會是推進重組的重點領域。不過，具體什麼時間會進行重組、某一時期會重組多少家，這還需根據央企的具體發展情況、外在客觀條件等因素來定，「成熟一戶、推進一戶」依舊會是未來央企重組堅持的原則，所謂的央企重組時間表是沒有且沒有必要的。<br>", item.getDescription());
    }

    @NonNull
    private static RssFeed createFeed() throws Exception {
        return new Persister().read(RssFeed.class, new FileInputStream("src/debug/assets/hkej.xml"));
    }

    @NonNull
    private static String createHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/hkej_details.html"));
    }
}
