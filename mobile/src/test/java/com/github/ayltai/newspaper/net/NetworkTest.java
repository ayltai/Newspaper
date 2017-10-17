package com.github.ayltai.newspaper.net;

import android.support.annotation.CallSuper;

import org.junit.Before;
import org.mockito.Mockito;

import com.github.ayltai.newspaper.UnitTest;

import okhttp3.OkHttpClient;

public abstract class NetworkTest extends UnitTest {
    protected OkHttpClient httpClient;
    protected NewsApiService apiService;

    @CallSuper
    @Before
    public void setUp() throws Exception {
        super.setUp();

        this.httpClient = Mockito.mock(OkHttpClient.class);
        this.apiService = Mockito.mock(NewsApiService.class);
    }
}
