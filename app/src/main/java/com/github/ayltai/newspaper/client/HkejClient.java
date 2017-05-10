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

final class HkejClient extends RssClient {
    //region Constants

    private static final String TAG_OPEN  = "<a href=\"";
    private static final String TAG_QUOTE = "\"";

    //endregion

    @Inject
    HkejClient(@NonNull final HttpClient client, @Nullable final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    public Observable<Item> updateItem(@NonNull final Item item) {
        return Observable.create(emitter -> {
            try {
                final String html = IOUtils.toString(this.client.download(item.getLink()), Client.ENCODING);

                if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "URL = " + item.getLink());

                final String imageContainer   = StringUtils.substringBetween(html, "<span class='enlargeImg'>", "</span>");
                final String imageUrl         = StringUtils.substringBetween(imageContainer, HkejClient.TAG_OPEN, HkejClient.TAG_QUOTE);
                final String imageDescription = StringUtils.substringBetween(imageContainer, "title=\"", HkejClient.TAG_QUOTE);

                if (imageUrl != null) {
                    item.getImages().clear();
                    item.getImages().add(new Image(imageUrl, imageDescription));
                }

                final String[]      contents = StringUtils.substringsBetween(StringUtils.substringBetween(html, "<div id='article-content'>", "</div>"), ">", "<");
                final StringBuilder builder  = new StringBuilder();

                for (final String content : contents) builder.append(content).append("<br>");

                item.setDescription(builder.toString());
                item.setIsFullDescription(true);

                emitter.onNext(item);
            } catch (final IOException e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.BUFFER);
    }
}
