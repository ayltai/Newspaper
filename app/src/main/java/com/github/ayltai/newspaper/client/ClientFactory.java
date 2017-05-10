package com.github.ayltai.newspaper.client;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.ContextModule;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.model.SourceFactory;
import com.github.ayltai.newspaper.net.DaggerNetComponent;
import com.github.ayltai.newspaper.net.HttpClient;
import com.github.ayltai.newspaper.net.NetModule;

public final class ClientFactory implements Closeable {
    private static ClientFactory instance;

    private final Map<String, Client> clients = new HashMap<>(12);
    private final HttpClient          client;

    @NonNull
    public static ClientFactory getInstance(@NonNull final Context context) {
        if (ClientFactory.instance == null) ClientFactory.instance = new ClientFactory(context);

        return ClientFactory.instance;
    }

    private ClientFactory(@NonNull final Context context) {
        this.client = ClientFactory.createHttpClient(context);

        final String[] sources = context.getResources().getStringArray(R.array.sources);

        int i = 0;

        this.clients.put(sources[i], new AppleDailyClient(this.client, SourceFactory.getInstance(context).getSource(sources[i])));
        i++;

        this.clients.put(sources[i], new OrientalDailyClient(this.client, SourceFactory.getInstance(context).getSource(sources[i])));
        i++;

        this.clients.put(sources[i], new SingTaoClient(this.client, SourceFactory.getInstance(context).getSource(sources[i])));
        i++;

        this.clients.put(sources[i], new SingTaoRealtimeClient(this.client, SourceFactory.getInstance(context).getSource(sources[i])));
        i++;

        this.clients.put(sources[i], new HketClient(this.client, SourceFactory.getInstance(context).getSource(sources[i])));
        i++;

        this.clients.put(sources[i], new SingPaoClient(this.client, SourceFactory.getInstance(context).getSource(sources[i])));
        i++;

        this.clients.put(sources[i], new MingPaoClient(this.client, SourceFactory.getInstance(context).getSource(sources[i])));
        i++;

        this.clients.put(sources[i], new HeadlineClient(this.client, SourceFactory.getInstance(context).getSource(sources[i])));
        i++;

        this.clients.put(sources[i], new HeadlineRealtimeClient(this.client, SourceFactory.getInstance(context).getSource(sources[i])));
        i++;

        this.clients.put(sources[i], new SkyPostClient(this.client, SourceFactory.getInstance(context).getSource(sources[i])));
        i++;

        this.clients.put(sources[i], new HkejClient(this.client, SourceFactory.getInstance(context).getSource(sources[i])));
        i++;

        this.clients.put(sources[i], new RthkClient(this.client, SourceFactory.getInstance(context).getSource(sources[i])));
    }

    @Nullable
    public Client getClient(@NonNull final String source) {
        return this.clients.get(source);
    }

    @NonNull
    private static HttpClient createHttpClient(@NonNull final Context context) {
        return DaggerNetComponent.builder()
            .contextModule(new ContextModule(context))
            .netModule(new NetModule())
            .build()
            .httpClient();
    }

    @Override
    public void close() {
        this.client.close();
    }
}
