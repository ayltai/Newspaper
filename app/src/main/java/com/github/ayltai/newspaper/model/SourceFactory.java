package com.github.ayltai.newspaper.model;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.client.HeadlineClient;

import io.realm.RealmList;

public final class SourceFactory {
    private static SourceFactory instance;

    private final Map<String, Source> sources = new HashMap<>(11);

    @NonNull
    public static SourceFactory getInstance(@NonNull final Context context) {
        if (SourceFactory.instance == null) SourceFactory.instance = new SourceFactory(context);

        return SourceFactory.instance;
    }

    private SourceFactory(@NonNull final Context context) {
        final String[] sources    = context.getResources().getStringArray(R.array.sources);
        final String[] categories = context.getResources().getStringArray(R.array.categories);

        this.sources.put(sources[0], SourceFactory.createAppleDailySource(sources, categories));
        this.sources.put(sources[1], SourceFactory.createOrientalDailySource(sources, categories));
        this.sources.put(sources[2], SourceFactory.createSingTaoDailySource(sources, categories));
        this.sources.put(sources[3], SourceFactory.createEconomicTimesSource(sources, categories));
        this.sources.put(sources[4], SourceFactory.createSingPaoDailySource(sources, categories));
        this.sources.put(sources[5], SourceFactory.createMingPaoSource(sources, categories));
        this.sources.put(sources[6], SourceFactory.createHeadlineSource(sources, categories));
        this.sources.put(sources[7], SourceFactory.createHeadlineRealtimeSource(sources, categories));
        this.sources.put(sources[8], SourceFactory.createSkyPostSource(sources, categories));
        this.sources.put(sources[9], SourceFactory.createEconomicJournalSource(sources, categories));
        this.sources.put(sources[10], SourceFactory.createRadioTelevisionSource(sources, categories));
    }

    @Nullable
    public Source getSource(@NonNull final String name) {
        return this.sources.get(name);
    }

    @NonNull
    private static Source createAppleDailySource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[0], new RealmList<>());
    }

    @NonNull
    private static Source createOrientalDailySource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[1], new RealmList<>(
            new Category("http://orientaldaily.on.cc/rss/news.xml", categories[1]),
            new Category("http://orientaldaily.on.cc/rss/china_world.xml", categories[2]),
            new Category("http://orientaldaily.on.cc/rss/finance.xml", categories[4]),
            new Category("http://orientaldaily.on.cc/rss/entertainment.xml", categories[6]),
            new Category("http://orientaldaily.on.cc/rss/lifestyle.xml", categories[8]),
            new Category("http://orientaldaily.on.cc/rss/sport.xml", categories[7])));
    }

    @NonNull
    private static Source createSingTaoDailySource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[2], new RealmList<>());
    }

    @NonNull
    private static Source createEconomicTimesSource(@NonNull final String[] sources, @NonNull final String[] categories) {
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
    private static Source createSingPaoDailySource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[4], new RealmList<>(
            new Category("https://www.singpao.com.hk/index.php?fi=news1", categories[1]),
            new Category("https://www.singpao.com.hk/index.php?fi=news8", categories[2]),
            new Category("https://www.singpao.com.hk/index.php?fi=news3", categories[4]),
            new Category("https://www.singpao.com.hk/index.php?fi=news4", categories[6]),
            new Category("https://www.singpao.com.hk/index.php?fi=news5", categories[7]),
            new Category("https://www.singpao.com.hk/index.php?fi=news7", categories[8])));
    }

    @NonNull
    private static Source createMingPaoSource(@NonNull final String[] sources, @NonNull final String[] categories) {
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
    private static Source createHeadlineSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[6], new RealmList<>(
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_HONG_KONG, categories[1]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_INTERNATIONAL, categories[2]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_CHINA, categories[3]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_FINANCE, categories[4]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_PROPERTY, categories[5]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_ENTERTAINMENT, categories[6]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_SUPPLEMENT, categories[8]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_SPORTS, categories[7])));
    }

    @NonNull
    private static Source createHeadlineRealtimeSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[7], new RealmList<>(
            new Category("http://hd.stheadline.com/news/realtime/hk/", categories[12]),
            new Category("http://hd.stheadline.com/news/realtime/wo/", categories[13]),
            new Category("http://hd.stheadline.com/news/realtime/chi/", categories[14]),
            new Category("http://hd.stheadline.com/news/realtime/fin/", categories[15]),
            new Category("http://hd.stheadline.com/news/realtime/pp/", categories[16]),
            new Category("http://hd.stheadline.com/news/realtime/ent/", categories[17]),
            new Category("http://hd.stheadline.com/news/realtime/spt/", categories[18])));
    }

    @NonNull
    private static Source createSkyPostSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[8], new RealmList<>(
            new Category("http://skypost.ulifestyle.com.hk/rss/sras001", categories[1]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras004", categories[2]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras003", categories[4]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras002", categories[6]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras005", categories[7]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras006", categories[8]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras007", categories[10])));
    }

    @NonNull
    private static Source createEconomicJournalSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[9], new RealmList<>(
            new Category("http://www1.hkej.com/dailynews/international", categories[2]),
            new Category("http://www1.hkej.com/dailynews/cntw", categories[3]),
            new Category("http://www1.hkej.com/dailynews/finnews", categories[4]),
            new Category("http://www1.hkej.com/dailynews/property", categories[5]),
            new Category("http://www1.hkej.com/dailynews/culture", categories[8])));
    }

    @NonNull
    private static Source createRadioTelevisionSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[10], new RealmList<>(
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_clocal.xml", categories[12]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_cinternational.xml", categories[13]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_greaterchina.xml", categories[14]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_cfinance.xml", categories[15]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_csport.xml", categories[18])));
    }
}
