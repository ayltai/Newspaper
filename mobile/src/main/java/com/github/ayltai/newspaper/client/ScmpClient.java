package com.github.ayltai.newspaper.client;

import java.util.ArrayList;
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

final class ScmpClient extends RssClient {
    private static final String CLOSE_DIV   = "</div>";
    private static final String CLOSE_QUOTE = "\"";

    @Inject
    ScmpClient(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
        super(client, apiService, source);
    }

    @WorkerThread
    @NonNull
    @Override
    public Single<NewsItem> updateItem(@NonNull final NewsItem item) {
        return Single.create(emitter -> this.apiService
            .getHtml(item.getLink())
            .compose(RxUtils.applyObservableBackgroundSchedulers())
            .retryWhen(RxUtils.exponentialBackoff(Constants.INITIAL_RETRY_DELAY, Constants.MAX_RETRIES, NetworkUtils::shouldRetry))
            .subscribe(
                html -> {
                    final String[]      contents        = StringUtils.substringsBetween(StringUtils.substringBetween(html, "<div class=\"panel-pane pane-entity-field pane-node-body", ScmpClient.CLOSE_DIV), "<p>", "</p>");
                    final String        imagesContainer = StringUtils.substringBetween(html, "<div class=\"swiper-container scmp-gallery-swiper\">", ScmpClient.CLOSE_DIV);
                    final List<Image>   images          = new ArrayList<>();
                    final StringBuilder builder         = new StringBuilder();

                    if (imagesContainer != null) {
                        final String[] imageContainers = StringUtils.substringsBetween(imagesContainer, "<img ", "/>");

                        for (final String imageContainer : imageContainers) {
                            final String imageUrl         = StringUtils.substringBetween(imageContainer, "data-enlarge=\"", ScmpClient.CLOSE_QUOTE);
                            final String imageDescription = StringUtils.substringBetween(imageContainer, "data-caption=\"", ScmpClient.CLOSE_QUOTE);

                            if (imageUrl != null) images.add(new Image(imageUrl, imageDescription));
                        }
                    }

                    for (final String content : contents) {
                        final String imageUrl         = StringUtils.substringBetween(content, "data-original=\"", ScmpClient.CLOSE_QUOTE);
                        final String imageDescription = StringUtils.substringBetween(content, "<img title=\"", ScmpClient.CLOSE_QUOTE);

                        if (imageUrl == null) {
                            builder.append(content).append("<br><br>");
                        } else {
                            images.add(new Image(imageUrl, imageDescription));
                        }
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
            ));
    }
}
