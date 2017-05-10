package com.github.ayltai.newspaper.client;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.model.Image;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.net.HttpClient;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import rx.Emitter;
import rx.Observable;

final class HeadlineRealtimeClient extends Client {
    //region Constants

    private static final String BASE_URI  = "http://hd.stheadline.com";
    private static final String TAG_LINK  = "</a>";
    private static final String TAG_QUOTE = "\"";
    private static final String TAG_CLOSE = "\">";
    private static final String HTTP      = "http:";

    //endregion

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        }
    };

    HeadlineRealtimeClient(@NonNull final HttpClient client, @Nullable final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    public Observable<List<Item>> getItems(@NonNull final String url) {
        return Observable.create(emitter -> {
            try {
                final String html = IOUtils.toString(this.client.download(url), Client.ENCODING);

                if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "URL = " + url);

                final String[]   sections     = StringUtils.substringsBetween(html, "<div class=\"topic\">", "<div class=\"col-xs-12 instantnews-list-1\">");
                final List<Item> items        = new ArrayList<>(sections.length);
                final String     categoryName = this.getCategoryName(url);

                for (final String section : sections) {
                    if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "Item = " + section);

                    final Item   item  = new Item();
                    final String title = StringUtils.substringBetween(section, "<h4>", "</h4>");

                    item.setTitle(StringUtils.substringBetween(title, HeadlineRealtimeClient.TAG_CLOSE, HeadlineRealtimeClient.TAG_LINK));
                    item.setLink(HeadlineRealtimeClient.BASE_URI + StringUtils.substringBetween(title, "<a href=\"", HeadlineRealtimeClient.TAG_CLOSE));
                    item.setDescription(StringUtils.substringBetween(section, "<p class=\"text\">", "</p>"));
                    item.setSource(this.source.getName());
                    item.setCategory(categoryName);

                    if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "Title = " + item.getTitle());
                    if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "Link = " + item.getLink());
                    if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "Description = " + item.getDescription());

                    final String image = StringUtils.substringBetween(section, "<img src=\"", HeadlineRealtimeClient.TAG_QUOTE);
                    if (image != null) item.getImages().add(new Image(HeadlineRealtimeClient.HTTP + image));

                    try {
                        item.setPublishDate(HeadlineRealtimeClient.DATE_FORMAT.get().parse(StringUtils.substringBetween(section, "<i class=\"fa fa-clock-o\"></i>", "</span>")));

                        items.add(item);
                    } catch (final ParseException e) {
                        LogUtils.getInstance().w(this.getClass().getSimpleName(), e.getMessage(), e);
                    }
                }

                emitter.onNext(items);
            } catch (final IOException e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.BUFFER);
    }

    @NonNull
    @Override
    public Observable<Item> updateItem(@NonNull final Item item) {
        return Observable.create(emitter -> {
            try {
                final String html = IOUtils.toString(this.client.download(item.getLink()), Client.ENCODING);

                if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "URL = " + item.getLink());

                HeadlineRealtimeClient.extractImages(StringUtils.substringsBetween(html, "<a class=\"fancybox\" rel=\"gallery\"", HeadlineRealtimeClient.TAG_LINK), item);
                HeadlineRealtimeClient.extractImages(StringUtils.substringsBetween(html, "<figure ", "</figure>"), item);

                item.setDescription(StringUtils.substringBetween(html, "<div id=\"news-content\" class=\"set-font-aera\" style=\"visibility: visible;\">", "</div>"));
                item.setIsFullDescription(true);

                emitter.onNext(item);
            } catch (final IOException e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.BUFFER);
    }

    private static void extractImages(@NonNull final String[] imageContainers, @NonNull final Item item) {
        final List<Image> images = new ArrayList<>();

        for (final String imageContainer : imageContainers) {
            final String imageUrl         = StringUtils.substringBetween(imageContainer, "href=\"", HeadlineRealtimeClient.TAG_QUOTE);
            final String imageDescription = StringUtils.substringBetween(imageContainer, "title=\"", HeadlineRealtimeClient.TAG_QUOTE);

            if (imageUrl != null) images.add(new Image(HeadlineRealtimeClient.HTTP + imageUrl, imageDescription));
        }

        if (!images.isEmpty()) {
            item.getImages().clear();
            item.getImages().addAll(images);
        }
    }
}
