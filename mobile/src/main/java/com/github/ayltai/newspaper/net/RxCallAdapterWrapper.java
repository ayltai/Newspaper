package com.github.ayltai.newspaper.net;

import java.lang.reflect.Type;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import retrofit2.Call;
import retrofit2.CallAdapter;

public final class RxCallAdapterWrapper implements CallAdapter<Observable<?>, Observable<?>> {
    @NonNull
    private final CallAdapter<?, ?> adapter;

    RxCallAdapterWrapper(@Nullable final CallAdapter<?, ?> adapter) {
        if (adapter == null) throw new IllegalArgumentException("The CallAdapter to be wrapped cannot be null");

        this.adapter = adapter;
    }

    @NonNull
    @Override
    public Type responseType() {
        return this.adapter.responseType();
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public Observable<?> adapt(@NonNull final Call call) {
        return ((Observable)this.adapter.adapt(call)).onErrorResumeNext(new Function<Throwable, Observable>() {
            @Override
            public Observable apply(@NonNull final Throwable throwable) {
                return Observable.error(throwable);
            }
        });
    }
}
