package com.github.ayltai.newspaper.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.client.HeadlineClient;

import io.realm.RealmList;

public final class SourceFactory {
    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        }
    };

    private static SourceFactory instance;

    private final Map<String, Source> sources = new HashMap<>(12);

    @NonNull
    public static SourceFactory getInstance(@NonNull final Context context) {
        if (SourceFactory.instance == null) SourceFactory.instance = new SourceFactory(context);

        return SourceFactory.instance;
    }

    private SourceFactory(@NonNull final Context context) {
        final String[] sources    = context.getResources().getStringArray(R.array.sources);
        final String[] categories = context.getResources().getStringArray(R.array.categories);

        int i = 0;

        this.sources.put(sources[i++], SourceFactory.createAppleDailySource(sources, categories));
        this.sources.put(sources[i++], SourceFactory.createOrientalDailySource(sources, categories));
        this.sources.put(sources[i++], SourceFactory.createSingTaoDailySource(sources, categories));
        this.sources.put(sources[i++], SourceFactory.createSingTaoRealtimeSource(sources, categories));
        this.sources.put(sources[i++], SourceFactory.createEconomicTimesSource(sources, categories));
        this.sources.put(sources[i++], SourceFactory.createSingPaoDailySource(sources, categories));
        this.sources.put(sources[i++], SourceFactory.createMingPaoSource(sources, categories));
        this.sources.put(sources[i++], SourceFactory.createHeadlineSource(sources, categories));
        this.sources.put(sources[i++], SourceFactory.createHeadlineRealtimeSource(sources, categories));
        this.sources.put(sources[i++], SourceFactory.createSkyPostSource(sources, categories));
        this.sources.put(sources[i++], SourceFactory.createEconomicJournalSource(sources, categories));
        this.sources.put(sources[i], SourceFactory.createRadioTelevisionSource(sources, categories));
    }

    @Nullable
    public Source getSource(@NonNull final String name) {
        return this.sources.get(name);
    }

    @NonNull
    private static Source createAppleDailySource(@NonNull final String[] sources, @NonNull final String[] categories) {
        final String date = DATE_FORMAT.get().format(new Date());

        return new Source(sources[0], new RealmList<>(
            new Category(String.format("http://hk.apple.nextmedia.com/video/videolist/%s/local/home/0", date), categories[12]),
            new Category(String.format("http://hk.apple.nextmedia.com/video/videolist/%s/chinainternational/home/0", date), categories[13]),
            new Category(String.format("http://hk.apple.nextmedia.com/video/videolist/%s/finance/home/0", date), categories[15]),
            new Category(String.format("http://hk.apple.nextmedia.com/video/videolist/%s/entertainmnt/home/0", date), categories[17]),
            new Category(String.format("http://hk.apple.nextmedia.com/video/videolist/%s/sports/home/0", date), categories[18])
        ));
    }

    @SuppressWarnings("checkstyle:magicnumber")
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

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createSingTaoDailySource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[2], new RealmList<>(
            new Category("http://std.stheadline.com/daily/section-list.php?cat=12", categories[1]),
            new Category("http://std.stheadline.com/daily/section-list.php?cat=13", categories[2]),
            new Category("http://std.stheadline.com/daily/section-list.php?cat=16", categories[3]),
            new Category("http://std.stheadline.com/daily/section-list.php?cat=15", categories[4]),
            new Category("http://std.stheadline.com/daily/section-list.php?cat=20", categories[5]),
            new Category("http://std.stheadline.com/daily/section-list.php?cat=17", categories[6]),
            new Category("http://std.stheadline.com/daily/section-list.php?cat=14", categories[7])));
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createSingTaoRealtimeSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[3], new RealmList<>(
            new Category("http://std.stheadline.com/instant/articles/listview/%E9%A6%99%E6%B8%AF/", categories[12]),
            new Category("http://std.stheadline.com/instant/articles/listview/%E5%9C%8B%E9%9A%9B/", categories[13]),
            new Category("http://std.stheadline.com/instant/articles/listview/%E4%B8%AD%E5%9C%8B/", categories[14]),
            new Category("http://std.stheadline.com/instant/articles/listview/%E7%B6%93%E6%BF%9F/", categories[15]),
            new Category("http://std.stheadline.com/instant/articles/listview/%E5%9C%B0%E7%94%A2/", categories[16]),
            new Category("http://std.stheadline.com/instant/articles/listview/%E5%A8%9B%E6%A8%82/", categories[17]),
            new Category("http://std.stheadline.com/instant/articles/listview/%E9%AB%94%E8%82%B2/", categories[18])));
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createEconomicTimesSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[4], new RealmList<>(
            new Category("http://www.hket.com/rss/headlines", categories[0]),
            new Category("http://www.hket.com/rss/hongkong", categories[1]),
            new Category("http://www.hket.com/rss/world", categories[2]),
            new Category("http://www.hket.com/rss/china", categories[3]),
            new Category("http://www.hket.com/rss/finance", categories[4]),
            new Category("http://www.hket.com/rss/lifestyle", categories[8]),
            new Category("http://www.hket.com/rss/technology", categories[9])));
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createSingPaoDailySource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[5], new RealmList<>(
            new Category("https://www.singpao.com.hk/index.php?fi=news1", categories[1]),
            new Category("https://www.singpao.com.hk/index.php?fi=news8", categories[2]),
            new Category("https://www.singpao.com.hk/index.php?fi=news3", categories[4]),
            new Category("https://www.singpao.com.hk/index.php?fi=news4", categories[6]),
            new Category("https://www.singpao.com.hk/index.php?fi=news5", categories[7]),
            new Category("https://www.singpao.com.hk/index.php?fi=news7", categories[8])));
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createMingPaoSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[6], new RealmList<>(
            new Category("https://news.mingpao.com/rss/pns/s00001.xml", categories[0]),
            new Category("https://news.mingpao.com/rss/pns/s00002.xml", categories[1]),
            new Category("https://news.mingpao.com/rss/pns/s00014.xml", categories[2]),
            new Category("https://news.mingpao.com/rss/pns/s00013.xml", categories[3]),
            new Category("https://news.mingpao.com/rss/pns/s00004.xml", categories[4]),
            new Category("https://news.mingpao.com/rss/pns/s00016.xml", categories[6]),
            new Category("https://news.mingpao.com/rss/pns/s00015.xml", categories[7]),
            new Category("https://news.mingpao.com/rss/pns/s00005.xml", categories[8]),
            new Category("https://news.mingpao.com/rss/pns/s00011.xml", categories[10]),
            new Category("https://news.mingpao.com/rss/ins/s00001.xml", categories[12]),
            new Category("https://news.mingpao.com/rss/ins/s00005.xml", categories[13]),
            new Category("https://news.mingpao.com/rss/ins/s00004.xml", categories[14]),
            new Category("https://news.mingpao.com/rss/ins/s00002.xml", categories[15]),
            new Category("https://news.mingpao.com/rss/ins/s00003.xml", categories[16]),
            new Category("https://news.mingpao.com/rss/ins/s00007.xml", categories[17]),
            new Category("https://news.mingpao.com/rss/ins/s00006.xml", categories[18])));
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createHeadlineSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[7], new RealmList<>(
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_HONG_KONG, categories[1]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_INTERNATIONAL, categories[2]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_CHINA, categories[3]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_FINANCE, categories[4]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_PROPERTY, categories[5]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_ENTERTAINMENT, categories[6]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_SUPPLEMENT, categories[8]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_SPORTS, categories[7])));
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createHeadlineRealtimeSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[8], new RealmList<>(
            new Category("http://hd.stheadline.com/news/realtime/hk/", categories[12]),
            new Category("http://hd.stheadline.com/news/realtime/wo/", categories[13]),
            new Category("http://hd.stheadline.com/news/realtime/chi/", categories[14]),
            new Category("http://hd.stheadline.com/news/realtime/fin/", categories[15]),
            new Category("http://hd.stheadline.com/news/realtime/pp/", categories[16]),
            new Category("http://hd.stheadline.com/news/realtime/ent/", categories[17]),
            new Category("http://hd.stheadline.com/news/realtime/spt/", categories[18])));
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createSkyPostSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[9], new RealmList<>(
            new Category("http://skypost.ulifestyle.com.hk/rss/sras001", categories[1]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras004", categories[2]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras003", categories[4]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras002", categories[6]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras005", categories[7]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras006", categories[8]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras007", categories[10])));
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createEconomicJournalSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[10], new RealmList<>(
            new Category("http://www.hkej.com/rss/onlinenews.xml", categories[15])));
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createRadioTelevisionSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[11], new RealmList<>(
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_clocal.xml", categories[12]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_cinternational.xml", categories[13]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_greaterchina.xml", categories[14]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_cfinance.xml", categories[15]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_csport.xml", categories[18])));
    }
}
