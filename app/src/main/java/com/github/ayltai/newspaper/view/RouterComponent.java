package com.github.ayltai.newspaper.view;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import androidx.annotation.NonNull;

import com.github.ayltai.newspaper.MainActivity;

import dagger.Component;

@Singleton
@Component(modules = { RouterModule.class })
public interface RouterComponent {
    @Nonnull
    @NonNull
    Router router();

    void inject(@Nonnull @NonNull MainActivity activity);
}
