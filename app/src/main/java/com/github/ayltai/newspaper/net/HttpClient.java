package com.github.ayltai.newspaper.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.support.annotation.NonNull;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public final class HttpClient extends BaseHttpClient implements Closeable {
    private final List<Call> calls = new ArrayList<>();

    public HttpClient() {
        super();
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

    public InputStream download(@NonNull final String url) throws IOException {
        final Call call = this.client.newCall(new Request.Builder().url(url).build());
        this.calls.add(call);

        final Response response = call.execute();

        if (response.isSuccessful()) {
            return response.body().byteStream();
        } else {
            throw new IOException("Unexpected response " + response);
        }
    }
}
