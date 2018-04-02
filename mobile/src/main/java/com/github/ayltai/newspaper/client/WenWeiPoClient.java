package com.github.ayltai.newspaper.client;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

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

final class WenWeiPoClient extends Client {
    //region Constants

    private static final String CLOSE_QUOTE     = "\"";
    private static final String CLOSE_PARAGRAPH = "</p>";
    private static final String LINE_BREAKS     = "<br><br>";

    //endregion

    @Inject
    WenWeiPoClient(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
        super(client, apiService, source);
    }

    @WorkerThread
    @NonNull
    @Override
    public Single<List<NewsItem>> getItems(@NonNull final String url) {
        return Single.create(emitter -> this.apiService
            .getHtml(url)
            .compose(RxUtils.applyObservableBackgroundSchedulers())
            .retryWhen(RxUtils.exponentialBackoff(Constants.INITIAL_RETRY_DELAY, Constants.MAX_RETRIES, NetworkUtils::shouldRetry))
            .subscribe(
                html -> {
                    final String[]       sections = StringUtils.substringsBetween(html, "<div class=\"content-art-box\">", "</article>");
                    final List<NewsItem> items    = new ArrayList<>(sections.length);
                    final String         category = this.getCategoryName(url);
                    final Calendar       calendar = Calendar.getInstance();

                    for (final String section : sections) {
                        final NewsItem item = new NewsItem();
                        final String   link = StringUtils.substringBetween(section, "<a href=\"", WenWeiPoClient.CLOSE_QUOTE);

                        if (link != null) {
                            item.setTitle(StringUtils.substringBetween(section, "target=\"_blank\">", "</a>"));
                            item.setDescription(StringUtils.substringBetween(section, "<p class=\"txt\">", WenWeiPoClient.CLOSE_PARAGRAPH));
                            item.setLink(link);
                            item.setSource(this.source.getName());
                            if (category != null) item.setCategory(category);

                            final String image = StringUtils.substringBetween(section, "<img src=\"", WenWeiPoClient.CLOSE_QUOTE);
                            if (image != null) item.getImages().add(new Image(image));

                            final String date = StringUtils.substringBetween(section, "<p class=\"date\">[ ", " ]</p>");
                            if (date != null) {
                                final String[] tokens = date.split("日 ");
                                final String[] times  = tokens[1].split(":");

                                calendar.set(Calendar.DATE, Integer.parseInt(tokens[0]));
                                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(times[0]));
                                calendar.set(Calendar.MINUTE, Integer.parseInt(times[1]));

                                item.setPublishDate(calendar.getTime());

                                items.add(item);
                            }
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
                        final String      html            = StringUtils.substringBetween(fullHtml, "<!-- Content start -->", "!-- Content end -->");
                        final String[]    imageContainers = StringUtils.substringsBetween(html, "<img ", ">");
                        final List<Image> images          = new ArrayList<>();

                        for (final String imageContainer : imageContainers) {
                            final String imageUrl         = StringUtils.substringBetween(imageContainer, "src=\"", WenWeiPoClient.CLOSE_QUOTE);
                            final String imageDescription = StringUtils.substringBetween(imageContainer, "alt=\"", WenWeiPoClient.CLOSE_QUOTE);

                            if (imageUrl != null) images.add(new Image(imageUrl, imageDescription));
                        }

                        if (!images.isEmpty()) {
                            item.getImages().clear();
                            item.getImages().addAll(images);
                        }

                        final String[]      primaryContents   = StringUtils.substringsBetween(html, "<p >", WenWeiPoClient.CLOSE_PARAGRAPH);
                        final String[]      secondaryContents = StringUtils.substringsBetween(html, "<p>", WenWeiPoClient.CLOSE_PARAGRAPH);
                        final StringBuilder builder           = new StringBuilder();

                        for (final String content : primaryContents)   builder.append(content).append(WenWeiPoClient.LINE_BREAKS);
                        for (final String content : secondaryContents) builder.append(content).append(WenWeiPoClient.LINE_BREAKS);

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
