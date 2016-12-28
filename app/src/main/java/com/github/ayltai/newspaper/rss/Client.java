package com.github.ayltai.newspaper.rss;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import com.github.ayltai.newspaper.data.Feed;

import io.realm.RealmList;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;

public final class Client implements Closeable {
    //region Constants

    private static final long CONNECT_TIMEOUT = 60;
    private static final long READ_TIMEOUT    = 60;
    private static final long WRITE_TIMEOUT   = 60;

    //endregion

    private final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(Client.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(Client.READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(Client.WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build();

    private final List<Call> calls = new ArrayList<>();

    public Observable<Feed> get(@NonNull final String url) {
        return Observable.create(subscriber -> {
            InputStream inputStream = null;

            try {
                final RealmList<Item> items = new RealmList<>(Parser.parse(inputStream = this.download(url)).toArray(new Item[0]));
                Collections.sort(items);

                subscriber.onNext(new Feed(url, items));
            } catch (final XmlPullParserException | IOException e) {
                subscriber.onError(e);
            } finally {
                Client.closeQuietly(inputStream);
            }
        });
    }

    @Override
    public void close() {
        synchronized (this.calls) {
            for (final Call call : this.calls) {
                if (!call.isCanceled()) call.cancel();
            }

            this.calls.clear();
        }
    }

    private InputStream download(@NonNull final String url) throws IOException {
        final Call call = this.client.newCall(new Request.Builder().url(url).build());
        this.calls.add(call);

        final Response response = call.execute();

        try {
            if (response.isSuccessful()) {
                return response.body().byteStream();
            } else {
                throw new IOException("Unexpected response " + response);
            }
        } finally {
            this.calls.remove(call);
        }
    }

    private static void closeQuietly(@Nullable final InputStream inputStream) {
        try {
            if (inputStream != null) inputStream.close();
        } catch (final IOException e) {
            Log.e(Client.class.getName(), e.getMessage(), e);
        }
    }
}
