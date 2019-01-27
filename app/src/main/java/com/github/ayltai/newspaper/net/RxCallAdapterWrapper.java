package com.github.ayltai.newspaper.net;

import java.lang.reflect.Type;

import javax.annotation.Nonnull;

import android.util.Log;

import androidx.annotation.NonNull;

import com.github.ayltai.newspaper.util.DevUtils;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import retrofit2.Call;
import retrofit2.CallAdapter;

final class RxCallAdapterWrapper implements CallAdapter<Single<?>, Single<?>> {
    @Nonnull
    @NonNull
    private final CallAdapter<?, ?> adapter;

    RxCallAdapterWrapper(@Nonnull @NonNull @lombok.NonNull final CallAdapter<?, ?> adapter) {
        this.adapter = adapter;
    }

    @Nonnull
    @NonNull
    @Override
    public Type responseType() {
        return this.adapter.responseType();
    }

    @Nonnull
    @NonNull
    @Override
    public Single<?> adapt(@Nonnull @NonNull @lombok.NonNull final Call call) {
        return ((Single)this.adapter.adapt(call)).onErrorResumeNext(new Function<Throwable, Single>() {
            @Override
            public Single apply(@NonNull final Throwable throwable) {
                if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), "Error URL = " + call.request().url());

                return Single.error(throwable);
            }
        });
    }
}
