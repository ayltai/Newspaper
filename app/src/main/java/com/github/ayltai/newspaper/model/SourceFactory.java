package com.github.ayltai.newspaper.model;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.R;

import io.realm.RealmList;

public final class SourceFactory {
    private static SourceFactory instance;

    private final Map<String, Source> sources = new HashMap<>(10);

    @NonNull
    public static SourceFactory getInstance(@NonNull final Context context) {
        if (SourceFactory.instance == null) SourceFactory.instance = new SourceFactory(context);

        return SourceFactory.instance;
    }

    private SourceFactory(@NonNull final Context context) {
        final String[] sources    = context.getResources().getStringArray(R.array.sources);
        final String[] categories = context.getResources().getStringArray(R.array.categories);

        this.sources.put(sources[0], this.createAppleDailySource(sources, categories));
        this.sources.put(sources[1], this.createOrientalDailySource(sources, categories));
        this.sources.put(sources[2], this.createSingTaoDailySource(sources, categories));
        this.sources.put(sources[3], this.createEconomicTimesSource(sources, categories));
        this.sources.put(sources[4], this.createSingPaoDailySource(sources, categories));
        this.sources.put(sources[5], this.createMingPaoSource(sources, categories));
        this.sources.put(sources[6], this.createHeadlineSource(sources, categories));
        this.sources.put(sources[7], this.createSkyPostSource(sources, categories));
        this.sources.put(sources[8], this.createEconomicJournalSource(sources, categories));
        this.sources.put(sources[9], this.createRadioTelevisionSource(sources, categories));
    }

    @Nullable
    public Source getSource(@NonNull final String name) {
        return this.sources.get(name);
    }

    @NonNull
    private Source createAppleDailySource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[0], new RealmList<>());
    }

    @NonNull
    private Source createOrientalDailySource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[1], new RealmList<>());
    }

    @NonNull
    private Source createSingTaoDailySource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[2], new RealmList<>());
    }

    @NonNull
    private Source createEconomicTimesSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[3], new RealmList<>());
    }

    @NonNull
    private Source createSingPaoDailySource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[4], new RealmList<>());
    }

    @NonNull
    private Source createMingPaoSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[5], new RealmList<>(
            new Category("https://news.mingpao.com/rss/pns/s00001.xml", categories[0]),
            new Category("https://news.mingpao.com/rss/pns/s00002.xml", categories[1]),
            new Category("https://news.mingpao.com/rss/pns/s00014.xml", categories[2]),
            new Category("https://news.mingpao.com/rss/pns/s00013.xml", categories[3]),
            new Category("https://news.mingpao.com/rss/pns/s00004.xml", categories[4]),
            new Category("https://news.mingpao.com/rss/pns/s00016.xml", categories[6]),
            new Category("https://news.mingpao.com/rss/pns/s00015.xml", categories[7]),
            new Category("https://news.mingpao.com/rss/pns/s00005.xml", categories[8]),
            new Category("https://news.mingpao.com/rss/pns/s00011.xml", categories[10]),
            new Category("https://news.mingpao.com/rss/pns/s00003.xml", categories[11]),
            new Category("https://news.mingpao.com/rss/ins/s00001.xml", categories[12]),
            new Category("https://news.mingpao.com/rss/ins/s00005.xml", categories[13]),
            new Category("https://news.mingpao.com/rss/ins/s00004.xml", categories[14]),
            new Category("https://news.mingpao.com/rss/ins/s00002.xml", categories[15]),
            new Category("https://news.mingpao.com/rss/ins/s00003.xml", categories[16]),
            new Category("https://news.mingpao.com/rss/ins/s00007.xml", categories[17]),
            new Category("https://news.mingpao.com/rss/ins/s00006.xml", categories[18])
        ));
    }

    @NonNull
    private Source createHeadlineSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[6], new RealmList<>());
    }

    @NonNull
    private Source createSkyPostSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[7], new RealmList<>(
            new Category("http://skypost.ulifestyle.com.hk/rss/sras001", categories[1]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras004", categories[2]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras003", categories[4]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras002", categories[6]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras005", categories[7]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras006", categories[8]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras007", categories[10])
        ));
    }

    @NonNull
    private Source createEconomicJournalSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[8], new RealmList<>());
    }

    @NonNull
    private Source createRadioTelevisionSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[9], new RealmList<>(
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_clocal.xml", categories[1]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_cinternational.xml", categories[2]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_greaterchina.xml", categories[3]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_cfinance.xml", categories[4]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_csport.xml", categories[7])));
    }
}
