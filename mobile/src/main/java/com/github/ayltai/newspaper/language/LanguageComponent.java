package com.github.ayltai.newspaper.language;

import javax.inject.Singleton;

import android.support.annotation.Nullable;

import com.google.cloud.language.v1beta2.LanguageServiceClient;

import dagger.Component;

@Singleton
@Component(modules = { LanguageModule.class })
public interface LanguageComponent {
    @Nullable
    LanguageServiceClient languageServiceClient();
}
