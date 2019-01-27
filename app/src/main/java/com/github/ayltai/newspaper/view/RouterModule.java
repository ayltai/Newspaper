package com.github.ayltai.newspaper.view;

import javax.annotation.Nonnull;

import android.app.Activity;

import androidx.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

@Module
public final class RouterModule {
    @Nonnull
    @NonNull
    private final Activity activity;

    public RouterModule(@Nonnull @NonNull @lombok.NonNull final Activity activity) {
        this.activity = activity;
    }

    @Nonnull
    @NonNull
    @Provides
    Router provideRouter() {
        return new MainRouter(this.activity);
    }
}
