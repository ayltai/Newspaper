package com.github.ayltai.newspaper.app.data.model;

import java.util.Arrays;
import java.util.Collection;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Category extends RealmObject implements Parcelable {
    private static final String REALTIME = "即時";

    //region Fields

    @PrimaryKey
    private String url;
    private String name;

    //endregion

    @NonNull
    public static String toDisplayName(@NonNull final String name) {
        return name.startsWith(Category.REALTIME) ? name.substring(2) : name;
    }

    @NonNull
    public static Collection<String> fromDisplayName(@NonNull final String name) {
        return Arrays.asList(name, Category.REALTIME + name);
    }

    //region Constructors

    public Category() {
    }

    public Category(@NonNull final String url, @NonNull final String name) {
        this.url  = url;
        this.name = name;
    }

    //endregion

    //region Properties

    public String getUrl() {
        return this.url;
    }

    public String getName() {
        return this.name;
    }

    //endregion

    @NonNull
    @Override
    public String toString() {
        return "Category { url = '" + this.url + "', name = '" + this.name + "' }";
    }

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull final Parcel dest, final int flags) {
        dest.writeString(this.url);
        dest.writeString(this.name);
    }

    protected Category(@NonNull final Parcel in) {
        this.url  = in.readString();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        @NonNull
        @Override
        public Category createFromParcel(@NonNull final Parcel source) {
            return new Category(source);
        }

        @NonNull
        @Override
        public Category[] newArray(final int size) {
            return new Category[size];
        }
    };

    //endregion
}
