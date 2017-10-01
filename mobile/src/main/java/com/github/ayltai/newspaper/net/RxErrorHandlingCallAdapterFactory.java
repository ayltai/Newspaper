package com.github.ayltai.newspaper.net;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import android.support.annotation.NonNull;

import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public final class RxErrorHandlingCallAdapterFactory extends CallAdapter.Factory {
    private final RxJava2CallAdapterFactory factory = RxJava2CallAdapterFactory.create();

    public static CallAdapter.Factory create() {
        return new RxErrorHandlingCallAdapterFactory();
    }

    private RxErrorHandlingCallAdapterFactory() {
    }

    @NonNull
    @Override
    public CallAdapter<?, ?> get(@NonNull final Type type, @NonNull final Annotation[] annotations, @NonNull final Retrofit retrofit) {
        return new RxCallAdapterWrapper(this.factory.get(type, annotations, retrofit));
    }
}
