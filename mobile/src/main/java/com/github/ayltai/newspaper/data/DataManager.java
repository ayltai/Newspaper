package com.github.ayltai.newspaper.data;

import java.util.concurrent.Executors;

import android.support.annotation.NonNull;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

public abstract class DataManager {
    public static final Scheduler SCHEDULER = Schedulers.from(Executors.newSingleThreadExecutor());

    private final Realm realm;

    protected DataManager(@NonNull final Realm realm) {
        this.realm = realm;
    }

    @NonNull
    public Realm getRealm() {
        return this.realm;
    }
}
