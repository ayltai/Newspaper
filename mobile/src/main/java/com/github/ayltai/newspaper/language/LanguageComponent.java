package com.github.ayltai.newspaper.language;

import javax.inject.Singleton;

import android.support.annotation.NonNull;

import dagger.Component;

@Singleton
@Component(modules = { LanguageModule.class })
public interface LanguageComponent {
    @NonNull
    LanguageService languageService();
}
