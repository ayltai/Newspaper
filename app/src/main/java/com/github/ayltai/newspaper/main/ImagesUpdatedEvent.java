package com.github.ayltai.newspaper.main;

import java.util.List;

import android.support.annotation.NonNull;

public final class ImagesUpdatedEvent {
    private final String       url;
    private final List<String> images;

    public ImagesUpdatedEvent(@NonNull final String url, @NonNull final List<String> images) {
        this.url    = url;
        this.images = images;
    }

    @NonNull
    public String getUrl() {
        return this.url;
    }

    @NonNull
    public List<String> getImages() {
        return this.images;
    }
}
