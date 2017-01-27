package com.github.ayltai.newspaper.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.util.TestUtils;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public final class HttpClient extends BaseHttpClient implements Closeable {
    private final List<Call> calls = new ArrayList<>();

    private final Context context;

    @Inject
    public HttpClient(@Nullable final Context context) {
        super();

        this.context = context;
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
        if (TestUtils.isRunningTest()) return this.mockDownload(url);

        final Call call = this.client.newCall(new Request.Builder().url(url).build());
        this.calls.add(call);

        final Response response = call.execute();

        if (response.isSuccessful()) {
            return response.body().byteStream();
        } else {
            throw new IOException("Unexpected response " + response);
        }
    }

    private InputStream mockDownload(@NonNull final String url) throws IOException {
        if (this.context == null) return null;

        return this.context.getResources().openRawResource(BaseHttpClient.ASSETS.get(url));
    }
}
