package com.github.ayltai.newspaper.util;

import java.util.Collection;

import android.support.annotation.NonNull;

import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public final class RxUtils {
    private RxUtils() {
    }

    public static <T> ObservableTransformer<T, T> applyObservableSchedulers(@NonNull final Scheduler scheduler) {
        return observable -> observable.observeOn(scheduler)
            .subscribeOn(scheduler);
    }

    public static <T> SingleTransformer<T, T> applySingleSchedulers(@NonNull final Scheduler scheduler) {
        return single -> single.observeOn(scheduler)
            .subscribeOn(scheduler);
    }

    public static <T> ObservableTransformer<T, T> applyObservableBackgroundSchedulers() {
        return observable -> observable.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io());
    }

    public static <T> SingleTransformer<T, T> applySingleBackgroundSchedulers() {
        return single -> single.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io());
    }

    public static <T> ObservableTransformer<T, T> applyObservableBackgroundToMainSchedulers() {
        return observable -> observable.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io());
    }

    public static <T> SingleTransformer<T, T> applySingleBackgroundToMainSchedulers() {
        return single -> single.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io());
    }

    public static <T> ObservableTransformer<T, T> applyObservableTrampolineSchedulers() {
        return observable -> observable.observeOn(Schedulers.trampoline())
            .subscribeOn(Schedulers.trampoline());
    }

    public static <T> SingleTransformer<T, T> applySingleTrampolineSchedulers() {
        return single -> single.observeOn(Schedulers.trampoline())
            .subscribeOn(Schedulers.trampoline());
    }

    public static void resetDisposables(@NonNull final Collection<Disposable> disposables) {
        for (final Disposable disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) disposable.dispose();
        }

        disposables.clear();
    }
}
