package com.github.ayltai.newspaper.net;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public final class NetModule {
    @Provides
    public HttpClient provideHttpClient(final Context context) {
        return new HttpClient(context);
    }
}
