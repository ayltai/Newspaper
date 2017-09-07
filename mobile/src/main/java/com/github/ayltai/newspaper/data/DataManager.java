package com.github.ayltai.newspaper.data;

import android.support.annotation.NonNull;

import io.realm.Realm;

public abstract class DataManager {
    private final Realm realm;

    protected DataManager(@NonNull final Realm realm) {
        this.realm = realm;
    }

    @NonNull
    public Realm getRealm() {
        return this.realm;
    }
}
