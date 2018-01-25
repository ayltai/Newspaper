package com.github.ayltai.newspaper.client;

import java.util.Map;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.data.model.SourceFactory;
import com.github.ayltai.newspaper.net.ApiService;
import com.github.ayltai.newspaper.net.DaggerHttpComponent;
import com.github.ayltai.newspaper.net.HttpComponent;

import gnu.trove.map.hash.THashMap;
import okhttp3.OkHttpClient;

public final class ClientFactory {
    private static ClientFactory instance;

    private final Map<String, Client> clients = new THashMap<>(15);

    @NonNull
    public static ClientFactory getInstance(@NonNull final Context context) {
        if (ClientFactory.instance == null) ClientFactory.instance = new ClientFactory(context);

        return ClientFactory.instance;
    }

    private ClientFactory(@NonNull final Context context) {
        final HttpComponent httpComponent = DaggerHttpComponent.builder().build();
        final OkHttpClient  client        = httpComponent.httpClient();
        final ApiService    apiService    = httpComponent.apiService();
        final String[]      sources       = context.getResources().getStringArray(R.array.sources);

        int i = 0;

        this.clients.put(sources[i], new AppleDailyClient(client, apiService, SourceFactory.getInstance(context).getSource(sources[i++])));
        this.clients.put(sources[i], new OrientalDailyClient(client, apiService, SourceFactory.getInstance(context).getSource(sources[i++])));
        this.clients.put(sources[i], new SingTaoClient(client, apiService, SourceFactory.getInstance(context).getSource(sources[i++])));
        this.clients.put(sources[i], new SingTaoRealtimeClient(client, apiService, SourceFactory.getInstance(context).getSource(sources[i++])));
        this.clients.put(sources[i], new HketClient(client, apiService, SourceFactory.getInstance(context).getSource(sources[i++])));
        this.clients.put(sources[i], new SingPaoClient(client, apiService, SourceFactory.getInstance(context).getSource(sources[i++])));
        this.clients.put(sources[i], new MingPaoClient(client, apiService, SourceFactory.getInstance(context).getSource(sources[i++])));
        this.clients.put(sources[i], new HeadlineClient(client, apiService, SourceFactory.getInstance(context).getSource(sources[i++])));
        this.clients.put(sources[i], new HeadlineRealtimeClient(client, apiService, SourceFactory.getInstance(context).getSource(sources[i++])));
        this.clients.put(sources[i], new SkyPostClient(client, apiService, SourceFactory.getInstance(context).getSource(sources[i++])));
        this.clients.put(sources[i], new HkejClient(client, apiService, SourceFactory.getInstance(context).getSource(sources[i++])));
        this.clients.put(sources[i], new RthkClient(client, apiService, SourceFactory.getInstance(context).getSource(sources[i++])));
        this.clients.put(sources[i], new ScmpClient(client, apiService, SourceFactory.getInstance(context).getSource(sources[i++])));
        this.clients.put(sources[i], new TheStandardClient(client, apiService, SourceFactory.getInstance(context).getSource(sources[i++])));
        this.clients.put(sources[i], new WenWeiPoClient(client, apiService, SourceFactory.getInstance(context).getSource(sources[i])));
    }

    @Nullable
    public Client getClient(@NonNull final String source) {
        return this.clients.get(source);
    }
}
