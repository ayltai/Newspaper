package com.github.ayltai.newspaper.client;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.Source;
import com.github.ayltai.newspaper.net.ApiService;
import com.github.ayltai.newspaper.net.NetworkUtils;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import io.reactivex.Single;
import okhttp3.OkHttpClient;

final class TheStandardClient extends Client {
    //region Constants

    private static final String BASE_URL = "http://www.thestandard.com.hk/";

    private static final String CLOSE_QUOTE     = "\"";
    private static final String OPEN_HREF       = "<a href=\"";
    private static final String OPEN_PARAGRAPH  = "<p>";
    private static final String CLOSE_PARAGRAPH = "</p>";

    private static final ThreadLocal<DateFormat> DATE_FORMAT_LONG = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd MMM yyyy h:mm a", Locale.ENGLISH);
        }
    };

    private static final ThreadLocal<DateFormat> DATE_FORMAT_SHORT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        }
    };

    //endregion

    @Inject
    TheStandardClient(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
        super(client, apiService, source);
    }

    @WorkerThread
    @NonNull
    @Override
    public Single<List<NewsItem>> getItems(@NonNull final String url) {
        final String[] tokens    = url.split(Pattern.quote("?"));
        final String   sessionId = tokens[1].substring(tokens[1].indexOf("sid=") + 4);

        // TODO: Supports multi-page loading
        return Single.create(emitter -> this.apiService
            .postHtml(tokens[0], Integer.parseInt(sessionId), 1)
            .compose(RxUtils.applyObservableBackgroundSchedulers())
            .retryWhen(RxUtils.exponentialBackoff(Constants.INITIAL_RETRY_DELAY, Constants.MAX_RETRIES, NetworkUtils::shouldRetry))
            .subscribe(
                html -> {
                    final String[]       sections = StringUtils.substringsBetween(html, "<li class='caption'>", "</li>");
                    final List<NewsItem> items    = new ArrayList<>(sections.length);
                    final String         category = this.getCategoryName(url);

                    for (final String section : sections) {
                        final NewsItem item = new NewsItem();
                        final String   link = StringUtils.substringBetween(section, TheStandardClient.OPEN_HREF, TheStandardClient.CLOSE_QUOTE);

                        if (link != null) {
                            final String title        = StringUtils.substringBetween(section, "<h1>", "</h1>");
                            final String trimmedTitle = StringUtils.substringBetween(title, "\">", "</a>");
                            item.setTitle(trimmedTitle == null ? title : trimmedTitle);

                            item.setDescription(StringUtils.substringBetween(section, TheStandardClient.OPEN_PARAGRAPH, TheStandardClient.CLOSE_PARAGRAPH));
                            item.setLink(TheStandardClient.BASE_URL + link);
                            item.setSource(this.source.getName());
                            if (category != null) item.setCategory(category);

                            final String image = StringUtils.substringBetween(section, "<img src=\"", TheStandardClient.CLOSE_QUOTE);
                            if (image != null) item.getImages().add(new Image(image));

                            final String date = StringUtils.substringBetween(section, "<span>", "</span>");
                            try {
                                item.setPublishDate(TheStandardClient.DATE_FORMAT_LONG.get().parse(date));
                            } catch (final ParseException e) {
                                try {
                                    item.setPublishDate(TheStandardClient.DATE_FORMAT_SHORT.get().parse(date));
                                } catch (final ParseException x) {
                                    // Ignored
                                }
                            }

                            items.add(item);
                        }
                    }

                    if (!emitter.isDisposed()) emitter.onSuccess(this.filter(items));
                },
                error -> {
                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), "Error URL = " + url, RxJava2Debug.getEnhancedStackTrace(error));

                    if (!emitter.isDisposed()) emitter.onSuccess(Collections.emptyList());
                }
            ));
    }

    @WorkerThread
    @NonNull
    @Override
    public Single<NewsItem> updateItem(@NonNull final NewsItem item) {
        return Single.create(emitter -> {
            if (DevUtils.isLoggable()) Log.d(this.getClass().getSimpleName(), item.getLink());

            this.apiService
                .getHtml(item.getLink())
                .compose(RxUtils.applyObservableBackgroundSchedulers())
                .retryWhen(RxUtils.exponentialBackoff(Constants.INITIAL_RETRY_DELAY, Constants.MAX_RETRIES, NetworkUtils::shouldRetry))
                .subscribe(
                    fullHtml -> {
                        final String        html     = StringUtils.substringBetween(fullHtml, "<div class=\"content\">", "<!-- ./carousel -->");
                        final String[]      contents = StringUtils.substringsBetween(html, TheStandardClient.OPEN_PARAGRAPH, TheStandardClient.CLOSE_PARAGRAPH);
                        final StringBuilder builder  = new StringBuilder();

                        for (final String content : contents) builder.append(content).append("<br><br>");

                        final String[]    imageContainers = StringUtils.substringsBetween(html, "<figure>", "</figure>");
                        final List<Image> images          = new ArrayList<>();

                        for (final String imageContainer : imageContainers) {
                            final String imageUrl         = StringUtils.substringBetween(imageContainer, TheStandardClient.OPEN_HREF, TheStandardClient.CLOSE_QUOTE);
                            final String imageDescription = StringUtils.substringBetween(imageContainer, "<i>", "</i>");

                            if (imageUrl != null) images.add(new Image(imageUrl, imageDescription));
                        }

                        if (!images.isEmpty()) {
                            item.getImages().clear();
                            item.getImages().addAll(images);
                        }

                        item.setDescription(builder.toString());
                        item.setIsFullDescription(true);

                        if (!emitter.isDisposed()) emitter.onSuccess(item);
                    },
                    error -> {
                        if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), "Error URL = " + item.getLink(), RxJava2Debug.getEnhancedStackTrace(error));

                        if (!emitter.isDisposed()) emitter.onSuccess(item);
                    }
                );
        });
    }
}
