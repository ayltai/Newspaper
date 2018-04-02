package com.github.ayltai.newspaper.data;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import com.github.ayltai.newspaper.app.data.model.Item;

import edu.emory.mathcs.backport.java.util.Collections;
import io.reactivex.Flowable;
import junit.framework.Assert;

public final class RealmLoaderTest extends DataTest {
    @Test
    public void testIsValid() {
        final RealmLoader<Item> loader = new RealmLoader<Item>(RuntimeEnvironment.application, null) {
            @SuppressWarnings("unchecked")
            @NonNull
            @Override
            protected Flowable<List<Item>> loadFromLocalSource(@NonNull final Context context, @Nullable final Bundle args) {
                return Flowable.just(Collections.emptyList());
            }

            @SuppressWarnings("unchecked")
            @NonNull
            @Override
            protected Flowable<List<Item>> loadFromRemoteSource(@NonNull final Context context, @Nullable final Bundle args) {
                return Flowable.just(Collections.emptyList());
            }
        };

        loader.onForceLoad();

        Assert.assertTrue(loader.isValid());

        Assert.assertFalse(new RealmLoader<Item>(RuntimeEnvironment.application, null) {
            @SuppressWarnings("unchecked")
            @NonNull
            @Override
            protected Flowable<List<Item>> loadFromLocalSource(@NonNull final Context context, @Nullable final Bundle args) {
                return Flowable.just(Collections.emptyList());
            }

            @SuppressWarnings("unchecked")
            @NonNull
            @Override
            protected Flowable<List<Item>> loadFromRemoteSource(@NonNull final Context context, @Nullable final Bundle args) {
                return Flowable.just(Collections.emptyList());
            }
        }.isValid());
    }
}
