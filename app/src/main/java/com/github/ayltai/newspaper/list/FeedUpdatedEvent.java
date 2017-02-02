package com.github.ayltai.newspaper.list;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.data.Feed;

public final class FeedUpdatedEvent {
    private final Feed feed;

    public FeedUpdatedEvent(@NonNull final Feed feed) {
        this.feed = feed;
    }

    public Feed getFeed() {
        return this.feed;
    }
}
