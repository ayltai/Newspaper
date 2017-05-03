package com.github.ayltai.newspaper.net;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.facebook.stetho.okhttp3.StethoInterceptor;
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

        if (TestUtils.isRunningInstrumentalTest() && HttpClient.ASSETS.isEmpty()) {
            // Puts mock responses into ASSETS
        }
    }
}
