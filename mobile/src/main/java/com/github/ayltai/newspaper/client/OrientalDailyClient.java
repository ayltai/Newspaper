package com.github.ayltai.newspaper.client;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.github.ayltai.newspaper.data.model.Image;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.data.model.Source;
import com.github.ayltai.newspaper.data.model.Video;
import com.github.ayltai.newspaper.net.ApiService;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.Maybe;
import okhttp3.OkHttpClient;

final class OrientalDailyClient extends RssClient {
    //region Constants

    private static final String BASE_URI  = "http://orientaldaily.on.cc";
    private static final String TAG_CLOSE = "\"";
    private static final String SLASH     = "/";

    //endregion

    @Inject
    OrientalDailyClient(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
        super(client, apiService, source);
    }

    @WorkerThread
    @NonNull
    @Override
    public Maybe<Item> updateItem(@NonNull final Item item) {
        return Maybe.create(emitter -> this.apiService
            .getHtml(item.getLink())
            .compose(RxUtils.applyObservableBackgroundSchedulers())
            .subscribe(
                html -> {
                    html = StringUtils.substringBetween(html, "<div id=\"contentCTN-top\"", "<p><!--AD-->");

                    final String[]    imageContainers = StringUtils.substringsBetween(html, "<div class=\"photo", "</div>");
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

                    emitter.onSuccess(item);
                },
                error -> {
                    if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);

                    emitter.onError(error);
                }
            ));
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @WorkerThread
    @Nullable
    private Video extractVideo(@NonNull final String url) {
        final String date = StringUtils.substringBetween(url, "http://orientaldaily.on.cc/cnt/news/", OrientalDailyClient.SLASH);
        if (date == null) return null;

        final String videoList = this.apiService.getHtml("http://orientaldaily.on.cc/cnt/keyinfo/" + date + "/videolist.xml").blockingSingle();
        if (videoList == null) return null;

        for (final String video : StringUtils.substringsBetween(videoList, "<news>", "</news>")) {
            if (("odn-" + date + "-" + date.substring(4) + "_" + StringUtils.substringBetween(url, date + OrientalDailyClient.SLASH, ".html")).equals(StringUtils.substringBetween(video, "<articleID>", "</articleID>"))) {
                final String thumbnailUrl = StringUtils.substringBetween(video, "<thumbnail>", "</thumbnail>");

                if (thumbnailUrl != null) {
                    final String videoUrl = thumbnailUrl.replace(".jpg", "_ipad.mp4");

                    return new Video("http://video.cdn.on.cc/Video/" + date.substring(0, 6) + OrientalDailyClient.SLASH + videoUrl, "http://tv.on.cc/xml/Thumbnail/" + date.substring(0, 6) + "/bigthumbnail/" + thumbnailUrl);
                }
            }
        }

        return null;
    }
}
