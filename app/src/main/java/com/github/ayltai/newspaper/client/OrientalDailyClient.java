package com.github.ayltai.newspaper.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import com.github.ayltai.newspaper.client.rss.RssClient;
import com.github.ayltai.newspaper.model.Image;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.model.Video;
import com.github.ayltai.newspaper.net.HttpClient;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import io.reactivex.Maybe;

final class OrientalDailyClient extends RssClient {
    //region Constants

    private static final String BASE_URI  = "http://orientaldaily.on.cc";
    private static final String TAG_CLOSE = "\"";
    private static final String SLASH     = "/";

    //endregion

    @Inject
    OrientalDailyClient(@NonNull final HttpClient client, @Nullable final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    public Maybe<Item> updateItem(@NonNull final Item item) {
        return Maybe.create(emitter -> {
            try {
                final String      html            = StringUtils.substringBetween(IOUtils.toString(this.client.download(item.getLink()), Client.ENCODING), "<div id=\"contentCTN-top\"", "<p><!--AD-->");
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
            } catch (final IOException e) {
                this.handleError(emitter, e);
            }
        });
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @Nullable
    private Video extractVideo(@NonNull final String url) {
        try {
            final String date      = StringUtils.substringBetween(url, "http://orientaldaily.on.cc/cnt/news/", OrientalDailyClient.SLASH);
            final String videoList = IOUtils.toString(this.client.download("http://orientaldaily.on.cc/cnt/keyinfo/" + date + "/videolist.xml"), Client.ENCODING);

            if (videoList == null) return null;

            final String[] videos = StringUtils.substringsBetween(videoList, "<news>", "</news>");

            for (final String video : videos) {
                if (("odn-" + date + "-" + date.substring(4) + "_" + StringUtils.substringBetween(url, date + OrientalDailyClient.SLASH, ".html")).equals(StringUtils.substringBetween(video, "<articleID>", "</articleID>"))) {
                    final String thumbnailUrl = StringUtils.substringBetween(video, "<thumbnail>", "</thumbnail>");

                    if (thumbnailUrl != null) {
                        final String videoUrl = thumbnailUrl.replace(".jpg", "_ipad.mp4");

                        return new Video("http://video.cdn.on.cc/Video/" + date.substring(0, 6) + OrientalDailyClient.SLASH + videoUrl, "http://tv.on.cc/xml/Thumbnail/" + date.substring(0, 6) + "/bigthumbnail/" + thumbnailUrl);
                    }
                }
            }
        } catch (final IOException e) {
            LogUtils.getInstance().e(this.getClass().getSimpleName(), e.getMessage(), e);
        }

        return null;
    }
}
