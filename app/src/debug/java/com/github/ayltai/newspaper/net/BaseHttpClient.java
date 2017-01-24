package com.github.ayltai.newspaper.net;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.TestUtils;

import okhttp3.OkHttpClient;

public abstract class BaseHttpClient {
    static final Map<String, Integer> ASSETS = new HashMap<>();

    //region Constants

    private static final long CONNECT_TIMEOUT = 60;
    private static final long READ_TIMEOUT    = 60;
    private static final long WRITE_TIMEOUT   = 60;

    //endregion

    final OkHttpClient client;

    BaseHttpClient() {
        this.client = new OkHttpClient.Builder()
            .addNetworkInterceptor(new StethoInterceptor())
            .connectTimeout(BaseHttpClient.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(BaseHttpClient.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(BaseHttpClient.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build();

        if (TestUtils.isRunningTest() && HttpClient.ASSETS.isEmpty()) {
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/basketball", R.raw.rss_basketball);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/business", R.raw.rss_business);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/china", R.raw.rss_china);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/entertainment", R.raw.rss_entertainment);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/food", R.raw.rss_food);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/health", R.raw.rss_health);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/hongkong-business", R.raw.rss_hk_business);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/hongkong-entertainment", R.raw.rss_hk_entertainment);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/hong-kong", R.raw.rss_hk);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/international-entertainment", R.raw.rss_international_entertainment);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/intl", R.raw.rss_intl);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/living", R.raw.rss_living);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/other-sports", R.raw.rss_other_sports);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/other-supplement", R.raw.rss_other_supplement);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/property", R.raw.rss_property);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/soccer", R.raw.rss_soccer);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/sports", R.raw.rss_sports);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/supplement", R.raw.rss_supplement);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/tech", R.raw.rss_tech);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/", R.raw.rss_top);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/travel", R.raw.rss_travel);
            HttpClient.ASSETS.put("https://hk.news.yahoo.com/rss/world", R.raw.rss_world);
        }
    }
}
