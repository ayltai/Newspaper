package com.github.ayltai.newspaper.client.rss;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import android.support.annotation.NonNull;

import org.apache.commons.io.IOUtils;

import org.xmlpull.v1.XmlPullParserException;

import com.github.ayltai.newspaper.client.Client;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.net.HttpClient;

import io.realm.RealmList;
import rx.Emitter;
import rx.Observable;

public abstract class RssClient extends Client {
    //@Inject
    protected RssClient(@NonNull final HttpClient client, @NonNull final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    public final Observable<List<Item>> getItems(@NonNull final String url) {
        return Observable.create(emitter -> {
            InputStream inputStream = null;

            try {
                final RealmList<Item> items = new RealmList<>(Parser.parse(inputStream = this.client.download(url)).toArray(new Item[0]));

                for (final Item item : items) item.setSource(this.source.getName());
                Collections.sort(items);

                emitter.onNext(items);
            } catch (final XmlPullParserException | IOException e) {
                emitter.onError(e);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }, Emitter.BackpressureMode.BUFFER);
    }
}
