package com.github.ayltai.newspaper.client;

import java.io.IOException;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.client.rss.RssClient;
import com.github.ayltai.newspaper.model.Image;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.net.HttpClient;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import io.reactivex.Single;

final class RthkClient extends RssClient {
    //region Constants

    private static final String TAG_CLOSE = "</div>";
    private static final String TAG_QUOTE = "\"";

    //endregion

    @Inject
    RthkClient(@NonNull final HttpClient client, @Nullable final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    public Single<Item> updateItem(@NonNull final Item item) {
        return Single.create(emitter -> {
            if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), item.getLink());

            try {
                final String html           = IOUtils.toString(this.client.download(item.getLink()), Client.ENCODING);
                final String imageContainer = StringUtils.substringBetween(html, "<div class=\"itemSlideShow\">", "<div class=\"clr\"></div>");

                if (imageContainer != null) {
                    final String imageUrl         = StringUtils.substringBetween(imageContainer, "<a href=\"", RthkClient.TAG_QUOTE);
                    final String imageDescription = StringUtils.substringBetween(imageContainer, "alt=\"", RthkClient.TAG_QUOTE);

                    if (imageUrl != null) {
                        item.getImages().clear();
                        item.getImages().add(new Image(imageUrl, imageDescription));
                    }

                    final String videoUrl         = StringUtils.substringBetween(imageContainer, "var videoThumbnail\t\t= '", "'");
                    final String videoDescription = StringUtils.substringBetween(imageContainer, "div class='detailNewsSlideTitleText'>", RthkClient.TAG_CLOSE);

                    if (videoUrl != null) item.getImages().add(new Image(videoUrl, videoDescription));
                }

                item.setDescription(StringUtils.substringBetween(html, "<div class=\"itemFullText\">", RthkClient.TAG_CLOSE));
                item.setIsFullDescription(true);

                emitter.onSuccess(item);
            } catch (final IOException e) {
                emitter.onError(e);
            }
        });
    }
}
