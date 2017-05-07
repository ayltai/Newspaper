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

final class SingTaoRealtimeClient extends Client {
    //region Constants

    private static final String TAG_CLOSE = "</div>";
    private static final String TAG_QUOTE = "\"";

    //endregion

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        }
    };

    SingTaoRealtimeClient(@NonNull final HttpClient client, @Nullable final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    public Observable<List<Item>> getItems(@NonNull final String url) {
        return Observable.create(emitter -> {
            try {
                final String html = IOUtils.toString(this.client.download(url), Client.ENCODING);

                if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "URL = " + url);

                final String[]   sections     = StringUtils.substringsBetween(html, "<div class=\"news-wrap\">", "</a>\n</div>");
                final List<Item> items        = new ArrayList<>(sections.length);
                final String     categoryName = this.getCategoryName(url);

                for (final String section : sections) {
                    if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "Item = " + section);

                    final Item item = new Item();

                    item.setTitle(StringUtils.substringBetween(section, "<div class=\"title\">", SingTaoRealtimeClient.TAG_CLOSE));
                    item.setLink(StringUtils.substringBetween(section, "<a href=\"", SingTaoRealtimeClient.TAG_QUOTE));
                    item.setSource(this.source.getName());
                    item.setCategory(categoryName);

                    final String image = StringUtils.substringBetween(section, "<img src=\"", SingTaoRealtimeClient.TAG_QUOTE);
                    if (image != null) item.getImages().add(new Image(image));

                    if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "Title = " + item.getTitle());
                    if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "Link = " + item.getLink());
                    if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "Description = " + item.getDescription());

                    try {
                        item.setPublishDate(SingTaoRealtimeClient.DATE_FORMAT.get().parse(StringUtils.substringBetween(section, "<i class=\"fa fa-clock-o mr5\"></i>", SingTaoRealtimeClient.TAG_CLOSE)));

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
                final String   html            = StringUtils.substringBetween(IOUtils.toString(this.client.download(item.getLink()), Client.ENCODING), "<div class=\"post-content\">", "<div class=\"post-sharing\">");
                final String[] imageContainers = StringUtils.substringsBetween(html, "<a class=\"fancybox-thumb\"", ">");

                for (final String imageContainer : imageContainers) {
                    final String imageUrl         = StringUtils.substringBetween(imageContainer, "href=\"", SingTaoRealtimeClient.TAG_QUOTE);
                    final String imageDescription = StringUtils.substringBetween(imageContainer, "title=\"", SingTaoRealtimeClient.TAG_QUOTE);

                    if (imageUrl != null) item.getImages().add(new Image(imageUrl, imageDescription));
                }

                final String[]      contents = StringUtils.substringsBetween(html, "<p>", "</p>");
                final StringBuilder builder  = new StringBuilder();

                for (final String content : contents) builder.append(content).append("<br>");

                item.setDescription(builder.toString());
                item.setIsFullDescription(true);

                emitter.onNext(item);
            } catch (final IOException e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.BUFFER);
    }
}
