package com.github.ayltai.newspaper.data;

import android.support.annotation.NonNull;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Favorite extends RealmObject {
    @PrimaryKey
    private int               id = 0;
    private RealmList<Source> sources;

    public Favorite() {
    }

    public Favorite(@NonNull final RealmList<Source> sources) {
        this.sources = sources;
    }

    @NonNull
    public final RealmList<Source> getSources() {
        return this.sources;
    }

    public final boolean contains(@NonNull final String url) {
        for (final Source source : this.sources) {
            if (url.equals(source.getUrl())) return true;
        }

        return false;
    }
}
