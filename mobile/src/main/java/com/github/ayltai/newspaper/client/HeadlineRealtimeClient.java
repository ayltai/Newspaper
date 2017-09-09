package com.github.ayltai.newspaper.client;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.github.ayltai.newspaper.data.model.Image;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.data.model.Source;
import com.github.ayltai.newspaper.net.ApiService;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.Maybe;
import io.reactivex.Single;
import okhttp3.OkHttpClient;

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

    HeadlineRealtimeClient(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
        super(client, apiService, source);
    }

    @WorkerThread
    @NonNull
    @Override
    public Single<List<Item>> getItems(@NonNull final String url) {
        return Single.create(emitter -> this.apiService
            .getHtml(url)
            .compose(RxUtils.applyObservableBackgroundSchedulers())
            .subscribe(
                html -> {
                    final String[]   sections = StringUtils.substringsBetween(html, "<div class=\"topic\">", "<p class=\"text-left\">");
                    final List<Item> items    = new ArrayList<>(sections.length);
                    final String     category = this.getCategoryName(url);

                    for (final String section : sections) {
                        final Item   item  = new Item();
                        final String title = StringUtils.substringBetween(section, "<h4>", "</h4>");

                        item.setTitle(StringUtils.substringBetween(title, HeadlineRealtimeClient.TAG_CLOSE, HeadlineRealtimeClient.TAG_LINK));
                        item.setLink(HeadlineRealtimeClient.BASE_URI + StringUtils.substringBetween(title, "<a href=\"", HeadlineRealtimeClient.TAG_CLOSE));
                        item.setDescription(StringUtils.substringBetween(section, "<p class=\"text\">", "</p>"));
                        item.setSource(this.source.getName());
                        if (category != null) item.setCategory(category);

                        final String image = StringUtils.substringBetween(section, "<img src=\"", HeadlineRealtimeClient.TAG_QUOTE);
                        if (image != null) item.getImages().add(new Image(HeadlineRealtimeClient.HTTP + image));

                        try {
                            item.setPublishDate(HeadlineRealtimeClient.DATE_FORMAT.get().parse(StringUtils.substringBetween(section, "<i class=\"fa fa-clock-o\"></i>", "</span>")));

                            items.add(item);
                        } catch (final ParseException e) {
                            if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
                        }
                    }

                    emitter.onSuccess(this.filter(items));
                },
                error -> {
                    if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);

                    emitter.onError(error);
                }
            ));
    }

    @WorkerThread
    @NonNull
    @Override
    public Maybe<Item> updateItem(@NonNull final Item item) {
        return Maybe.create(emitter -> {
            if (TestUtils.isLoggable()) Log.d(this.getClass().getSimpleName(), item.getLink());

            this.apiService
                .getHtml(item.getLink())
                .compose(RxUtils.applyObservableBackgroundSchedulers())
                .subscribe(
                    html -> {
                        HeadlineRealtimeClient.extractImages(StringUtils.substringsBetween(html, "<a class=\"fancybox image\" rel=\"fancybox-thumb\"", HeadlineRealtimeClient.TAG_LINK), item);

                        item.setDescription(StringUtils.substringBetween(html, "<div id=\"news-content\" class=\"set-font-aera\" style=\"visibility: visible;\">", "</div>"));
                        item.setIsFullDescription(true);

                        emitter.onSuccess(item);
                    },
                    error -> {
                        if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);

                        emitter.onError(error);
                    }
                );
        });
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
