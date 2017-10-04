package com.github.ayltai.newspaper.analytics;

public final class SearchEvent extends Event {
    public static final String NAME = "Search";

    public static final String ATTRIBUTE_QUERY       = "Query";
    public static final String ATTRIBUTE_SCREEN_NAME = "Screen Name";

    public SearchEvent() {
        super(SearchEvent.NAME);
    }
}
