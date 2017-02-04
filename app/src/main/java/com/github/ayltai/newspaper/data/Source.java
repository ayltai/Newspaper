package com.github.ayltai.newspaper.data;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Source extends RealmObject {
    //region Fields

    @PrimaryKey
    private String url;
    private String name;

    //endregion

    //region Constructors

    public Source() {
    }

    public Source(@NonNull final String url, @NonNull final String name) {
        this.url  = url;
        this.name = name;
    }

    //endregion

    //region Properties

    @NonNull
    public /* final */ String getUrl() {
        return this.url;
    }

    @NonNull
    public /* final */ String getName() {
        return this.name;
    }

    public final void setName(@NonNull final String name) {
        this.name = name;
    }

    //endregion
}
