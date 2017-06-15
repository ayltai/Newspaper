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

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.model.Image;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.net.HttpClient;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import io.reactivex.Maybe;
import io.reactivex.Single;

final class HketClient extends Client {
    //region Constants

    private static final int DATE_LENGTH = 10;

    private static final String BASE_URI       = "https://topick.hket.com";
    private static final String CHINA_BASE_URI = "http://china.hket.com/";

    private static final String TAG_DATA_SRC  = "data-src=\"";
    private static final String TAG_PARAGRAPH = "</p>";
    private static final String TAG_QUOTE     = "\"";

    //endregion

    private static final ThreadLocal<DateFormat> DATE_FORMAT_LONG = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
        }
    };

    private static final ThreadLocal<DateFormat> DATE_FORMAT_SHORT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        }
    };

    @Inject
    HketClient(@NonNull final HttpClient client, @Nullable final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    public Single<List<Item>> getItems(@NonNull final String url) {
        return Single.create(emitter -> {
            try {
                final InputStream inputStream = this.client.download(url);

                if (inputStream == null) {
                    emitter.onSuccess(Collections.emptyList());
                    return;
                }

                final String     html         = IOUtils.toString(inputStream, Client.ENCODING);
                final String[]   sections     = StringUtils.substringsBetween(html, "<div class=\"article-listing\">", "</a>");
                final List<Item> items        = new ArrayList<>(sections.length);
                final String     categoryName = this.getCategoryName(url);

                for (final String section : sections) {
                    final Item   item = new Item();
                    final String link = StringUtils.substringBetween(section, "href=\"", HketClient.TAG_QUOTE);

                    item.setTitle(StringUtils.substringBetween(section, "<h3 class=\"reduce-line\">", "</h3>"));
                    if (link != null) item.setLink((link.startsWith("http") ? Constants.EMPTY : HketClient.BASE_URI) + link);
                    item.setSource(this.source.getName());
                    item.setCategory(categoryName);

                    final String image = StringUtils.substringBetween(section, HketClient.TAG_DATA_SRC, HketClient.TAG_QUOTE);
                    if (image != null) item.getImages().add(new Image(image));

                    final String date = StringUtils.substringBetween(section, "<p class=\"article-listing-detail_datetime\">", HketClient.TAG_PARAGRAPH);

                    if (date != null) {
                        try {
                            item.setPublishDate(date.length() > HketClient.DATE_LENGTH ? HketClient.DATE_FORMAT_LONG.get().parse(date) : HketClient.DATE_FORMAT_SHORT.get().parse(date));

                            items.add(item);
                        } catch (final ParseException e) {
                            LogUtils.getInstance().w(this.getClass().getSimpleName(), e.getMessage(), e);
                        }
                    }
                }

                emitter.onSuccess(this.filters(url, items));
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
                final boolean     isChinaNews     = item.getLink().startsWith(HketClient.CHINA_BASE_URI);
                final String      html            = StringUtils.substringBetween(IOUtils.toString(this.client.download(item.getLink()), Client.ENCODING), isChinaNews ? "<div id=\"content-main\">" : "<div class=\"article-detail\">", isChinaNews ? "<div class=\"fb-like\"" : "<div class=\"article-detail_facebook-like\">");
                final String[]    imageContainers = StringUtils.substringsBetween(html, "<img ", "/>");
                final List<Image> images          = new ArrayList<>();

                for (final String imageContainer : imageContainers) {
                    final String imageUrl         = StringUtils.substringBetween(imageContainer, HketClient.TAG_DATA_SRC, HketClient. TAG_QUOTE);
                    final String imageDescription = StringUtils.substringBetween(imageContainer, "alt=\"", HketClient.TAG_QUOTE);

                    if (imageUrl != null) images.add(new Image(imageUrl, imageDescription));
                }

                if (!images.isEmpty()) {
                    item.getImages().clear();
                    item.getImages().addAll(images);
                }

                final String[]      contents = StringUtils.substringsBetween(html, "<p>", HketClient.TAG_PARAGRAPH);
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
