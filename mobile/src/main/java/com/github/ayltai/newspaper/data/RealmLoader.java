package com.github.ayltai.newspaper.data;

import java.util.Collection;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.ayltai.newspaper.net.NetworkUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

public abstract class RealmLoader<D> extends RxLoader<D> {
    protected static final String KEY_REFRESH = "refresh";

    private Realm realm;

    protected RealmLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context, args);
    }

    @NonNull
    protected Scheduler getScheduler() {
        return TestUtils.isRunningUnitTest() ? Schedulers.trampoline() : DataManager.SCHEDULER;
    }

    @Nullable
    protected Realm getRealm() {
        return this.realm;
    }

    protected boolean isValid() {
        if (this.realm == null) {
            if (TestUtils.isLoggable()) throw new IllegalStateException("No Realm instance is available");

            return false;
        }

        return true;
    }

    @NonNull
    @Override
    protected Flowable<D> load(@NonNull final Context context, @Nullable final Bundle args) {
        final boolean isOnline = NetworkUtils.isOnline(context);

        if (isOnline && RealmLoader.isForceRefresh(args)) return this.loadFromRemoteSource(context, args);

        if (this.isValid()) {
            return Flowable.create(emitter -> this.loadFromLocalSource(context, args)
                .compose(RxUtils.applyFlowableSchedulers(this.getScheduler()))
                .subscribe(
                    data -> {
                        if (data instanceof Collection && !((Collection)data).isEmpty()) emitter.onNext(data);

                        if (isOnline) {
                            this.loadFromRemoteSource(context, args)
                                .subscribe(items -> {
                                    if (items instanceof Collection && !((Collection)items).isEmpty()) {
                                        emitter.onNext(items);
                                    } else {
                                        emitter.onNext(data);
                                    }
                                });
                        } else {
                            emitter.onNext(data);
                        }
                    },
                    error -> {
                        if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                    }), BackpressureStrategy.LATEST);
        }

        return this.loadFromRemoteSource(context, args);
    }

    @NonNull
    protected abstract Flowable<D> loadFromLocalSource(@NonNull Context context, @Nullable Bundle args);

    @NonNull
    protected abstract Flowable<D> loadFromRemoteSource(@NonNull Context context, @Nullable Bundle args);

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

                    if (TestUtils.isLoggable()) Log.d(this.getClass().getSimpleName(), "A Realm instance is created");

                    super.onForceLoad();
                },
                error -> {
                    if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
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

                emitter.onSuccess(Irrelevant.INSTANCE);
            })
            .compose(RxUtils.applySingleSchedulers(this.getScheduler()))
            .subscribe(
                irrelevant -> {
                    if (TestUtils.isLoggable()) Log.d(this.getClass().getSimpleName(), "A Realm instance is closed");
                },
                error -> {
                    if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                });

        return result;
    }

    protected static boolean isForceRefresh(@Nullable final Bundle args) {
        return args != null && args.getBoolean(RealmLoader.KEY_REFRESH, false);
    }
}
