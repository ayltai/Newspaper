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

    private final Map<String, Client> clients = new HashMap<>(10);
    private final HttpClient          client;

    @NonNull
    public static ClientFactory getInstance(@NonNull final Context context) {
        if (ClientFactory.instance == null) ClientFactory.instance = new ClientFactory(context);

        return ClientFactory.instance;
    }

    public ClientFactory(@NonNull final Context context) {
        this.client = this.createHttpClient(context);

        final String[] sources = context.getResources().getStringArray(R.array.sources);

        this.clients.put(sources[0], new AppleDailyClient(client, SourceFactory.getInstance(context).getSource(sources[0])));
        this.clients.put(sources[1], new OrientalDailyClient(client, SourceFactory.getInstance(context).getSource(sources[1])));
        this.clients.put(sources[2], new SingTaoClient(client, SourceFactory.getInstance(context).getSource(sources[2])));
        this.clients.put(sources[3], new HketClient(client, SourceFactory.getInstance(context).getSource(sources[3])));
        this.clients.put(sources[4], new SingPaoClient(client, SourceFactory.getInstance(context).getSource(sources[4])));
        this.clients.put(sources[5], new MingPaoClient(client, SourceFactory.getInstance(context).getSource(sources[5])));
        this.clients.put(sources[6], new HeadlineClient(client, SourceFactory.getInstance(context).getSource(sources[6])));
        this.clients.put(sources[7], new SkyPostClient(client, SourceFactory.getInstance(context).getSource(sources[7])));
        this.clients.put(sources[8], new HkejClient(client, SourceFactory.getInstance(context).getSource(sources[8])));
        this.clients.put(sources[9], new RthkClient(client, SourceFactory.getInstance(context).getSource(sources[9])));
    }

    @Nullable
    public Client getClient(@NonNull final String source) {
        return this.clients.get(source);
    }

    @NonNull
    private HttpClient createHttpClient(@NonNull final Context context) {
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
