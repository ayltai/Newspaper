package com.github.ayltai.newspaper.net;

import java.util.concurrent.TimeUnit;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import okhttp3.OkHttpClient;

public abstract class BaseHttpClient {
    //region Constants

    private static final long CONNECT_TIMEOUT = 60;
    private static final long READ_TIMEOUT    = 60;
    private static final long WRITE_TIMEOUT   = 60;

    //endregion

    protected final OkHttpClient client;

    protected BaseHttpClient() {
        this.client = new OkHttpClient.Builder()
            .addNetworkInterceptor(new StethoInterceptor())
            .connectTimeout(BaseHttpClient.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(BaseHttpClient.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(BaseHttpClient.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build();
    }
}
