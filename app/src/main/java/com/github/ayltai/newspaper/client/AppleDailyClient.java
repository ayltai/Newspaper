package com.github.ayltai.newspaper.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.model.Image;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.model.Video;
import com.github.ayltai.newspaper.net.HttpClient;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import io.reactivex.Maybe;
import io.reactivex.Single;

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
    AppleDailyClient(@NonNull final HttpClient client, @Nullable final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    public Single<List<Item>> getItems(@NonNull final String url) {
        return Single.create(emitter -> {
            try {
                final InputStream inputStream = this.client.download(url);

                if (inputStream == null) {
                    emitter.onSuccess(Collections.emptyList());
                } else {
                    final String     html         = IOUtils.toString(inputStream, Client.ENCODING);
                    final String[]   sections     = StringUtils.substringsBetween(StringUtils.substringBetween(html, "<div class=\"itemContainer\">", "<div class=\"clear\"></div>"), "div class=\"item\">", AppleDailyClient.TAG_DIV);
                    final List<Item> items        = new ArrayList<>(sections.length);
                    final String     categoryName = this.getCategoryName(url);

                    for (final String section : sections) {
                        final Item   item = new Item();
                        final String link = StringUtils.substringBetween(section, AppleDailyClient.TAG_HREF, AppleDailyClient.TAG_QUOTE);

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
                            item.setCategory(categoryName);

                            final String image = StringUtils.substringBetween(section, "<img src=\"", AppleDailyClient.TAG_QUOTE);
                            if (image != null) item.getImages().add(new Image(image));

                            final String time = StringUtils.substringBetween(section, "pix/", "_");
                            if (time != null) item.setPublishDate(new Date(Long.valueOf(time) * AppleDailyClient.SECOND));

                            items.add(item);
                        }
                    }

                    emitter.onSuccess(this.filters(url, items));
                }
            } catch (final IOException e) {
                this.handleError(emitter, e);
            }
        });
    }

    @NonNull
    @Override
    public Maybe<Item> updateItem(@NonNull final Item item) {
        return Maybe.create(emitter -> {
            if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), item.getLink());

            try {
                final String      fullHtml        = IOUtils.toString(this.client.download(item.getLink()), Client.ENCODING);
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

                emitter.onSuccess(item);
            } catch (final IOException e) {
                this.handleError(emitter, e);
            }
        });
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @Nullable
    private Video extractVideo(@NonNull final String url, @Nullable final String videoId) {
        if (videoId == null) return null;

        final String[] ids      = url.split(AppleDailyClient.SLASH);
        final String   category = ids[ids.length - 4].replace("news", "local").replace("international", "chinainternational").replace("financeestate", "finance");

        if (ids.length > 4) {
            try {
                final JSONArray items = new JSONArray(StringUtils.substringBetween(IOUtils.toString(this.client.download("http://hk.dv.nextmedia.com/video/videoplayer/" + ids[ids.length - 2] + AppleDailyClient.SLASH + category + AppleDailyClient.SLASH + category + AppleDailyClient.SLASH + ids[ids.length - 1] + AppleDailyClient.SLASH + videoId + "/0/0/0?ts=" + String.valueOf(System.currentTimeMillis() / 1000L)), Client.ENCODING), "window.videoPlaylistOriginal = ", "];") + "]");

                for (int i = 0; i < items.length(); i++) {
                    final JSONObject item = items.getJSONObject(i);

                    if (videoId.equals(item.getString("video_id"))) return new Video(item.getString("video"), item.getString("image_zoom"));
                }
            } catch (final IOException | JSONException e) {
                LogUtils.getInstance().e(this.getClass().getSimpleName(), e.getMessage(), e);
            }
        }

        return null;
    }
}
