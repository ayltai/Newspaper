package com.github.ayltai.newspaper.data;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import com.github.ayltai.newspaper.app.data.model.NewsItem;

import io.reactivex.Flowable;
import junit.framework.Assert;

public final class RealmLoaderTest extends DataTest {
    @Test
    public void testIsValid() {
        final RealmLoader<NewsItem> loader = new RealmLoader<NewsItem>(RuntimeEnvironment.application, null) {
            @NonNull
            @Override
            protected Flowable<NewsItem> loadFromLocalSource(@NonNull final Context context, @Nullable final Bundle args) {
                return null;
            }

            @NonNull
            @Override
            protected Flowable<NewsItem> loadFromRemoteSource(@NonNull final Context context, @Nullable final Bundle args) {
                return null;
            }
        };

        loader.onForceLoad();

        Assert.assertTrue(loader.isValid());

        Assert.assertFalse(new RealmLoader<NewsItem>(RuntimeEnvironment.application, null) {
            @NonNull
            @Override
            protected Flowable<NewsItem> loadFromLocalSource(@NonNull final Context context, @Nullable final Bundle args) {
                return null;
            }

            @NonNull
            @Override
            protected Flowable<NewsItem> loadFromRemoteSource(@NonNull final Context context, @Nullable final Bundle args) {
                return null;
            }
        }.isValid());
    }
}
