package com.github.ayltai.newspaper.language;

import javax.inject.Singleton;

import android.support.annotation.NonNull;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import dagger.Module;
import dagger.Provides;

@Module
public final class LanguageModule {
    private final GoogleCredential credential;

    public LanguageModule(@NonNull final GoogleCredential credential) {
        this.credential = credential;
    }

    @NonNull
    @Singleton
    @Provides
    public LanguageService provideLanguageService() {
        return new LanguageService(this.credential);
    }
}
