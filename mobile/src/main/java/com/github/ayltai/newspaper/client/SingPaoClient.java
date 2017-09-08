package com.github.ayltai.newspaper.client;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

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

final class SingPaoClient extends Client {
    //region Constants

    private static final String BASE_URI = "https://www.singpao.com.hk/";
    private static final String TAG      = "'";
    private static final String FONT     = "</font>";

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
    public Single<List<Item>> getItems(@NonNull final String url) {
        return Single.create(emitter -> this.apiService
            .getHtml(url)
            .compose(RxUtils.applyObservableBackgroundSchedulers())
            .subscribe(
                html -> {
                    final String[]   sections = StringUtils.substringsBetween(html, "<tr valign='top'><td width='220'>", "</td></tr>");
                    final List<Item> items    = new ArrayList<>(sections.length);
                    final String     category = this.getCategoryName(url);

                    for (final String section : sections) {
                        final Item item = new Item();

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
                            if (TestUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), e.getMessage(), e);
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
                        html = StringUtils.substringBetween(html, "<td class='news_title'>", "您可能有興趣:");

                        final List<Image> images            = new ArrayList<>();
                        final String[]    imageUrls         = StringUtils.substringsBetween(html, "target='_blank'><img src='", SingPaoClient.TAG);
                        final String[]    imageDescriptions = StringUtils.substringsBetween(html, "<font size='4'>", SingPaoClient.FONT);

                        for (int i = 0; i < imageUrls.length; i++) {
                            final Image image = new Image(SingPaoClient.BASE_URI + imageUrls[0], imageDescriptions[0]);

                            if (!images.contains(image)) images.add(image);
                        }

                        if (!images.isEmpty()) {
                            item.getImages().clear();
                            item.getImages().addAll(images);
                        }

                        final String[]      contents = StringUtils.substringsBetween(html, "<p>", "</p>");
                        final StringBuilder builder  = new StringBuilder();

                        for (final String content : contents) builder.append(content).append("<br><br>");

                        item.setDescription(builder.toString());
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
}
