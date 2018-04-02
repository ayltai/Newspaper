package com.github.ayltai.newspaper.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArraySet;
import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

public abstract class RealmLoader<D extends Comparable<D>> extends RxLoader<D> {
    protected static final String KEY_REFRESH = "refresh";

    private Realm realm;

    protected RealmLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context, args);
    }

    @NonNull
    protected Scheduler getScheduler() {
        return DevUtils.isRunningUnitTest() ? Schedulers.trampoline() : DataManager.SCHEDULER;
    }

    @Nullable
    protected Realm getRealm() {
        return this.realm;
    }

    protected boolean isValid() {
        if (this.realm == null) {
            if (DevUtils.isLoggable()) throw new IllegalStateException("No Realm instance is available");

            return false;
        }

        return true;
    }

    @NonNull
    @Override
    protected Flowable<List<D>> load(@NonNull final Context context, @Nullable final Bundle args) {
        return Flowable.create(emitter -> this.loadFromLocalSource(context, args)
            .zipWith(this.loadFromRemoteSource(context, args), (localItems, remoteItems) -> {
                final Set<D> results = new ArraySet<>();
                results.addAll(localItems);
                results.addAll(remoteItems);

                return new ArrayList<>(results);
            })
            .subscribe(
                results -> {
                    Collections.sort(results);

                    emitter.onNext(results);
                },
                emitter::onError
            ), BackpressureStrategy.LATEST);
    }

    @NonNull
    protected abstract Flowable<List<D>> loadFromLocalSource(@NonNull Context context, @Nullable Bundle args);

    @NonNull
    protected abstract Flowable<List<D>> loadFromRemoteSource(@NonNull Context context, @Nullable Bundle args);

    @CallSuper
    @Override
    protected void onForceLoad() {
        Single.<Realm>create(emitter -> emitter.onSuccess(DaggerDataComponent.builder()
                .dataModule(new DataModule(this.getContext()))
                .build()
                .realm()))
            .compose(RxUtils.applySingleSchedulers(this.getScheduler()))
            .subscribe(
                realm -> {
                    this.realm = realm;

                    if (DevUtils.isLoggable()) Log.d(this.getClass().getSimpleName(), "A Realm instance is created");

                    super.onForceLoad();
                },
                error -> {
                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                });
    }

    @CallSuper
    @Override
    protected boolean onCancelLoad() {
        final boolean result = super.onCancelLoad();

        Single.<Irrelevant>create(
            emitter -> {
                if (this.realm != null) {
                    this.realm.close();
                    this.realm = null;
                }

                if (!emitter.isDisposed()) emitter.onSuccess(Irrelevant.INSTANCE);
            })
            .compose(RxUtils.applySingleSchedulers(this.getScheduler()))
            .subscribe(
                irrelevant -> {
                    if (DevUtils.isLoggable()) Log.d(this.getClass().getSimpleName(), "A Realm instance is closed");
                },
                error -> {
                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                });

        return result;
    }

    protected static boolean isForceRefresh(@Nullable final Bundle args) {
        return args != null && args.getBoolean(RealmLoader.KEY_REFRESH, false);
    }
}
