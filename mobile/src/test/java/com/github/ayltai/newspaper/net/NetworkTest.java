package com.github.ayltai.newspaper.net;

import android.support.annotation.CallSuper;

import com.github.ayltai.newspaper.AppUnitTest;

import org.junit.Before;
import org.mockito.Mockito;

import okhttp3.OkHttpClient;

public abstract class NetworkTest extends AppUnitTest {
    protected OkHttpClient httpClient;
    protected ApiService   apiService;

    @CallSuper
    @Before
    public void setUp() throws Exception {
        super.setUp();

        this.httpClient = Mockito.mock(OkHttpClient.class);
        this.apiService = Mockito.mock(ApiService.class);
    }
}
