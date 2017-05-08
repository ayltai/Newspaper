package com.github.ayltai.newspaper.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.client.rss.RssClient;
import com.github.ayltai.newspaper.model.Image;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.net.HttpClient;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import rx.Emitter;
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
        return Observable.create(emitter -> {
            try {
                final String html = IOUtils.toString(this.client.download(item.getLink()), Client.ENCODING);

                if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "URL = " + item.getLink());

                final String[]    imageContainers = StringUtils.substringsBetween(html, "<a class=\"fancybox\" rel=\"gallery\"", "</a>");
                final List<Image> images          = new ArrayList<>();

                for (final String imageContainer : imageContainers) {
                    final String imageUrl         = StringUtils.substringBetween(imageContainer, "href=\"", "\"");
                    final String imageDescription = StringUtils.substringBetween(imageContainer, "title=\"■", "\">");

                    if (imageUrl != null) images.add(new Image("http:" + imageUrl, imageDescription));
                }

                if (!images.isEmpty()) {
                    item.getImages().clear();
                    item.getImages().addAll(images);
                }

                final String[]      contents = StringUtils.substringsBetween(html, "<div id=\"news-content\" class=\"set-font-aera\" style=\"visibility: visible;\">", "</div>");
                final StringBuilder builder  = new StringBuilder();

                for (final String content : contents) builder.append(content);

                item.setDescription(builder.toString());
                item.setIsFullDescription(true);

                emitter.onNext(item);
            } catch (final IOException e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.BUFFER);
    }
}
