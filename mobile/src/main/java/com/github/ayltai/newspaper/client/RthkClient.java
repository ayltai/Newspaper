package com.github.ayltai.newspaper.client;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.Source;
import com.github.ayltai.newspaper.app.data.model.Video;
import com.github.ayltai.newspaper.net.ApiService;
import com.github.ayltai.newspaper.net.NetworkUtils;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import io.reactivex.Single;
import okhttp3.OkHttpClient;

final class RthkClient extends RssClient {
    //region Constants

    private static final String TAG_CLOSE        = "</div>";
    private static final String TAG_DOUBLE_QUOTE = "\"";
    private static final String TAG_SINGLE_QUOTE = "'";

    //endregion

    @Inject
    RthkClient(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
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
                    final String imageContainer = StringUtils.substringBetween(html, "<div class=\"itemSlideShow\">", "<div class=\"clr\"></div>");

                    if (imageContainer != null) {
                        final String imageUrl         = StringUtils.substringBetween(imageContainer, "<a href=\"", RthkClient.TAG_DOUBLE_QUOTE);
                        final String imageDescription = StringUtils.substringBetween(imageContainer, "alt=\"", RthkClient.TAG_DOUBLE_QUOTE);

                        if (imageUrl != null) {
                            item.getImages().clear();
                            item.getImages().add(new Image(imageUrl, imageDescription));
                        }

                        final String videoUrl     = StringUtils.substringBetween(imageContainer, "var videoFile\t\t\t= '", RthkClient.TAG_SINGLE_QUOTE);
                        final String thumbnailUrl = StringUtils.substringBetween(imageContainer, "var videoThumbnail\t\t= '", RthkClient.TAG_SINGLE_QUOTE);

                        if (videoUrl != null && thumbnailUrl != null) item.setVideo(new Video(videoUrl, thumbnailUrl));
                    }

                    item.setDescription(StringUtils.substringBetween(html, "<div class=\"itemFullText\">", RthkClient.TAG_CLOSE));
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
