package com.github.ayltai.newspaper.analytics;

import android.support.annotation.NonNull;

public final class SearchEvent extends Event {
    public static final String NAME = "Search";

    public static final String ATTRIBUTE_QUERY       = "Query";
    public static final String ATTRIBUTE_SCREEN_NAME = "Screen Name";
    public static final String ATTRIBUTE_CATEGORY    = "Category";

    public SearchEvent() {
        super(SearchEvent.NAME);
    }

    @NonNull
    public SearchEvent setQuery(@NonNull final String query) {
        this.attributes.add(new Attribute(SearchEvent.ATTRIBUTE_QUERY, query));
        return this;
    }

    @NonNull
    public SearchEvent setScreenName(@NonNull final String screenName) {
        this.attributes.add(new Attribute(SearchEvent.ATTRIBUTE_SCREEN_NAME, screenName));
        return this;
    }

    @NonNull
    public SearchEvent setCategory(@NonNull final String category) {
        this.attributes.add(new Attribute(SearchEvent.ATTRIBUTE_CATEGORY, category));
        return this;
    }
}
