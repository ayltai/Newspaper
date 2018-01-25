package com.github.ayltai.newspaper.app.data.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.client.HeadlineClient;

import gnu.trove.map.hash.THashMap;
import io.realm.RealmList;

public final class SourceFactory {
    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        }
    };

    private static SourceFactory instance;

    private final Map<String, Source> sources = new THashMap<>(15);

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
        this.sources.put(sources[i++], SourceFactory.createRadioTelevisionSource(sources, categories));
        this.sources.put(sources[i++], SourceFactory.createSouthChinaMorningPostSource(sources, categories));
        this.sources.put(sources[i++], SourceFactory.createTheStandardSource(sources, categories));
        this.sources.put(sources[i],   SourceFactory.createWenWeiPoSource(sources, categories));
    }

    @NonNull
    public Source getSource(@NonNull final String name) {
        final Source source = this.sources.get(name);

        if (source == null) throw new IllegalArgumentException("Unrecognized source name: " + name);

        return source;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createAppleDailySource(@NonNull final String[] sources, @NonNull final String[] categories) {
        final String date = SourceFactory.DATE_FORMAT.get().format(new Date());

        return new Source(sources[0], new RealmList<>(
            new Category(String.format("https://hk.appledaily.com/video/videolist/%s/local/home/0", date), categories[9]),
            new Category(String.format("https://hk.appledaily.com/video/videolist/%s/international/home/0", date), categories[10]),
            new Category(String.format("https://hk.appledaily.com/video/videolist/%s/finance/home/0", date), categories[12]),
            new Category(String.format("https://hk.appledaily.com/video/videolist/%s/entertainment/home/0", date), categories[14]),
            new Category(String.format("https://hk.appledaily.com/video/videolist/%s/sports/home/0", date), categories[15])), R.drawable.avatar_apple_daily);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createOrientalDailySource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[1], new RealmList<>(
            new Category("http://orientaldaily.on.cc/rss/news.xml", categories[0]),
            new Category("http://orientaldaily.on.cc/rss/china_world.xml", categories[1]),
            new Category("http://orientaldaily.on.cc/rss/finance.xml", categories[3]),
            new Category("http://orientaldaily.on.cc/rss/entertainment.xml", categories[5]),
            new Category("http://orientaldaily.on.cc/rss/lifestyle.xml", categories[7]),
            new Category("http://orientaldaily.on.cc/rss/sport.xml", categories[6])), R.drawable.avatar_oriental_daily);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createSingTaoDailySource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[2], new RealmList<>(
            new Category("http://std.stheadline.com/daily/section-list.php?cat=12", categories[0]),
            new Category("http://std.stheadline.com/daily/section-list.php?cat=13", categories[1]),
            new Category("http://std.stheadline.com/daily/section-list.php?cat=16", categories[2]),
            new Category("http://std.stheadline.com/daily/section-list.php?cat=15", categories[3]),
            new Category("http://std.stheadline.com/daily/section-list.php?cat=20", categories[4]),
            new Category("http://std.stheadline.com/daily/section-list.php?cat=17", categories[5]),
            new Category("http://std.stheadline.com/daily/section-list.php?cat=14", categories[6])), R.drawable.avatar_singtao);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createSingTaoRealtimeSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[3], new RealmList<>(
            new Category("http://std.stheadline.com/instant/articles/listview/%E9%A6%99%E6%B8%AF/", categories[9]),
            new Category("http://std.stheadline.com/instant/articles/listview/%E5%9C%8B%E9%9A%9B/", categories[10]),
            new Category("http://std.stheadline.com/instant/articles/listview/%E4%B8%AD%E5%9C%8B/", categories[11]),
            new Category("http://std.stheadline.com/instant/articles/listview/%E7%B6%93%E6%BF%9F/", categories[12]),
            new Category("http://std.stheadline.com/instant/articles/listview/%E5%9C%B0%E7%94%A2/", categories[13]),
            new Category("http://std.stheadline.com/instant/articles/listview/%E5%A8%9B%E6%A8%82/", categories[14]),
            new Category("http://std.stheadline.com/instant/articles/listview/%E9%AB%94%E8%82%B2/", categories[15])), R.drawable.avatar_singtao);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createEconomicTimesSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[4], new RealmList<>(
            new Category("http://www.hket.com/rss/hongkong", categories[0]),
            new Category("http://www.hket.com/rss/world", categories[1]),
            new Category("http://www.hket.com/rss/china", categories[2]),
            new Category("http://www.hket.com/rss/finance", categories[3]),
            new Category("http://www.hket.com/rss/lifestyle", categories[7]),
            new Category("http://www.hket.com/rss/technology", categories[16])), R.drawable.avatar_hket);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createSingPaoDailySource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[5], new RealmList<>(
            new Category("https://www.singpao.com.hk/index.php?fi=news1", categories[0]),
            new Category("https://www.singpao.com.hk/index.php?fi=news8", categories[1]),
            new Category("https://www.singpao.com.hk/index.php?fi=news3", categories[3]),
            new Category("https://www.singpao.com.hk/index.php?fi=news4", categories[5]),
            new Category("https://www.singpao.com.hk/index.php?fi=news5", categories[6]),
            new Category("https://www.singpao.com.hk/index.php?fi=news7", categories[7])), R.drawable.avatar_singpao);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createMingPaoSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[6], new RealmList<>(
            new Category("https://news.mingpao.com/rss/pns/s00002.xml", categories[0]),
            new Category("https://news.mingpao.com/rss/pns/s00014.xml", categories[1]),
            new Category("https://news.mingpao.com/rss/pns/s00013.xml", categories[2]),
            new Category("https://news.mingpao.com/rss/pns/s00004.xml", categories[3]),
            new Category("https://news.mingpao.com/rss/pns/s00016.xml", categories[5]),
            new Category("https://news.mingpao.com/rss/pns/s00015.xml", categories[6]),
            new Category("https://news.mingpao.com/rss/pns/s00005.xml", categories[7]),
            new Category("https://news.mingpao.com/rss/pns/s00011.xml", categories[8]),
            new Category("https://news.mingpao.com/rss/ins/s00001.xml", categories[9]),
            new Category("https://news.mingpao.com/rss/ins/s00005.xml", categories[10]),
            new Category("https://news.mingpao.com/rss/ins/s00004.xml", categories[11]),
            new Category("https://news.mingpao.com/rss/ins/s00002.xml", categories[12]),
            new Category("https://news.mingpao.com/rss/ins/s00003.xml", categories[13]),
            new Category("https://news.mingpao.com/rss/ins/s00007.xml", categories[14]),
            new Category("https://news.mingpao.com/rss/ins/s00006.xml", categories[15])), R.drawable.avatar_mingpao);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createHeadlineSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[7], new RealmList<>(
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_HONG_KONG, categories[0]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_INTERNATIONAL, categories[1]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_CHINA, categories[2]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_FINANCE, categories[3]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_PROPERTY, categories[4]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_ENTERTAINMENT, categories[5]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_SUPPLEMENT, categories[7]),
            new Category(HeadlineClient.URL + HeadlineClient.CATEGORY_SPORTS, categories[8])), R.drawable.avatar_headline);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createHeadlineRealtimeSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[8], new RealmList<>(
            new Category("http://hd.stheadline.com/news/realtime/hk/", categories[9]),
            new Category("http://hd.stheadline.com/news/realtime/wo/", categories[10]),
            new Category("http://hd.stheadline.com/news/realtime/chi/", categories[11]),
            new Category("http://hd.stheadline.com/news/realtime/fin/", categories[12]),
            new Category("http://hd.stheadline.com/news/realtime/pp/", categories[13]),
            new Category("http://hd.stheadline.com/news/realtime/ent/", categories[14]),
            new Category("http://hd.stheadline.com/news/realtime/spt/", categories[15])), R.drawable.avatar_headline);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createSkyPostSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[9], new RealmList<>(
            new Category("http://skypost.ulifestyle.com.hk/rss/sras001", categories[0]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras004", categories[1]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras003", categories[3]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras002", categories[5]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras005", categories[6]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras006", categories[7]),
            new Category("http://skypost.ulifestyle.com.hk/rss/sras007", categories[8])), R.drawable.avatar_skypost);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createEconomicJournalSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[10], new RealmList<>(new Category("http://www.hkej.com/rss/onlinenews.xml", categories[12])), R.drawable.avatar_hkej);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createRadioTelevisionSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[11], new RealmList<>(
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_clocal.xml", categories[9]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_cinternational.xml", categories[10]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_greaterchina.xml", categories[11]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_cfinance.xml", categories[12]),
            new Category("http://rthk.hk/rthk/news/rss/c_expressnews_csport.xml", categories[15])), R.drawable.avatar_rthk);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createSouthChinaMorningPostSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[12], new RealmList<>(
            new Category("http://www.scmp.com/rss/2/feed", categories[9]),
            new Category("http://www.scmp.com/rss/5/feed", categories[10]),
            new Category("http://www.scmp.com/rss/4/feed", categories[11]),
            new Category("http://www.scmp.com/rss/92/feed", categories[12]),
            new Category("http://www.scmp.com/rss/96/feed", categories[13]),
            new Category("http://www.scmp.com/rss/95/feed", categories[15]),
            new Category("http://www.scmp.com/rss/94/feed", categories[16])

        ), R.drawable.avatar_scmp);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createTheStandardSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[13], new RealmList<>(
            new Category("http://www.thestandard.com.hk/ajax_sections_list.php?sid=4", categories[9]),
            new Category("http://www.thestandard.com.hk/ajax_sections_list.php?sid=6", categories[10]),
            new Category("http://www.thestandard.com.hk/ajax_sections_list.php?sid=3", categories[11]),
            new Category("http://www.thestandard.com.hk/ajax_sections_list.php?sid=2", categories[12]),
            new Category("http://www.thestandard.com.hk/ajax_sections_list.php?sid=8", categories[15])
        ), R.drawable.avatar_the_standard);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @NonNull
    private static Source createWenWeiPoSource(@NonNull final String[] sources, @NonNull final String[] categories) {
        return new Source(sources[14], new RealmList<>(
            new Category("http://news.wenweipo.com/list_news.php?cat=000IN&instantCat=hk", categories[9]),
            new Category("http://news.wenweipo.com/list_news.php?cat=000IN&instantCat=china", categories[10]),
            new Category("http://news.wenweipo.com/list_news.php?cat=000IN&instantCat=world", categories[11])
        ), R.drawable.avatar_wen_wei_po);
    }
}
