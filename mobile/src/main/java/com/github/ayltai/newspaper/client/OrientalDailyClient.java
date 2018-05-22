package com.github.ayltai.newspaper.client;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.github.ayltai.newspaper.rss.RssFeed;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import okhttp3.OkHttpClient;

final class OrientalDailyClient extends RssClient {
    //region Constants

    private static final String BASE_URI  = "http://orientaldaily.on.cc";
    private static final String TAG_CLOSE = "\"";
    private static final String SLASH     = "/";
    private static final String DIV_CLOSE = "</div>";

    //endregion

    @Inject
    OrientalDailyClient(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
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
                    html = StringUtils.substringBetween(html, "<div id=\"contentCTN-top\"", "<div id=\"articleNav\">");

                    final String[]    imageContainers = StringUtils.substringsBetween(html, "<div class=\"photo", OrientalDailyClient.DIV_CLOSE);
                    final List<Image> images          = new ArrayList<>();

                    for (final String imageContainer : imageContainers) {
                        final String imageUrl         = StringUtils.substringBetween(imageContainer, "href=\"", OrientalDailyClient.TAG_CLOSE);
                        final String imageDescription = StringUtils.substringBetween(imageContainer, "title=\"", OrientalDailyClient.TAG_CLOSE);

                        if (imageUrl != null) images.add(new Image(OrientalDailyClient.BASE_URI + imageUrl, imageDescription));
                    }

                    if (!images.isEmpty()) {
                        item.getImages().clear();
                        item.getImages().addAll(images);
                    }

                    final Video video = this.extractVideo(item.getLink());
                    if (video != null) item.setVideo(video);

                    final String[]      contents = StringUtils.substringsBetween(html, "<p>", "</p>");
                    final StringBuilder builder  = new StringBuilder();

                    for (final String content : contents) builder.append(content).append("<br><br>");

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

    @NonNull
    @Override
    protected List<NewsItem> filter(@NonNull final String url, @NonNull final RssFeed feed) {
        final List<NewsItem> items = super.filter(url, feed);

        for (final NewsItem item : items) item.setDescription(StringUtils.substringBetween(item.getDescription(), "<div style=\"float:left;\">", OrientalDailyClient.DIV_CLOSE));

        return items;
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @WorkerThread
    @Nullable
    private Video extractVideo(@NonNull final String url) {
        final String date = StringUtils.substringBetween(url, "http://orientaldaily.on.cc/cnt/news/", OrientalDailyClient.SLASH);
        if (date == null) return null;

        final String videoList = this.apiService.getHtml("http://orientaldaily.on.cc/cnt/keyinfo/" + date + "/videolist.xml").blockingSingle();
        if (videoList == null) return null;

        for (final String video : StringUtils.substringsBetween(videoList, "<news>", "</news>")) {
            if (("odn-" + date + "-" + date.substring(4) + "_" + StringUtils.substringBetween(url, date + OrientalDailyClient.SLASH, ".html")).equals(StringUtils.substringBetween(video, "<articleID>", "</articleID>"))) {
                final String thumbnailUri = StringUtils.substringBetween(video, "<thumbnail>", "</thumbnail>");
                final String videoUri     = StringUtils.substringBetween(video, "?mid=", "&amp;mtype=video");

                if (videoUri != null && thumbnailUri != null) return new Video("http://video.cdn.on.cc/Video/" + date.substring(0, 6) + OrientalDailyClient.SLASH + videoUri + "_ipad.mp4", "http://tv.on.cc/xml/Thumbnail/" + date.substring(0, 6) + "/bigthumbnail/" + thumbnailUri);
            }
        }

        return null;
    }
}
