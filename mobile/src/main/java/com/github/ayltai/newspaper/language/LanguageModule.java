package com.github.ayltai.newspaper.language;

import java.io.IOException;

import javax.inject.Singleton;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.cloud.language.v1beta2.LanguageServiceClient;

import com.github.ayltai.newspaper.util.TestUtils;

import dagger.Module;
import dagger.Provides;

@Module
public final class LanguageModule {
    @Nullable
    @Singleton
    @Provides
    static LanguageServiceClient provideLanguageServiceClient() {
        try {
            return LanguageServiceClient.create();
        } catch (final IOException e) {
            if (TestUtils.isLoggable()) Log.e(LanguageModule.class.getSimpleName(), e.getMessage(), e);
        }

        return null;
    }
}
