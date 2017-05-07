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

import rx.Emitter;
import rx.Observable;

final class SkyPostClient extends RssClient {
    //region Constants

    private static final String TAG_OPEN_HEADER  = "<h3>";
    private static final String TAG_CLOSE_HEADER = "</h3>";
    private static final String TAG_BREAK        = "<br>";
    private static final String TAG_OPEN_TITLE   = "<h4>";
    private static final String TAG_CLOSE_TITLE  = "</h4>";

    //endregion

    @Inject
    SkyPostClient(@NonNull final HttpClient client, @Nullable final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    public Observable<Item> updateItem(@NonNull final Item item) {
        return Observable.create(emitter -> {
            try {
                final String html = StringUtils.substringBetween(IOUtils.toString(this.client.download(item.getLink()), Client.ENCODING), "<div class=\"article-title-widget\">", "<div class=\"article-detail_extra-info\">");

                if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "URL = " + item.getLink());

                final String   headline        = StringUtils.substringBetween(html, "<h3 class=\"article-details__main-headline\">", SkyPostClient.TAG_CLOSE_HEADER);
                final String   subHeadline     = StringUtils.substringBetween(html, "<h3 class=\"article-details__lower-headline\">", SkyPostClient.TAG_CLOSE_HEADER);
                final String[] contents        = StringUtils.substringsBetween(html, "<P>", "</P>");
                final String[] imageContainers = StringUtils.substringsBetween(html, "<div class=\"article-detail__img-container\">", "</div>");

                for (final String imageContainer : imageContainers) {
                    final String imageUrl         = StringUtils.substringBetween(imageContainer, "data-src=\"", "\"");
                    final String imageDescription = StringUtils.substringBetween(imageContainer, "<p class=\"article-detail__img-caption\">", "</p>");

                    if (imageUrl != null) item.getImages().add(new Image(imageUrl, imageDescription));
                }

                final StringBuilder builder = new StringBuilder();
                builder.append(SkyPostClient.TAG_OPEN_HEADER).append(headline).append(SkyPostClient.TAG_CLOSE_HEADER).append(SkyPostClient.TAG_BREAK);

                if (subHeadline != null) builder.append(SkyPostClient.TAG_OPEN_TITLE).append(subHeadline).append(SkyPostClient.TAG_CLOSE_TITLE).append(SkyPostClient.TAG_BREAK);

                for (final String content : contents) {
                    final String text = StringUtils.substringBetween(content, "<b>", "</b>");

                    if (text != null) {
                        builder.append(SkyPostClient.TAG_OPEN_TITLE).append(text).append(SkyPostClient.TAG_CLOSE_TITLE);
                    } else {
                        builder.append(content).append(SkyPostClient.TAG_BREAK);
                    }
                }

                item.setDescription(builder.toString());
                item.setIsFullDescription(true);

                emitter.onNext(item);
            } catch (final IOException e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.BUFFER);
    }
}
