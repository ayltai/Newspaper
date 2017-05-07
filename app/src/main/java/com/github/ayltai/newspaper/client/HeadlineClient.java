package com.github.ayltai.newspaper.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.client.rss.RssClient;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.net.HttpClient;

import rx.Observable;

public final class HeadlineClient extends RssClient {
    //region Constants

    public static final String URL = "http://hd.stheadline.com/rss/news/daily/";

    public static final String CATEGORY_HONG_KONG     = "?category=hongkong";
    public static final String CATEGORY_INTERNATIONAL = "?category=international";
    public static final String CATEGORY_CHINA         = "?category=chain";
    public static final String CATEGORY_FINANCE       = "?category=finance";
    public static final String CATEGORY_PROPERTY      = "?category=property";
    public static final String CATEGORY_ENTERTAINMENT = "?category=entertainment";
    public static final String CATEGORY_SUPPLEMENT    = "?category=supplement";
    public static final String CATEGORY_SPORTS        = "?category=sports";

    //endregion

    private static final Map<String, String> KEYWORDS = new HashMap<>(8);

    static {
        HeadlineClient.KEYWORDS.put(CATEGORY_HONG_KONG, " (港聞) ");
        HeadlineClient.KEYWORDS.put(CATEGORY_INTERNATIONAL, " (國際) ");
        HeadlineClient.KEYWORDS.put(CATEGORY_CHINA, " (中國) ");
        HeadlineClient.KEYWORDS.put(CATEGORY_FINANCE, " (財經) ");
        HeadlineClient.KEYWORDS.put(CATEGORY_PROPERTY, " (地產) ");
        HeadlineClient.KEYWORDS.put(CATEGORY_ENTERTAINMENT, " (娛樂) ");
        HeadlineClient.KEYWORDS.put(CATEGORY_SUPPLEMENT, " (副刊) ");
        HeadlineClient.KEYWORDS.put(CATEGORY_SPORTS, " (體育) ");
    }

    @Inject
    HeadlineClient(@NonNull final HttpClient client, @Nullable final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    protected List<Item> filters(@NonNull final String url, @NonNull final List<Item> items) {
        final String     keyword       = HeadlineClient.KEYWORDS.get(url.substring(HeadlineClient.URL.length()));
        final List<Item> filteredItems = new ArrayList<>();

        for (final Item item : items) {
            final int index = item.getTitle().indexOf(keyword);

            if (index >= 0) {
                item.setTitle(item.getTitle().substring(0, index));

                filteredItems.add(item);
            }
        }

        return filteredItems;
    }

    @NonNull
    @Override
    public Observable<Item> updateItem(@NonNull final Item item) {
        return null;
    }
}
