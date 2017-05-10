package com.github.ayltai.newspaper.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

final class HketClient extends RssClient {
    private static final String TAG_QUOTE = "\"";

    @Inject
    HketClient(@NonNull final HttpClient client, @Nullable final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    public Single<Item> updateItem(@NonNull final Item item) {
        return Single.create(emitter -> {
            if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), item.getLink());

            try {
                final String      html            = StringUtils.substringBetween(IOUtils.toString(this.client.download(item.getLink()), Client.ENCODING), "<div class=\"article-detail\">", "<div class=\"article-detail_facebook-like\">");
                final String[]    imageContainers = StringUtils.substringsBetween(html, "<img ", "/>");
                final List<Image> images          = new ArrayList<>();

                for (final String imageContainer : imageContainers) {
                    final String imageUrl         = StringUtils.substringBetween(imageContainer, "data-src=\"",HketClient. TAG_QUOTE);
                    final String imageDescription = StringUtils.substringBetween(imageContainer, "alt=\"", HketClient.TAG_QUOTE);

                    if (imageUrl != null) images.add(new Image(imageUrl, imageDescription));
                }

                if (!images.isEmpty()) {
                    item.getImages().clear();
                    item.getImages().addAll(images);
                }

                final String[]      contents = StringUtils.substringsBetween(html, "<p>", "</p>");
                final StringBuilder builder  = new StringBuilder();

                for (final String content : contents) builder.append(content).append("<br>");

                item.setDescription(builder.toString());
                item.setIsFullDescription(true);

                emitter.onSuccess(item);
            } catch (final IOException e) {
                emitter.onError(e);
            }
        });
    }
}
