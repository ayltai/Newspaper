package com.github.ayltai.newspaper.data;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;

import dagger.Component;
import io.realm.Realm;

@Component(modules = { DataModule.class })
public interface DataComponent {
    @Nonnull
    @NonNull
    Realm realm();
}
