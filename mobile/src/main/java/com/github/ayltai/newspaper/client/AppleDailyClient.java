package com.github.ayltai.newspaper.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

final class AppleDailyClient extends Client {
    //region Constants

    private static final long SECOND = 1000;

    private static final String SLASH = "/";

    private static final String TAG_QUOTE    = "\"";
    private static final String TAG_HREF     = "href=\"";
    private static final String TAG_TITLE    = "title=\"";
    private static final String TAG_DIV      = "</div>";
    private static final String TAG_OPEN_H2  = "<h2>";
    private static final String TAG_CLOSE_H2 = "</h2>";
    private static final String TAG_OPEN_H3  = "<h3>";
    private static final String TAG_CLOSE_H3 = "</h3>";

    //endregion

    @Inject
    AppleDailyClient(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
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
                    final String[]       sections = StringUtils.substringsBetween(StringUtils.substringBetween(html, "<div class=\"itemContainer\">", "<div class=\"clear\"></div>"), "div class=\"item\">", AppleDailyClient.TAG_DIV);
                    final List<NewsItem> items    = new ArrayList<>(sections.length);
                    final String         category = this.getCategoryName(url);

                    for (final String section : sections) {
                        final NewsItem item = new NewsItem();
                        final String   link = StringUtils.substringBetween(section, AppleDailyClient.TAG_HREF, AppleDailyClient.TAG_QUOTE);

                        if (link != null) {
                            item.setTitle(StringUtils.substringBetween(section, AppleDailyClient.TAG_TITLE, AppleDailyClient.TAG_QUOTE));
                            item.setLink(link.substring(0, link.lastIndexOf(AppleDailyClient.SLASH))
                                .replace("dv", "apple")
                                .replace("actionnews/local", "news/art")
                                .replace("actionnews/chinainternational", "international/art")
                                .replace("actionnews/finance", "financeestate/art")
                                .replace("actionnews/entertainment", "entertainment/art")
                                .replace("actionnews/sports", "sports/art"));
                            item.setSource(this.source.getName());
                            if (category != null) item.setCategory(category);

                            final String image = StringUtils.substringBetween(section, "<img src=\"", AppleDailyClient.TAG_QUOTE);
                            if (image != null) item.getImages().add(new Image(image));

                            final String time = StringUtils.substringBetween(section, "pix/", "_");
                            if (time != null) item.setPublishDate(new Date(Long.valueOf(time) * AppleDailyClient.SECOND));

                            items.add(item);
                        }
                    }

                    emitter.onSuccess(this.filter(items));
                },
                error -> {
                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), "Error URL = " + url, RxJava2Debug.getEnhancedStackTrace(error));

                    emitter.onSuccess(Collections.emptyList());
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
                    fullHtml -> {
                        final String      html            = StringUtils.substringBetween(fullHtml, "!-- START ARTILCLE CONTENT -->", "<!-- END ARTILCLE CONTENT -->");
                        final String[]    imageContainers = StringUtils.substringsBetween(html, "rel=\"fancybox-button\"", "/>");
                        final List<Image> images          = new ArrayList<>();

                        for (final String imageContainer : imageContainers) {
                            final String imageUrl         = StringUtils.substringBetween(imageContainer, AppleDailyClient.TAG_HREF, AppleDailyClient.TAG_QUOTE);
                            final String imageDescription = StringUtils.substringBetween(imageContainer, AppleDailyClient.TAG_TITLE, AppleDailyClient.TAG_QUOTE);

                            if (imageUrl != null) images.add(new Image(imageUrl, imageDescription));
                        }

                        if (!images.isEmpty()) {
                            item.getImages().clear();
                            item.getImages().addAll(images);
                        }

                        final Video video = this.extractVideo(item.getLink(), StringUtils.substringBetween(fullHtml, "var videoId = '", "';"));
                        if (video != null) item.setVideo(video);

                        final String[]      contents = StringUtils.substringsBetween(html, "<div class=\"ArticleContent_Inner\">", AppleDailyClient.TAG_DIV);
                        final StringBuilder builder  = new StringBuilder();

                        for (final String content : contents) builder.append(content.replace(AppleDailyClient.TAG_OPEN_H2, AppleDailyClient.TAG_OPEN_H3).replace(AppleDailyClient.TAG_CLOSE_H2, AppleDailyClient.TAG_CLOSE_H3));

                        item.setDescription(builder.toString());
                        item.setIsFullDescription(true);

                        if (!emitter.isDisposed()) emitter.onSuccess(item);
                    },
                    error -> {
                        if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), "Error URL = " + item.getLink(), RxJava2Debug.getEnhancedStackTrace(error));

                        if (!emitter.isDisposed()) emitter.onError(error);
                    }
                );
        });
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @Nullable
    private Video extractVideo(@NonNull final String url, @Nullable final String videoId) {
        if (videoId == null) return null;

        final String[] ids      = url.split(AppleDailyClient.SLASH);
        final String   category = ids[ids.length - 4].replace("news", "local").replace("international", "chinainternational").replace("financeestate", "finance");

        if (ids.length > 4) {
            final JSONArray items = this.apiService
                .getHtml("https://hk.video.appledaily.com/video/videoplayer/" + ids[ids.length - 2] + AppleDailyClient.SLASH + category + AppleDailyClient.SLASH + category + AppleDailyClient.SLASH + ids[ids.length - 1] + AppleDailyClient.SLASH + videoId + "/0/0/0?ts=" + String.valueOf(System.currentTimeMillis() / 1000L))
                .compose(RxUtils.applyObservableBackgroundSchedulers())
                .map(html -> new JSONArray(StringUtils.substringBetween(html, "window.videoPlaylistOriginal = ", "];") + "]"))
                .blockingSingle();

            for (int i = 0; i < items.length(); i++) {
                try {
                    final JSONObject item = items.getJSONObject(i);

                    if (videoId.equals(item.getString("video_id"))) return new Video(item.getString("video"), item.getString("image_zoom"));
                } catch (final JSONException e) {
                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), RxJava2Debug.getEnhancedStackTrace(e));
                }
            }
        }

        return null;
    }
}
