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
import android.text.TextUtils;
import android.util.Log;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.Source;
import com.github.ayltai.newspaper.app.data.model.Video;
import com.github.ayltai.newspaper.net.ApiService;
import com.github.ayltai.newspaper.net.NetworkUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.Single;
import okhttp3.OkHttpClient;

final class HketClient extends Client {
    //region Constants

    private static final int DATE_LENGTH = 10;

    private static final String BASE_URI        = "https://topick.hket.com";
    private static final String CHINA_BASE_URI  = "http://china.hket.com/";
    private static final String INVEST_BASE_URI = "http://invest.hket.com/";

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
    HketClient(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
        super(client, apiService, source);
    }

    @SuppressWarnings("CyclomaticComplexity")
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
                    final String[]       sections = StringUtils.substringsBetween(StringUtils.substringBetween(html, "<div class=\"section-listing-widget\">", "<div class=\"pagination-widget\">"), "<div class=\"article-listing\">", "</a>");
                    final List<NewsItem> items    = new ArrayList<>(sections.length);
                    final String         category = this.getCategoryName(url);

                    for (final String section : sections) {
                        final NewsItem item = new NewsItem();
                        final String   link = StringUtils.substringBetween(section, "href=\"", HketClient.TAG_QUOTE);

                        item.setTitle(StringUtils.substringBetween(section, "<h3 class=\"reduce-line\">", "</h3>"));
                        if (link != null) item.setLink((link.startsWith("http") ? "" : HketClient.BASE_URI) + link);
                        item.setSource(this.source.getName());
                        if (category != null) item.setCategory(category);

                        final String image = StringUtils.substringBetween(section, HketClient.TAG_DATA_SRC, HketClient.TAG_QUOTE);
                        if (image != null) item.getImages().add(new Image(image));

                        final String date = StringUtils.substringBetween(section, "<p class=\"article-listing-detail_datetime\">", HketClient.TAG_PARAGRAPH);

                        if (date != null) {
                            try {
                                item.setPublishDate(date.length() > HketClient.DATE_LENGTH ? HketClient.DATE_FORMAT_LONG.get().parse(date) : HketClient.DATE_FORMAT_SHORT.get().parse(date));

                                items.add(item);
                            } catch (final ParseException e) {
                                if (TestUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), e.getMessage(), e);
                            }
                        }
                    }

                    emitter.onSuccess(this.filter(items));
                },
                error -> {
                    if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), "Error URL = " + url, error);

                    emitter.onSuccess(Collections.emptyList());
                }
            ));
    }

    @WorkerThread
    @NonNull
    @Override
    public Single<NewsItem> updateItem(@NonNull final NewsItem item) {
        final boolean isChinaNews  = item.getLink().startsWith(HketClient.CHINA_BASE_URI);
        final boolean isInvestNews = item.getLink().startsWith(HketClient.INVEST_BASE_URI);

        return Single.create(emitter -> this.apiService
            .getHtml(item.getLink())
            .compose(RxUtils.applyObservableBackgroundSchedulers())
            .retryWhen(RxUtils.exponentialBackoff(Constants.INITIAL_RETRY_DELAY, Constants.MAX_RETRIES, NetworkUtils::shouldRetry))
            .subscribe(
                html -> {
                    html = StringUtils.substringBetween(html, isChinaNews || isInvestNews ? "<div id=\"content-main\">" : "<div class=\"article-detail\">", isChinaNews ? "<div class=\"fb-like\"" : isInvestNews ? "<div class=\"fb-page-like\">" : "<div class=\"article-detail_facebook-like\">");

                    if (html == null) {
                        emitter.onError(new ParseException("Unparseable content", 0));
                    } else {
                        HketClient.extraImages(html, item);

                        final String videoId = StringUtils.substringBetween(html, "<iframe src=\"//www.youtube.com/embed/", "?rel=0");
                        if (videoId != null) item.setVideo(new Video("https://www.youtube.com/watch?v=" + videoId, String.format("https://img.youtube.com/vi/%s/mqdefault.jpg", videoId)));

                        final String[]      contents = StringUtils.substringsBetween(html, "<p>", HketClient.TAG_PARAGRAPH);
                        final StringBuilder builder  = new StringBuilder();

                        for (final String content : contents) {
                            if (!TextUtils.isEmpty(content)) builder.append(content).append("<br>");
                        }

                        item.setDescription(builder.toString());
                        item.setIsFullDescription(true);

                        emitter.onSuccess(item);
                    }
                },
                error -> {
                    if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), "Error URL = " + item.getLink(), error);

                    emitter.onError(error);
                }
            ));
    }

    private static void extraImages(@NonNull final String html, @NonNull final Item item) {
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
    }
}
