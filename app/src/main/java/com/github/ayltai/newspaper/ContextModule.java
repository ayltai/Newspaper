package com.github.ayltai.newspaper;

import android.content.Context;
import android.support.annotation.Nullable;

import dagger.Module;
import dagger.Provides;

@Module
public final class ContextModule {
    private final Context context;

    public ContextModule(@Nullable final Context context) {
        this.context = context;
    }

    @Provides
    public Context provideContext() {
        return this.context;
    }
}
