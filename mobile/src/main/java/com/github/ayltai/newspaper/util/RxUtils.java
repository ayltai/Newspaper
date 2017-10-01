package com.github.ayltai.newspaper.util;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import io.reactivex.FlowableTransformer;
import io.reactivex.MaybeTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public final class RxUtils {
    private RxUtils() {
    }

    public static <T> ObservableTransformer<T, T> applyObservableSchedulers(@NonNull final Scheduler scheduler) {
        return observable -> observable.observeOn(scheduler)
            .subscribeOn(scheduler);
    }

    public static <T> FlowableTransformer<T, T> applyFlowableSchedulers(@NonNull final Scheduler scheduler) {
        return flowable -> flowable.observeOn(scheduler)
            .subscribeOn(scheduler);
    }

    public static <T> SingleTransformer<T, T> applySingleSchedulers(@NonNull final Scheduler scheduler) {
        return single -> single.observeOn(scheduler)
            .subscribeOn(scheduler);
    }

    public static <T> ObservableTransformer<T, T> applyObservableBackgroundSchedulers() {
        if (TestUtils.isRunningUnitTest()) return RxUtils.applyObservableTrampolineSchedulers();

        return observable -> observable.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io());
    }

    public static <T> FlowableTransformer<T, T> applyFlowableBackgroundSchedulers() {
        if (TestUtils.isRunningUnitTest()) return RxUtils.applyFlowableTrampolineSchedulers();

        return flowable -> flowable.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io());
    }

    public static <T> SingleTransformer<T, T> applySingleBackgroundSchedulers() {
        if (TestUtils.isRunningUnitTest()) return RxUtils.applySingleTrampolineSchedulers();

        return single -> single.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io());
    }

    public static <T> MaybeTransformer<T, T> applyMaybeBackgroundSchedulers() {
        if (TestUtils.isRunningUnitTest()) return RxUtils.applyMaybeTrampolineSchedulers();

        return maybe -> maybe.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io());
    }

    public static <T> ObservableTransformer<T, T> applyObservableBackgroundToMainSchedulers() {
        if (TestUtils.isRunningUnitTest()) return RxUtils.applyObservableTrampolineSchedulers();

        return observable -> observable.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io());
    }

    public static <T> FlowableTransformer<T, T> applyFlowableBackgroundToMainSchedulers() {
        if (TestUtils.isRunningUnitTest()) return RxUtils.applyFlowableTrampolineSchedulers();

        return flowable -> flowable.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io());
    }

    public static <T> SingleTransformer<T, T> applySingleBackgroundToMainSchedulers() {
        if (TestUtils.isRunningUnitTest()) return RxUtils.applySingleTrampolineSchedulers();

        return single -> single.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io());
    }

    public static <T> MaybeTransformer<T, T> applyMaybeBackgroundToMainSchedulers() {
        if (TestUtils.isRunningUnitTest()) return RxUtils.applyMaybeTrampolineSchedulers();

        return maybe -> maybe.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io());
    }

    public static <T> ObservableTransformer<T, T> applyObservableTrampolineSchedulers() {
        return observable -> observable.observeOn(Schedulers.trampoline())
            .subscribeOn(Schedulers.trampoline());
    }

    public static <T> FlowableTransformer<T, T> applyFlowableTrampolineSchedulers() {
        return flowable -> flowable.observeOn(Schedulers.trampoline())
            .subscribeOn(Schedulers.trampoline());
    }

    public static <T> SingleTransformer<T, T> applySingleTrampolineSchedulers() {
        return single -> single.observeOn(Schedulers.trampoline())
            .subscribeOn(Schedulers.trampoline());
    }

    public static <T> MaybeTransformer<T, T> applyMaybeTrampolineSchedulers() {
        return maybe -> maybe.observeOn(Schedulers.trampoline())
            .subscribeOn(Schedulers.trampoline());
    }

    public static void resetDisposables(@NonNull final Collection<Disposable> disposables) {
        for (final Disposable disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) disposable.dispose();
        }

        disposables.clear();
    }

    public static Function<Observable<? extends Throwable>, Observable<?>> exponentialBackoff(final int initialDelayInSeconds, final int maxRetries, final Function<Throwable, Boolean> retryIf) {
        return observable -> observable.zipWith(Observable.range(0, maxRetries), (error, count) -> {
            if (count == maxRetries) return Pair.create(error, maxRetries);

            if (retryIf.apply(error)) return Pair.create(error, count);

            return Pair.create(error, maxRetries);
        }).flatMap(pair -> {
            final int count = pair.second;

            if (count == maxRetries) return Observable.error(pair.first);

            return Observable.timer((long)Math.pow(initialDelayInSeconds, count), TimeUnit.SECONDS);
        });
    }
}
