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

public final class AppleDailyClientTest extends NetworkTest {
    private static final String APPLE_DAILY_URL         = "http://hk.apple.nextmedia.com/video/videolist/20170909/local/home/0";
    private static final String APPLE_DAILY_DETAILS_URL = "http://hk.apple.nextmedia.com/news/art/20170909/20147535";

    private AppleDailyClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doAnswer(invocationOnMock -> {
            final String url = invocationOnMock.getArgument(0);

            if (AppleDailyClientTest.APPLE_DAILY_URL.equals(url)) return Observable.just(AppleDailyClientTest.createHtml());
            if (AppleDailyClientTest.APPLE_DAILY_DETAILS_URL.equals(url)) return Observable.just(AppleDailyClientTest.createDetailsHtml());

            return Observable.just(AppleDailyClientTest.createVideoHtml());
        }).when(this.apiService).getHtml(Mockito.anyString());

        this.client = new AppleDailyClient(this.httpClient, this.apiService, SourceFactory.getInstance(RuntimeEnvironment.application).getSource("蘋果日報"));
    }

    @Test
    public void Given_AppleDailyUrl_When_getItemsIsCalled_Then_ItemsAreReturned() {
        final List<NewsItem> items = this.client.getItems(AppleDailyClientTest.APPLE_DAILY_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 28, items.size());
        Assert.assertEquals("Incorrect item title", "教大民主牆令校長好痛心　閉路離奇流出咩居心？", items.get(0).getTitle());
        Assert.assertEquals("Incorrect item link", AppleDailyClientTest.APPLE_DAILY_DETAILS_URL, items.get(0).getLink());
        Assert.assertNull("Incorrect item description", items.get(0).getDescription());
    }

    @Test
    public void Given_Item_When_updateItemIsCalled_Then_ItemIsUpdated() {
        final Item item = this.client.updateItem(this.client.getItems(AppleDailyClientTest.APPLE_DAILY_URL).blockingGet().get(0)).blockingGet();

        Assert.assertEquals("Incorrect item.getImages().size()", 6, item.getImages().size());
        Assert.assertEquals("Incorrect image URL", "http://static.apple.nextmedia.com/images/apple-photos/apple/20170909/large/09la1p201new.jpg", item.getImages().get(0).getUrl());
        Assert.assertEquals("Incorrect image description", "■教大校長張仁良昨譴責冒犯性標語時眼泛淚光。王心義攝", item.getImages().get(0).getDescription());
        Assert.assertNotNull("item.getVideo() is null", item.getVideo());
        Assert.assertEquals("Incorrect video URL", "http://video.appledaily.com.hk/mcp/encode/2017/09/09/3437461/20170908_news_53_newADAD_w.mp4", item.getVideo().getVideoUrl());
        Assert.assertEquals("Incorrect video thumbnail URL", "http://static.apple.nextmedia.com/images/apple-photos/video/20170909/org/1504900568_d1c2.jpg", item.getVideo().getThumbnailUrl());
        Assert.assertEquals("Incorrect item full description", "<p class=\"ArticleIntro\">\n                                \t【本報訊】教育局副局長蔡若蓮長子周四跳樓身亡，教育大學學生會民主牆出現冒犯性標語，行", item.getDescription().substring(0, 100));
    }

    @NonNull
    private static String createHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/appledaily.html"));
    }

    @NonNull
    private static String createDetailsHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/appledaily_details.html"));
    }

    @NonNull
    private static String createVideoHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/appledaily_video.html"));
    }
}
