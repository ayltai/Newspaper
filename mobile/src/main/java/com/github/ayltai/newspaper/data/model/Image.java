package com.github.ayltai.newspaper.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.realm.RealmObject;

public class Image extends RealmObject implements Parcelable {
    //region Fields

    private String url;
    private String description;

    //endregion

    //region Constructors

    public Image() {
    }

    public Image(@NonNull final String url) {
        this.url = url;
    }

    public Image(@NonNull final String url, @Nullable final String description) {
        this.url         = url;
        this.description = description;
    }

    //endregion

    //region Properties

    @NonNull
    public String getUrl() {
        return this.url;
    }

    @Nullable
    public String getDescription() {
        return this.description;
    }

    //endregion

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        final Image image = (Image)o;

        return this.url.equals(image.url);

    }

    @Override
    public int hashCode() {
        return this.url.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "Image { url = '" + this.url + "', description = '" + this.description + "'}";
    }

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull final Parcel dest, final int flags) {
        dest.writeString(this.url);
        dest.writeString(this.description);
    }

    protected Image(@NonNull final Parcel in) {
        this.url         = in.readString();
        this.description = in.readString();
    }

    public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {
        @NonNull
        @Override
        public Image createFromParcel(@NonNull final Parcel source) {
            return new Image(source);
        }

        @NonNull
        @Override
        public Image[] newArray(final int size) {
            return new Image[size];
        }
    };

    //endregion
}
