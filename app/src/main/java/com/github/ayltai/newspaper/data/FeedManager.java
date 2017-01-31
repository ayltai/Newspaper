package com.github.ayltai.newspaper.data;

import android.support.annotation.NonNull;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;

public class FeedManager {
    private final Realm realm;

    public FeedManager(@NonNull final Realm realm) {
        this.realm = realm;
    }

    @NonNull
    public Observable<Feed> getFeed(@NonNull final String url) {
        if (this.realm.isClosed()) throw new IllegalStateException("Realm is closed");

        return Observable.create(subscriber -> {
            final RealmResults<Feed> feeds = this.realm.where(Feed.class).equalTo(Feed.FIELD_URL, url).findAll();

            if (feeds.isEmpty()) {
                subscriber.onNext(null);
            } else {
                subscriber.onNext(feeds.first());
            }
        });
    }
}
