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

import static com.github.ayltai.newspaper.util.StringUtils.substringBetween;

final class OrientalDailyClient extends RssClient {
    //region Constants

    private static final String BASE_URI  = "http://orientaldaily.on.cc";
    private static final String TAG_CLOSE = "\"";

    //endregion

    @Inject
    OrientalDailyClient(@NonNull final HttpClient client, @Nullable final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    public Observable<Item> updateItem(@NonNull final Item item) {
        return Observable.create(emitter -> {
            try {
                final String html = substringBetween(IOUtils.toString(this.client.download(item.getLink()), Client.ENCODING), "<div id=\"contentCTN-top\"", "<p><!--AD-->");

                if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "URL = " + item.getLink());

                final String[] imageContainers = StringUtils.substringsBetween(html, "<div class=\"photo", "</div>");

                for (final String imageContainer : imageContainers) {
                    final String imageUrl         = StringUtils.substringBetween(imageContainer, "href=\"", OrientalDailyClient.TAG_CLOSE);
                    final String imageDescription = StringUtils.substringBetween(imageContainer, "title=\"", OrientalDailyClient.TAG_CLOSE);

                    if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "Image URL = " + imageUrl);
                    if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "Image Description = " + imageDescription);

                    if (imageUrl != null) item.getImages().add(new Image(OrientalDailyClient.BASE_URI + imageUrl, imageDescription));
                }

                final String[]      contents = StringUtils.substringsBetween(html, "<p>", "</p>");
                final StringBuilder builder  = new StringBuilder();

                for (final String content : contents) builder.append(content).append("<br><br>");

                item.setDescription(builder.toString());
                item.setIsFullDescription(true);

                emitter.onNext(item);
            } catch (final IOException e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.BUFFER);
    }
}
