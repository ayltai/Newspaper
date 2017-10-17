package com.github.ayltai.newspaper.client;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.Source;
import com.github.ayltai.newspaper.net.NewsApiService;
import com.github.ayltai.newspaper.net.NetworkUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.Single;
import okhttp3.OkHttpClient;

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

    SingTaoRealtimeClient(@NonNull final OkHttpClient client, @NonNull final NewsApiService apiService, @NonNull final Source source) {
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
                    final String[]       sections = StringUtils.substringsBetween(html, "<div class=\"news-wrap\">", "</a>\n</div>");
                    final List<NewsItem> items    = new ArrayList<>(sections.length);
                    final String         category = this.getCategoryName(url);

                    for (final String section : sections) {
                        final NewsItem item = new NewsItem();

                        item.setTitle(StringUtils.substringBetween(section, "<div class=\"title\">", SingTaoRealtimeClient.TAG_CLOSE));
                        item.setLink(StringUtils.substringBetween(section, "<a href=\"", SingTaoRealtimeClient.TAG_QUOTE));
                        item.setSource(this.source.getName());
                        if (category != null) item.setCategory(category);

                        final String image = StringUtils.substringBetween(section, "<img src=\"", SingTaoRealtimeClient.TAG_QUOTE);
                        if (image != null) item.getImages().add(new Image(image));

                        try {
                            item.setPublishDate(SingTaoRealtimeClient.DATE_FORMAT.get().parse(StringUtils.substringBetween(section, "<i class=\"fa fa-clock-o mr5\"></i>", SingTaoRealtimeClient.TAG_CLOSE)));

                            items.add(item);
                        } catch (final ParseException e) {
                            if (TestUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), e.getMessage(), e);
                        }
                    }

                    if (!emitter.isDisposed()) emitter.onSuccess(this.filter(items));
                },
                error -> {
                    if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), "Error URL = " + url, error);

                    if (!emitter.isDisposed()) emitter.onSuccess(Collections.emptyList());
                }
            ));
    }

    @WorkerThread
    @NonNull
    @Override
    public Single<NewsItem> updateItem(@NonNull final NewsItem item) {
        return Single.create(emitter -> {
            if (TestUtils.isLoggable()) Log.d(this.getClass().getSimpleName(), item.getLink());

            this.apiService
                .getHtml(item.getLink())
                .compose(RxUtils.applyObservableBackgroundSchedulers())
                .retryWhen(RxUtils.exponentialBackoff(Constants.INITIAL_RETRY_DELAY, Constants.MAX_RETRIES, NetworkUtils::shouldRetry))
                .subscribe(
                    html -> {
                        html = StringUtils.substringBetween(html, "<div class=\"post-content\">", "<div class=\"post-sharing\">");

                        final String[]    imageContainers = StringUtils.substringsBetween(html, "<a class=\"fancybox-thumb", ">");
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

                        if (!emitter.isDisposed()) emitter.onSuccess(item);
                    },
                    error -> {
                        if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), "Error URL = " + item.getLink(), error);

                        if (!emitter.isDisposed()) emitter.onError(error);
                    }
                );
        });
    }
}
