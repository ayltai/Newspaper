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

    public static final long CONNECT_TIMEOUT = 60;
    public static final long READ_TIMEOUT    = 60;
    public static final long WRITE_TIMEOUT   = 60;

    //endregion

    final OkHttpClient client;

    BaseHttpClient() {
        this.client = new OkHttpClient.Builder()
            .addNetworkInterceptor(new StethoInterceptor())
            .connectTimeout(BaseHttpClient.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(BaseHttpClient.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(BaseHttpClient.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build();

        if (TestUtils.isRunningInstrumentalTest() && BaseHttpClient.ASSETS.isEmpty()) {
            BaseHttpClient.ASSETS.put("http://www.hket.com/rss/headlines", R.raw.rss_hk);
            BaseHttpClient.ASSETS.put("http://orientaldaily.on.cc/rss/news.xml", R.raw.rss_intl);
            BaseHttpClient.ASSETS.put("https://www.singpao.com.hk/index.php?fi=news8", R.raw.rss_china);
            BaseHttpClient.ASSETS.put("https://news.mingpao.com/rss/pns/s00013.xml", R.raw.rss_entertainment);
        }
    }
}
