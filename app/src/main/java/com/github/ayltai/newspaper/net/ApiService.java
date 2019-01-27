package com.github.ayltai.newspaper.net;

import java.util.List;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;

import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.data.model.Source;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @Nonnull
    @NonNull
    @GET("sources")
    Single<List<Source>> sources();

    @Nonnull
    @NonNull
    @GET("{sourceNames}/{categoryNames}")
    Single<List<Item>> query(@Nonnull @NonNull @Path("sourceNames") String sourceNames, @Nonnull @NonNull @Path("categoryNames") String categoryNames);
}
