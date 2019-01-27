package com.github.ayltai.newspaper.net;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;

import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

final class RxErrorHandlingCallAdapterFactory extends CallAdapter.Factory {
    private final RxJava2CallAdapterFactory factory = RxJava2CallAdapterFactory.create();

    public static CallAdapter.Factory create() {
        return new RxErrorHandlingCallAdapterFactory();
    }

    private RxErrorHandlingCallAdapterFactory() {
    }

    @Nonnull
    @NonNull
    @Override
    public CallAdapter<?, ?> get(@Nonnull @NonNull @lombok.NonNull final Type type, @Nonnull @NonNull @lombok.NonNull final Annotation[] annotations, @Nonnull @NonNull @lombok.NonNull final Retrofit retrofit) {
        return new RxCallAdapterWrapper(this.factory.get(type, annotations, retrofit));
    }
}
