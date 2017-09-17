package com.github.ayltai.newspaper.client;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.github.ayltai.newspaper.data.model.Image;
import com.github.ayltai.newspaper.data.model.NewsItem;
import com.github.ayltai.newspaper.data.model.Source;
import com.github.ayltai.newspaper.data.model.Video;
import com.github.ayltai.newspaper.net.ApiService;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.Maybe;
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
    public Maybe<NewsItem> updateItem(@NonNull final NewsItem item) {
        return Maybe.create(emitter -> this.apiService
            .getHtml(item.getLink())
            .compose(RxUtils.applyObservableBackgroundSchedulers())
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

                    emitter.onSuccess(item);
                },
                error -> {
                    if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);

                    emitter.onError(error);
                }
            ));
    }
}
