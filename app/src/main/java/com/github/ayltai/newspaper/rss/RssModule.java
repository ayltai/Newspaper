package com.github.ayltai.newspaper.rss;

import com.github.ayltai.newspaper.net.HttpClient;

import dagger.Module;
import dagger.Provides;

@Module
public final class RssModule {
    @Provides
    public Client provideClient(final HttpClient httpClient) {
        return new Client(httpClient);
    }
}
