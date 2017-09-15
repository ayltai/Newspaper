package com.github.ayltai.newspaper.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Category extends RealmObject implements Parcelable {
    //region Fields

    @PrimaryKey
    private String url;
    private String name;

    //endregion

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
        return "Category { url = '" + url + "', name = '" + name + "' }";
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
