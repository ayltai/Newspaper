package com.github.ayltai.newspaper.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.model.Image;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.net.HttpClient;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import rx.Emitter;
import rx.Observable;

final class AppleDailyClient extends Client {
    //region Constants

    private static final long SECOND = 1000;

    private static final String TAG_QUOTE    = "\"";
    private static final String TAG_OPEN_H2  = "<h2>";
    private static final String TAG_CLOSE_H2 = "</h2>";
    private static final String TAG_OPEN_H3  = "<h3>";
    private static final String TAG_CLOSE_H3 = "</h3>";

    //endregion

    @Inject
    AppleDailyClient(@NonNull final HttpClient client, @Nullable final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    public Observable<List<Item>> getItems(@NonNull final String url) {
        return Observable.create(emitter -> {
            try {
                final String html = IOUtils.toString(this.client.download(url), Client.ENCODING);

                if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "URL = " + url);

                final String[]   sections     = StringUtils.substringsBetween(StringUtils.substringBetween(html, "<div class=\"itemContainer\">", "<div class=\"clear\"></div>"), "div class=\"item\">", "</div>");
                final List<Item> items        = new ArrayList<>(sections.length);
                final String     categoryName = this.getCategoryName(url);

                for (final String section : sections) {
                    if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "Item = " + section);

                    final Item   item = new Item();
                    final String link = StringUtils.substringBetween(section, "href=\"", AppleDailyClient.TAG_QUOTE);

                    if (link != null) {
                        item.setTitle(StringUtils.substringBetween(section, "title=\"", AppleDailyClient.TAG_QUOTE));
                        item.setLink(link.substring(0, link.lastIndexOf("/")).replace("dv", "apple").replace("actionnews", "news").replace("local", "art").replace("chinainternational", "art").replace("finance", "art").replace("entertainmnt", "art").replace("sports", "art"));
                        item.setSource(this.source.getName());
                        item.setCategory(categoryName);

                        final String image = StringUtils.substringBetween(section, "<img src=\"", AppleDailyClient.TAG_QUOTE);
                        if (image != null) item.getImages().add(new Image(image));

                        final String time = StringUtils.substringBetween(section, "pix/", "_");
                        if (time != null) item.setPublishDate(new Date(Long.valueOf(time) * SECOND));

                        items.add(item);
                    }
                }

                emitter.onNext(items);
            } catch (final IOException e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.BUFFER);
    }

    @NonNull
    @Override
    public Observable<Item> updateItem(@NonNull final Item item) {
        return Observable.create(emitter -> {
            try {
                final String html = StringUtils.substringBetween(IOUtils.toString(this.client.download(item.getLink()), Client.ENCODING), "!-- START ARTILCLE CONTENT -->", "<!-- END ARTILCLE CONTENT -->");

                if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "URL = " + item.getLink());

                final String[]    imageContainers = StringUtils.substringsBetween(html, "rel=\"fancybox-button\"", "/>");
                final List<Image> images          = new ArrayList<>();

                for (final String imageContainer : imageContainers) {
                    final String imageUrl         = StringUtils.substringBetween(imageContainer, "href=\"", AppleDailyClient.TAG_QUOTE);
                    final String imageDescription = StringUtils.substringBetween(imageContainer, "title=\"", AppleDailyClient.TAG_QUOTE);

                    if (imageUrl != null) images.add(new Image(imageUrl, imageDescription));
                }

                if (!images.isEmpty()) {
                    item.getImages().clear();
                    item.getImages().addAll(images);
                }

                final String[]      contents = StringUtils.substringsBetween(html, "<div class=\"ArticleContent_Inner\">", "</div>");
                final StringBuilder builder  = new StringBuilder();

                for (final String content : contents) builder.append(content.replace(TAG_OPEN_H2, TAG_OPEN_H3).replace(TAG_CLOSE_H2, TAG_CLOSE_H3));

                item.setDescription(builder.toString());
                item.setIsFullDescription(true);

                emitter.onNext(item);
            } catch (final IOException e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.BUFFER);
    }
}
