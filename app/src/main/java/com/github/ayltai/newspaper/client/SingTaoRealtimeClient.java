package com.github.ayltai.newspaper.client;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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

import io.reactivex.Maybe;
import io.reactivex.Single;

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
    public Single<List<Item>> getItems(@NonNull final String url) {
        return Single.create(emitter -> {
            try {
                final InputStream inputStream = this.client.download(url);

                if (inputStream == null) {
                    LogUtils.getInstance().i(this.getClass().getSimpleName(), "No content from URL " + url);

                    emitter.onSuccess(Collections.emptyList());
                } else {
                    if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), url);

                    final String     html         = IOUtils.toString(inputStream, Client.ENCODING);
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

                    emitter.onSuccess(items);
                }
            } catch (final IOException e) {
                this.handleError(emitter, e);
            }
        });
    }

    @NonNull
    @Override
    public Maybe<Item> updateItem(@NonNull final Item item) {
        return Maybe.create(emitter -> {
            if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), item.getLink());

            try {
                final String      html            = StringUtils.substringBetween(IOUtils.toString(this.client.download(item.getLink()), Client.ENCODING), "<div class=\"post-content\">", "<div class=\"post-sharing\">");
                final String[]    imageContainers = StringUtils.substringsBetween(html, "<a class=\"fancybox-thumb\"", ">");
                final List<Image> images          = new ArrayList<>();

                for (final String imageContainer : imageContainers) {
                    final String imageUrl         = StringUtils.substringBetween(imageContainer, "href=\"", SingTaoRealtimeClient.TAG_QUOTE);
                    final String imageDescription = StringUtils.substringBetween(imageContainer, "title=\"", SingTaoRealtimeClient.TAG_QUOTE);

                    if (imageUrl != null) images.add(new Image(imageUrl, imageDescription));
                }

                if (!images.isEmpty()) {
                    item.getImages().clear();
                    item.getImages().addAll(images);
                }

                final String[]      contents = StringUtils.substringsBetween(html, "<p>", "</p>");
                final StringBuilder builder  = new StringBuilder();

                for (final String content : contents) builder.append(content).append("<br>");

                item.setDescription(builder.toString());
                item.setIsFullDescription(true);

                emitter.onSuccess(item);
            } catch (final IOException e) {
                this.handleError(emitter, e);
            }
        });
    }
}
