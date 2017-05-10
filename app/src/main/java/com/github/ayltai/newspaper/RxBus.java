package com.github.ayltai.newspaper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.Pair;

import org.reactivestreams.Subscriber;

import io.reactivex.disposables.Disposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public class RxBus {
    private static ThreadLocal<RxBus> INSTANCE = new ThreadLocal<>();

    private final Map<Pair<Class, Subscriber>, Disposable> disposables = new HashMap<>();
    private final FlowableProcessor<Object>                bus         = PublishProcessor.create().toSerialized();

    public static RxBus getInstance() {
        final RxBus instance = RxBus.INSTANCE.get();

        if (instance == null) {
            RxBus.INSTANCE.set(new RxBus());

            return RxBus.INSTANCE.get();
        }

        return instance;
    }

    @VisibleForTesting
    RxBus() {
    }

    public <T> void register(@NonNull final Class<T> eventType, @NonNull final Subscriber<T> subscriber) {
        final Pair<Class, Subscriber> key = Pair.create(eventType, subscriber);

        if (this.disposables.containsKey(key)) throw new IllegalArgumentException("The given subscriber is already registered");

        this.disposables.put(key, this.bus.filter(event -> event != null && event.getClass().equals(eventType)).subscribe(value -> subscriber.onNext((T)value)));
    }

    public <T> void unregister(@NonNull final Class<T> eventType, @NonNull final Subscriber<T> subscriber) {
        final Pair<Class, Subscriber> key = Pair.create(eventType, subscriber);

        if (this.disposables.containsKey(key)) this.disposables.remove(key).dispose();
    }

    public void unregisterAll() {
        for (final Pair<Class, Subscriber> pair : new HashSet<>(this.disposables.keySet())) {
            this.unregister(pair.first, pair.second);
        }
    }

    public <T> void send(@NonNull final T event) {
        if (!this.disposables.isEmpty()) this.bus.onNext(event);
    }
}
