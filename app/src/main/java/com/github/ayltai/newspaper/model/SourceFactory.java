package com.github.ayltai.newspaper.model;

import java.util.HashMap;
import java.util.Map;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.realm.RealmList;

public final class SourceFactory {
    //region Constants

    private static final String APPLE_DAILY      = "蘋果日報";
    private static final String ORIENTAL_DAILY   = "東方日報";
    private static final String SING_TAO_DAILY   = "星島日報";
    private static final String ECONOMIC_TIMES   = "經濟日報";
    private static final String SING_PAO_DAILY   = "成報";
    private static final String MING_PAO         = "明報";
    private static final String HEADLINE         = "頭條日報";
    private static final String SKY_POST         = "晴報";
    private static final String ECONOMIC_JOURNAL = "信報";
    private static final String RADIO_TELEVISION = "香港電台";

    //endregion

    private static final Map<String, Source> SOURCES = new HashMap<>(10);

    static {
        SourceFactory.SOURCES.put(SourceFactory.APPLE_DAILY,      SourceFactory.createAppleDailySource());
        SourceFactory.SOURCES.put(SourceFactory.ORIENTAL_DAILY,   SourceFactory.createOrientalDailySource());
        SourceFactory.SOURCES.put(SourceFactory.SING_TAO_DAILY,   SourceFactory.createSingTaoDailySource());
        SourceFactory.SOURCES.put(SourceFactory.ECONOMIC_TIMES,   SourceFactory.createEconomicTimesSource());
        SourceFactory.SOURCES.put(SourceFactory.SING_PAO_DAILY,   SourceFactory.createSingPaoDailySource());
        SourceFactory.SOURCES.put(SourceFactory.MING_PAO,         SourceFactory.createMingPaoSource());
        SourceFactory.SOURCES.put(SourceFactory.HEADLINE,         SourceFactory.createHeadlineSource());
        SourceFactory.SOURCES.put(SourceFactory.SKY_POST,         SourceFactory.createSkyPostSource());
        SourceFactory.SOURCES.put(SourceFactory.ECONOMIC_JOURNAL, SourceFactory.createEconomicJournalSource());
        SourceFactory.SOURCES.put(SourceFactory.RADIO_TELEVISION, SourceFactory.createRadioTelevisionSource());
    }

    @Nullable
    public static Source create(@NonNull final String name) {
        return SourceFactory.SOURCES.get(name);
    }

    @NonNull
    private static Source createAppleDailySource() {
        final RealmList<Category> categories = new RealmList<>();

        return new Source(SourceFactory.APPLE_DAILY, categories);
    }

    @NonNull
    private static Source createOrientalDailySource() {
        final RealmList<Category> categories = new RealmList<>();

        return new Source(SourceFactory.ORIENTAL_DAILY, categories);
    }

    @NonNull
    private static Source createSingTaoDailySource() {
        final RealmList<Category> categories = new RealmList<>();

        return new Source(SourceFactory.SING_TAO_DAILY, categories);
    }

    @NonNull
    private static Source createEconomicTimesSource() {
        final RealmList<Category> categories = new RealmList<>();

        return new Source(SourceFactory.ECONOMIC_TIMES, categories);
    }

    @NonNull
    private static Source createSingPaoDailySource() {
        final RealmList<Category> categories = new RealmList<>();

        return new Source(SourceFactory.SING_PAO_DAILY, categories);
    }

    @NonNull
    private static Source createMingPaoSource() {
        final RealmList<Category> categories = new RealmList<>();

        return new Source(SourceFactory.MING_PAO, categories);
    }

    @NonNull
    private static Source createHeadlineSource() {
        final RealmList<Category> categories = new RealmList<>();

        return new Source(SourceFactory.HEADLINE, categories);
    }

    @NonNull
    private static Source createSkyPostSource() {
        final RealmList<Category> categories = new RealmList<>();

        return new Source(SourceFactory.SKY_POST, categories);
    }

    @NonNull
    private static Source createEconomicJournalSource() {
        final RealmList<Category> categories = new RealmList<>();

        return new Source(SourceFactory.ECONOMIC_JOURNAL, categories);
    }

    @NonNull
    private static Source createRadioTelevisionSource() {
        final RealmList<Category> categories = new RealmList<>();

        return new Source(SourceFactory.RADIO_TELEVISION, categories);
    }
}
