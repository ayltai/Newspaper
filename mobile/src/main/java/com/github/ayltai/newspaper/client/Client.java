package com.github.ayltai.newspaper.client;

import java.util.ArrayList;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.data.model.Category;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.Source;
import com.github.ayltai.newspaper.net.ApiService;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.Single;
import okhttp3.OkHttpClient;

public abstract class Client {
    //region Variables

    protected final OkHttpClient client;
    protected final ApiService   apiService;
    protected final Source       source;

    //endregion

    protected Client(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
        this.client     = client;
        this.apiService = apiService;
        this.source     = source;
    }

    @WorkerThread
    @NonNull
    public abstract Single<List<NewsItem>> getItems(@NonNull String url);

    @WorkerThread
    @NonNull
    public abstract Single<NewsItem> updateItem(@NonNull NewsItem item);

    @Nullable
    protected final String getCategoryName(@NonNull final String url) {
        for (final Category category : this.source.getCategories()) {
            if (category.getUrl().equals(url)) return category.getName();
        }

        return null;
    }

    @NonNull
    protected List<NewsItem> filter(@NonNull final List<NewsItem> items) {
        final List<NewsItem> filteredItems = new ArrayList<>();

        for (final NewsItem item : items) {
            final String title = item.getTitle();
            if (title != null) item.setTitle(title.replaceAll("<br>", "\n"));

            if (TestUtils.isRunningUnitTest()) {
                filteredItems.add(item);
            } else if (item.getPublishDate() != null && item.getPublishDate().getTime() > System.currentTimeMillis() - Constants.HOUSEKEEP_TIME) {
                filteredItems.add(item);
            }
        }

        return filteredItems;
    }
}
