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
        return new Source(sources[1], new RealmList<>(
            new Category("http://orientaldaily.on.cc/rss/news.xml", categories[1]),
            new Category("http://orientaldaily.on.cc/rss/china_world.xml", categories[2]),
            new Category("http://orientaldaily.on.cc/rss/finance.xml", categories[4]),
            new Category("http://orientaldaily.on.cc/rss/entertainment.xml", categories[6]),
            new Category("http://orientaldaily.on.cc/rss/lifestyle.xml", categories[8]),
            new Category("http://orientaldaily.on.cc/rss/sport.xml", categories[10])));
    }

    @NonNull
    private Source createSingTaoDailySource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[2], new RealmList<>());
    }

    @NonNull
    private Source createEconomicTimesSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[3], new RealmList<>(
            new Category("http://www.hket.com/rss/headlines", categories[0]),
            new Category("http://www.hket.com/rss/hongkong", categories[1]),
            new Category("http://www.hket.com/rss/world", categories[2]),
            new Category("http://www.hket.com/rss/china", categories[3]),
            new Category("http://www.hket.com/rss/finance", categories[4]),
            new Category("http://www.hket.com/rss/lifestyle", categories[8]),
            new Category("http://www.hket.com/rss/technology", categories[9])));
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
            new Category("https://news.mingpao.com/rss/ins/s00006.xml", categories[18])));
    }

    @NonNull
    private Source createHeadlineSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[6], new RealmList<>(
            new Category("http://www.feed43.com/5652083487268223.xml", categories[12]),
            new Category("http://www.feed43.com/8334601303067526.xml", categories[13]),
            new Category("http://www.feed43.com/1650212611678040.xml", categories[14]),
            new Category("http://www.feed43.com/6861325447714757.xml", categories[16]),
            new Category("http://www.feed43.com/1074704207872608.xml", categories[17]),
            new Category("http://www.feed43.com/6835046601035184.xml", categories[18])));
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
            new Category("http://skypost.ulifestyle.com.hk/rss/sras007", categories[10])));
    }

    @NonNull
    private Source createEconomicJournalSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[8], new RealmList<>(
            new Category("http://www.feed43.com/8115574211567336.xml", categories[1]),
            new Category("http://www.feed43.com/4411814127482753.xml", categories[2]),
            new Category("http://www.feed43.com/7220621531762083.xml", categories[3]),
            new Category("http://www.feed43.com/0875012577044427.xml", categories[4]),
            new Category("http://www.feed43.com/2720672460258087.xml", categories[5]),
            new Category("http://www.feed43.com/1632345172705331.xml", categories[8]),
            new Category("http://www.feed43.com/2135755628716870.xml", categories[11])));
    }

    @NonNull
    private Source createRadioTelevisionSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[9], new RealmList<>(
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_clocal.xml", categories[1]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_cinternational.xml", categories[2]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_greaterchina.xml", categories[3]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_cfinance.xml", categories[4]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_csport.xml", categories[7]),
            new Category("http://feeds.feedburner.com/rthk/irFT", categories[12]),
            new Category("http://feeds.feedburner.com/rthk/Lpzl", categories[13]),
            new Category("http://feeds.feedburner.com/rthk/Tumf", categories[14]),
            new Category("http://feeds.feedburner.com/rthk/ksHf", categories[15]),
            new Category("http://feeds.feedburner.com/rthk/bkob", categories[18])));
    }
}
