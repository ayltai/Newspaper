package com.github.ayltai.newspaper.data;

import android.support.annotation.NonNull;

import dagger.Component;
import io.realm.Realm;

@Component(modules = { DataModule.class })
public interface DataComponent {
    @NonNull
    Realm realm();
}
