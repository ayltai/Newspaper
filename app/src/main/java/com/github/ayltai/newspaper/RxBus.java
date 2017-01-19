package com.github.ayltai.newspaper;

import java.util.HashMap;
import java.util.Map;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.Pair;

import rx.Subscriber;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class RxBus {
    private static final RxBus INSTANCE = new RxBus();

    private final Map<Pair<Class<?>, Subscriber<?>>, Subscription> subscriptions = new HashMap<>();
    private final Subject<Object, ?>                               bus           = new SerializedSubject<>(PublishSubject.create());

    public static RxBus getInstance() {
        return RxBus.INSTANCE;
    }

    @VisibleForTesting
    public RxBus() {
    }

    public <T> void register(@NonNull final Class<? extends T> eventType, @NonNull final Subscriber<T> subscriber) {
        final Pair<Class<?>, Subscriber<?>> key = Pair.create(eventType, subscriber);

        if (this.subscriptions.containsKey(key)) throw new IllegalArgumentException("The given subscriber is already registered");

        this.subscriptions.put(key, this.bus.filter(event -> event != null && event.getClass().equals(eventType)).subscribe(o -> subscriber.onNext((T)o)));
    }

    public <T> void unregister(@NonNull final Class<? extends T> eventType, @NonNull final Subscriber<T> subscriber) {
        final Pair<Class<?>, Subscriber<?>> key = Pair.create(eventType, subscriber);

        if (!this.subscriptions.containsKey(key)) throw new IllegalArgumentException("The given subscriber is not registered");

        this.subscriptions.remove(key).unsubscribe();
    }

    public <T> void send(@NonNull final T event) {
        if (!this.subscriptions.isEmpty()) this.bus.onNext(event);
    }
}
