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

public final class AppleDailyClientTest extends NetworkTest {
    private static final String APPLE_DAILY_URL = "http://hk.apple.nextmedia.com/video/videolist/20170909/local/home/0";

    private AppleDailyClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn(Observable.just(AppleDailyClientTest.createHtml())).when(this.apiService).getHtml(AppleDailyClientTest.APPLE_DAILY_URL);

        this.client = new AppleDailyClient(this.httpClient, this.apiService, SourceFactory.getInstance(RuntimeEnvironment.application).getSource("蘋果日報"));
    }

    @Test
    public void Given_AppleDailyUrl_When_getItemsIsCalled_Then_ItemsAreReturned() {
        final List<Item> items = this.client.getItems(AppleDailyClientTest.APPLE_DAILY_URL).blockingGet();

        Assert.assertEquals("Incorrect items.size()", 28, items.size());
        Assert.assertEquals("Incorrect item title", "教大民主牆令校長好痛心　閉路離奇流出咩居心？", items.get(0).getTitle());
        Assert.assertNull("Incorrect item description", items.get(0).getDescription());
    }

    @NonNull
    private static String createHtml() throws IOException {
        return IOUtils.readString(new FileInputStream("src/debug/assets/appledaily_video_list.html"));
    }
}
