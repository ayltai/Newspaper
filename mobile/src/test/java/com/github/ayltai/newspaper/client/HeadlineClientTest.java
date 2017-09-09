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

public final class HeadlineClientTest extends NetworkTest {
    private static final String HEADLINE_URL        = HeadlineClient.URL + HeadlineClient.CATEGORY_HONG_KONG;
    private static final String HEADLINE_DETAIL_URL = "http://hd.stheadline.com/news/daily/hk/601358/";

    private HeadlineClient client;

    @CallSuper
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn(Observable.just(HeadlineClientTest.createFeed())).when(this.apiService).getFeed(HeadlineClientTest.HEADLINE_URL);
        Mockito.doReturn(Observable.just(HeadlineClientTest.createHtml())).when(this.apiService).getHtml(HeadlineClientTest.HEADLINE_DETAIL_URL);

        this.client = new HeadlineClient(this.httpClient, this.apiService, SourceFactory.getInstance(RuntimeEnvironment.application).getSource("頭條日報"));
    }

    @Test
    public void Given_HeadlineUrl_When_getItemsIsCalled_Then_ItemsAreReturned() {
        final List<Item> items = this.client.getItems(HeadlineClientTest.HEADLINE_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 29, items.size());
        Assert.assertEquals("Incorrect item title", "鐵人賽頭重創後情緒低落  蔡若蓮長子抑鬱墮樓亡", items.get(0).getTitle());
        Assert.assertEquals("Incorrect item link", HeadlineClientTest.HEADLINE_DETAIL_URL, items.get(0).getLink());
        Assert.assertEquals("Incorrect item description", "        教育局副局長蔡若蓮長子潘匡仁，昨晨在家中伸腳出窗危坐，女傭發現並將他拉回屋內，他其後返回房間並將門上鎖，未幾墮樓，倒臥平台，送院後證實死亡。潘生前任職物理治療師，平日喜歡運動和玩樂器，去年在單車賽中撞傷頭部。警方表示，死者生前有情緒病紀錄，據悉是抑鬱症。蔡若蓮驚聞噩耗，到醫院見兒子最後一面，離開時神情哀傷，她感謝各界人士關心，希望各界給予空間讓她和家人渡過困難時間。\n" +
            "    死者潘匡仁(二十五歲)，洋名Peter，生前任職北區醫院物理治療師，居住柯士甸道擎天半島第三座四十一樓一單位。據土地註冊署資料顯示，該單位○三年以四百四十萬元一手購入。據悉，潘喜歡音樂及運動，去年參加三項鐵人賽中的單車賽時，遇上意外，頭部受傷，他患抑鬱症近一年。\n" +
            "伸腳出窗危坐  女傭拉回屋內\n" +
            "　　昨晨十一時許，大廈保安員突然聽到一聲墮物巨響，跑出查看時，發現一名男子倒臥第三座對開九樓平台血泊中，相信他由高處墮下，於是報警求助。消防及救護員接報趕至，為傷者急救及送院。傷者抵醫院被抬落救護車時陷昏迷，口部插着氧氣喉，面部及白色T恤沾有血漬，惜搶救後傷重不治。\n" +
            "　　警員封鎖現場調查，發現四十一樓一個單位有窗戶打開，相信事主由該單位墮樓。其後證實死者為教育局副局長蔡若蓮的二十五歲長子。\n" +
            "蔡若蓮：需空間渡過困難\n" +
            "　　消息稱，潘昨晨墮樓前，一度雙腳伸出窗台危坐，疑欲跳樓，女傭衝前制止並將他拉回屋內。其後，他反鎖自己在房內，未幾即發生墮樓事件。警方在現場未有發現遺書，現仍調查其墮樓是否與患病有關。\n" +
            "　　蔡若蓮透過教育局回應，希望各界給予空間讓她和家人渡過困難時間。教育局局長楊潤雄對事件深感難過和惋惜，教育局全體同事會全力支持副局長及與她同行，渡過困難的時刻。北區醫院表示，對事件深感難過。\n" +
            "　　蔡若蓮事後得悉長子墮樓重傷趕往醫院，惜抵醫院時其子已證實死亡，母子天人永隔。她見兒子最後一面，離開醫院時顯得哀傷，由男女陪同登車離去。相關新聞刊P4版\n" +
            " ", items.get(0).getDescription());
    }

    @Test
    public void Given_Item_When_updateItemIsCalled_Then_ItemIsUpdated() {
        final Item item = this.client.updateItem(this.client.getItems(HeadlineClientTest.HEADLINE_URL).blockingGet().get(0)).blockingGet();

        Assert.assertEquals("Incorrect item.getImages().size()", 5, item.getImages().size());
        Assert.assertEquals("Incorrect image URL", "http://static.stheadline.com/stheadline/news_res/2017/09/08/246161/wnnp002p01a.jpg", item.getImages().get(0).getUrl());
        Assert.assertEquals("Incorrect image description", "潘匡仁被送往伊利沙伯醫院，搶救不治。", item.getImages().get(0).getDescription());
        Assert.assertEquals("Incorrect item full description", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;教育局副局長蔡若蓮長子潘匡仁，昨晨在家中伸腳出窗危坐，女傭發現並將他拉回屋內，他其後返回房間並將門上鎖，未幾墮樓，倒臥平台，送院後證實死亡。潘生前任職物理治療師，平日喜歡運動和玩樂器，去年在單車賽中撞傷頭部。警方表示，死者生前有情緒病紀錄，據悉是抑鬱症。蔡若蓮驚聞噩耗，到醫院見兒子最後一面，離開時神情哀傷，她感謝各界人士關心，希望各界給予空間讓她和家人渡過困難時間。<br /><br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;死者潘匡仁(二十五歲)，洋名Peter，生前任職北區醫院物理治療師，居住柯士甸道擎天半島第三座四十一樓一單位。據土地註冊署資料顯示，該單位○三年以四百四十萬元一手購入。據悉，潘喜歡音樂及運動，去年參加三項鐵人賽中的單車賽時，遇上意外，頭部受傷，他患抑鬱症近一年。<br /><br />伸腳出窗危坐 女傭拉回屋內<br /><br />　　昨晨十一時許，大廈保安員突然聽到一聲墮物巨響，跑出查看時，發現一名男子倒臥第三座對開九樓平台血泊中，相信他由高處墮下，於是報警求助。消防及救護員接報趕至，為傷者急救及送院。傷者抵醫院被抬落救護車時陷昏迷，口部插着氧氣喉，面部及白色T恤沾有血漬，惜搶救後傷重不治。<br /><br />　　警員封鎖現場調查，發現四十一樓一個單位有窗戶打開，相信事主由該單位墮樓。其後證實死者為教育局副局長蔡若蓮的二十五歲長子。<br /><br />蔡若蓮：需空間渡過困難<br /><br />　　消息稱，潘昨晨墮樓前，一度雙腳伸出窗台危坐，疑欲跳樓，女傭衝前制止並將他拉回屋內。其後，他反鎖自己在房內，未幾即發生墮樓事件。警方在現場未有發現遺書，現仍調查其墮樓是否與患病有關。<br /><br />　　蔡若蓮透過教育局回應，希望各界給予空間讓她和家人渡過困難時間。教育局局長楊潤雄對事件深感難過和惋惜，教育局全體同事會全力支持副局長及與她同行，渡過困難的時刻。北區醫院表示，對事件深感難過。<br /><br />　　蔡若蓮事後得悉長子墮樓重傷趕往醫院，惜抵醫院時其子已證實死亡，母子天人永隔。她見兒子最後一面，離開醫院時顯得哀傷，由男女陪同登車離去。相關新聞刊P4版<br /><br />", item.getDescription());
    }

    @NonNull
    private static RssFeed createFeed() throws Exception {
        return new Persister().read(RssFeed.class, new FileInputStream("src/debug/assets/headline.xml"));
    }

    @NonNull
    private static String createHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/headline_details.html"));
    }
}
