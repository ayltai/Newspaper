package com.github.ayltai.newspaper.client;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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

final class SingPaoClient extends Client {
    //region Constants

    private static final String BASE_URI = "https://www.singpao.com.hk/";
    private static final String TAG      = "'";
    private static final String FONT     = "</font>";
    private static final String CLOSE    = "</p>";

    //endregion

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        }
    };

    @Inject
    SingPaoClient(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
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
                    final String[]       sections = StringUtils.substringsBetween(html, "<tr valign='top'><td width='220'>", "</td></tr>");
                    final List<NewsItem> items    = new ArrayList<>(sections.length);
                    final String         category = this.getCategoryName(url);

                    for (final String section : sections) {
                        final NewsItem item = new NewsItem();

                        item.setTitle(StringUtils.substringBetween(section, "class='list_title'>", "</a>"));
                        item.setLink(SingPaoClient.BASE_URI + StringUtils.substringBetween(section, "<td><a href='", SingPaoClient.TAG));
                        item.setDescription(StringUtils.substringBetween(section, "<br><br>\n", SingPaoClient.FONT));
                        item.setSource(this.source.getName());
                        if (category != null) item.setCategory(category);
                        item.getImages().add(new Image(SingPaoClient.BASE_URI + StringUtils.substringBetween(section, "<img src='", SingPaoClient.TAG)));

                        try {
                            item.setPublishDate(SingPaoClient.DATE_FORMAT.get().parse(StringUtils.substringBetween(section, "<font class='list_date'>", "<br>")));

                            items.add(item);
                        } catch (final ParseException e) {
                            if (DevUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), e.getMessage(), RxJava2Debug.getEnhancedStackTrace(e));
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
                        html = StringUtils.substringBetween(html, "<td class='news_title'>", "您可能有興趣:");

                        final List<Image> images            = new ArrayList<>();
                        final String[]    imageUrls         = StringUtils.substringsBetween(html, "target='_blank'><img src='", SingPaoClient.TAG);
                        final String[]    imageDescriptions = StringUtils.substringsBetween(html, "<font size='4'>", SingPaoClient.FONT);

                        for (int i = 0; i < imageUrls.length; i++) {
                            final Image image = new Image(SingPaoClient.BASE_URI + imageUrls[i], imageDescriptions[i]);

                            if (!images.contains(image)) images.add(image);
                        }

                        if (!images.isEmpty()) {
                            item.getImages().clear();
                            item.getImages().addAll(images);
                        }

                        String[] contents = StringUtils.substringsBetween(html, "<p class=\"內文\">", SingPaoClient.CLOSE);
                        if (contents.length == 0) contents = StringUtils.substringsBetween(html, "<p>", SingPaoClient.CLOSE);

                        final StringBuilder builder = new StringBuilder();
                        for (final String content : contents) builder.append(content).append("<br><br>");

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
