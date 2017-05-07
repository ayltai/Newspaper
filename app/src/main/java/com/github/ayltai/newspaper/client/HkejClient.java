package com.github.ayltai.newspaper.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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

final class HkejClient extends Client {
    //region Constants

    private static final String BASE_URI  = "http://www1.hkej.com";
    private static final String TAG_OPEN  = "<a href=\"";
    private static final String TAG_CLOSE = "\">";

    //endregion

    @Inject
    HkejClient(@NonNull final HttpClient client, @Nullable final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    public Observable<List<Item>> getItems(@NonNull final String url) {
        return Observable.create(emitter -> {
            try {
                final String html = IOUtils.toString(this.client.download(url), Client.ENCODING);

                if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "URL = " + url);

                final String[]   sections = StringUtils.substringsBetween(html, "<div class=\"news", "全文</a></p>");
                final List<Item> items    = new ArrayList<>(sections.length);

                final Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.ZONE_OFFSET, 8);
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                final Date   publishDate  = calendar.getTime();
                final String categoryName = this.getCategoryName(url);

                for (final String section : sections) {
                    if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "Item = " + section);

                    final Item   item  = new Item();
                    final String title = StringUtils.substringBetween(section, "<h2>", "</h2>");

                    item.setTitle(StringUtils.substringBetween(title, HkejClient.TAG_CLOSE, "</a>"));
                    item.setLink(HkejClient.BASE_URI + StringUtils.substringBetween(title, HkejClient.TAG_OPEN, HkejClient.TAG_CLOSE));
                    item.setDescription(StringUtils.substringBetween(section, "<p class=\"recap\">", HkejClient.TAG_OPEN));
                    item.setPublishDate(publishDate);
                    item.setSource(this.source.getName());
                    item.setCategory(categoryName);

                    if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "Title = " + item.getTitle());
                    if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "Link = " + item.getLink());
                    if (BuildConfig.DEBUG) LogUtils.getInstance().d(this.getClass().getSimpleName(), "Description = " + item.getDescription());

                    final String image = StringUtils.substringBetween(section, "<img src=\"", "\" />");
                    if (image != null) item.getImages().add(new Image(image));

                    items.add(item);
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
        return null;
    }
}
