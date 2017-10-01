package com.github.ayltai.newspaper.app.data.model;

import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.realm.RealmList;

public interface Item extends Cloneable, Comparable<Item> {
    @Nullable
    String getTitle();

    @Nullable
    String getDescription();

    boolean isFullDescription();

    @NonNull
    String getLink();

    @Nullable
    Date getPublishDate();

    @NonNull
    String getSource();

    @NonNull
    String getCategory();

    @NonNull
    RealmList<Image> getImages();

    @Nullable
    Video getVideo();

    boolean isBookmarked();
}
