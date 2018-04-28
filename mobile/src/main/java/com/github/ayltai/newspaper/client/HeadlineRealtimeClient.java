package com.github.ayltai.newspaper.client;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.Source;
import com.github.ayltai.newspaper.net.ApiService;
import com.github.ayltai.newspaper.net.NetworkUtils;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.reactivex.Single;
import okhttp3.OkHttpClient;

final class HeadlineRealtimeClient extends Client {
    //region Constants

    private static final String BASE_URI  = "http://hd.stheadline.com";
    private static final String IMAGE_URI = "http://static.stheadline.com";
    private static final String TAG_LINK  = "</a>";
    private static final String TAG_QUOTE = "\"";
    private static final String HTTP      = "http:";

    //endregion

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        }
    };

    HeadlineRealtimeClient(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
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
                    final String[]       sections = StringUtils.substringsBetween(html, "<div class=\"topic\">", "<p class=\"text-left\">");
                    final List<NewsItem> items    = new ArrayList<>(sections.length);
                    final String         category = this.getCategoryName(url);

                    for (final String section : sections) {
                        final NewsItem item  = new NewsItem();
                        final String   title = StringUtils.substringBetween(section, "<h", "</h");

                        item.setTitle(StringUtils.substringBetween(title, "\">", HeadlineRealtimeClient.TAG_LINK));
                        item.setLink(HeadlineRealtimeClient.BASE_URI + StringUtils.substringBetween(title, "<a href=\"", "\" "));
                        item.setDescription(StringUtils.substringBetween(section, "<p class=\"text\">", "</p>"));
                        item.setSource(this.source.getName());
                        if (category != null) item.setCategory(category);

                        final String image = StringUtils.substringBetween(section, "<img src=\"", HeadlineRealtimeClient.TAG_QUOTE);
                        if (image != null) item.getImages().add(new Image(HeadlineRealtimeClient.formatImageUrl(image)));

                        try {
                            item.setPublishDate(HeadlineRealtimeClient.DATE_FORMAT.get().parse(StringUtils.substringBetween(section, "<i class=\"fa fa-clock-o\"></i>", "</span>")));

                            items.add(item);
                        } catch (final ParseException e) {
                            if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), RxJava2Debug.getEnhancedStackTrace(e));
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
                    html -> {
                        HeadlineRealtimeClient.extractImages(StringUtils.substringsBetween(html, "<a class=\"fancybox image\" rel=\"fancybox-thumb\"", HeadlineRealtimeClient.TAG_LINK), item);

                        item.setDescription(StringUtils.substringBetween(html, "<div id=\"news-content\" class=\"set-font-aera\" style=\"visibility: visible;\">", "</div>"));
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

    private static void extractImages(@NonNull final String[] imageContainers, @NonNull final Item item) {
        final List<Image> images = new ArrayList<>();

        for (final String imageContainer : imageContainers) {
            final String imageUrl         = StringUtils.substringBetween(imageContainer, "href=\"", HeadlineRealtimeClient.TAG_QUOTE);
            final String imageDescription = StringUtils.substringBetween(imageContainer, "title=\"", HeadlineRealtimeClient.TAG_QUOTE);

            if (imageUrl != null) images.add(new Image(HeadlineRealtimeClient.formatImageUrl(imageUrl), imageDescription));
        }

        if (!images.isEmpty()) {
            item.getImages().clear();
            item.getImages().addAll(images);
        }
    }

    @NonNull
    private static String formatImageUrl(@NonNull final String url) {
        return url.startsWith("//") ? HeadlineRealtimeClient.HTTP + url : url.startsWith(HeadlineRealtimeClient.HTTP) ? url : HeadlineRealtimeClient.IMAGE_URI + url;
    }
}
