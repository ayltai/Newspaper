package com.github.ayltai.newspaper.language;

import javax.inject.Singleton;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.BuildConfig;
import com.textrazor.TextRazor;

import dagger.Module;
import dagger.Provides;

@Module
final class LanguageModule {
    @NonNull
    @Singleton
    @Provides
    static LanguageService provideLanguageService() {
        return new LanguageService(new TextRazor(BuildConfig.NLP_API_TOKEN));
    }
}
