package com.github.ayltai.newspaper.util;

import java.util.Collection;

import javax.annotation.Nonnull;

import android.util.Log;

import androidx.annotation.NonNull;

import com.akaita.java.rxjava2debug.RxJava2Debug;

import io.reactivex.FlowableTransformer;
import io.reactivex.MaybeTransformer;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RxUtils {
    public void resetDisposables(@Nonnull @NonNull @lombok.NonNull final Collection<Disposable> disposables) {
        for (final Disposable disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) disposable.dispose();
        }

        disposables.clear();
    }

    public <T extends Throwable> void handleError(@Nonnull @NonNull @lombok.NonNull final T error) {
        if (DevUtils.isLoggable()) Log.e(RxUtils.class.getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
    }

    public <T> FlowableTransformer<T, T> applyFlowableSchedulers(@Nonnull @NonNull @lombok.NonNull final Scheduler scheduler) {
        return source -> source.observeOn(scheduler).subscribeOn(scheduler);
    }

    public <T> ObservableTransformer<T, T> applyObservableSchedulers(@Nonnull @NonNull @lombok.NonNull final Scheduler scheduler) {
        return source -> source.observeOn(scheduler).subscribeOn(scheduler);
    }

    public <T> SingleTransformer<T, T> applySingleSchedulers(@Nonnull @NonNull @lombok.NonNull final Scheduler scheduler) {
        return source -> source.observeOn(scheduler).subscribeOn(scheduler);
    }

    public <T> MaybeTransformer<T, T> applyMaybeSchedulers(@Nonnull @NonNull @lombok.NonNull final Scheduler scheduler) {
        return source -> source.observeOn(scheduler).subscribeOn(scheduler);
    }

    public <T> FlowableTransformer<T, T> applyFlowableTrampolineSchedulers() {
        return source -> source.observeOn(Schedulers.trampoline()).subscribeOn(Schedulers.trampoline());
    }

    public <T> ObservableTransformer<T, T> applyObservableTrampolineSchedulers() {
        return source -> source.observeOn(Schedulers.trampoline()).subscribeOn(Schedulers.trampoline());
    }

    public <T> SingleTransformer<T, T> applySingleTrampolineSchedulers() {
        return source -> source.observeOn(Schedulers.trampoline()).subscribeOn(Schedulers.trampoline());
    }

    public <T> MaybeTransformer<T, T> applyMaybeTrampolineSchedulers() {
        return source -> source.observeOn(Schedulers.trampoline()).subscribeOn(Schedulers.trampoline());
    }

    public <T> FlowableTransformer<T, T> applyFlowableBackgroundSchedulers() {
        if (DevUtils.isRunningUnitTest()) return RxUtils.applyFlowableTrampolineSchedulers();

        return source -> source.observeOn(Schedulers.io()).subscribeOn(Schedulers.io());
    }

    public <T> ObservableTransformer<T, T> applyObservableBackgroundSchedulers() {
        if (DevUtils.isRunningUnitTest()) return RxUtils.applyObservableTrampolineSchedulers();

        return source -> source.observeOn(Schedulers.io()).subscribeOn(Schedulers.io());
    }

    public <T> SingleTransformer<T, T> applySingleBackgroundSchedulers() {
        if (DevUtils.isRunningUnitTest()) return RxUtils.applySingleTrampolineSchedulers();

        return source -> source.observeOn(Schedulers.io()).subscribeOn(Schedulers.io());
    }

    public <T> MaybeTransformer<T, T> applyMaybeBackgroundSchedulers() {
        if (DevUtils.isRunningUnitTest()) return RxUtils.applyMaybeTrampolineSchedulers();

        return source -> source.observeOn(Schedulers.io()).subscribeOn(Schedulers.io());
    }

    public <T> FlowableTransformer<T, T> applyFlowableBackgroundToMainSchedulers() {
        if (DevUtils.isRunningUnitTest()) return RxUtils.applyFlowableTrampolineSchedulers();

        return source -> source.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public <T> ObservableTransformer<T, T> applyObservableBackgroundToMainSchedulers() {
        if (DevUtils.isRunningUnitTest()) return RxUtils.applyObservableTrampolineSchedulers();

        return source -> source.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public <T> SingleTransformer<T, T> applySingleBackgroundToMainSchedulers() {
        if (DevUtils.isRunningUnitTest()) return RxUtils.applySingleTrampolineSchedulers();

        return source -> source.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public <T> MaybeTransformer<T, T> applyMaybeBackgroundToMainSchedulers() {
        if (DevUtils.isRunningUnitTest()) return RxUtils.applyMaybeTrampolineSchedulers();

        return source -> source.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }
}
