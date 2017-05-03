package com.github.ayltai.newspaper.data;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.model.Source;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Favorite extends RealmObject {
    //region Fields

    @PrimaryKey
    private int               id = 0;
    private RealmList<Source> sources;

    //endregion

    //region Constructors

    public Favorite() {
    }

    public Favorite(@NonNull final RealmList<Source> sources) {
        this.sources = sources;
    }

    //endregion

    @NonNull
    public final RealmList<Source> getSources() {
        return this.sources;
    }

    public final boolean contains(@NonNull final String name) {
        for (final Source source : this.sources) {
            if (name.equals(source.getName())) return true;
        }

        return false;
    }
}
