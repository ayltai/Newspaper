package com.github.ayltai.newspaper.data.model;

import javax.annotation.Nonnull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import io.realm.RealmObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class Image extends RealmObject implements Parcelable {
    @Getter
    private String imageUrl;

    @Getter
    private String description;

    public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {
        @Nonnull
        @NonNull
        @Override
        public Image createFromParcel(@Nonnull @NonNull @lombok.NonNull final Parcel source) {
            return new Image(source);
        }

        @Nonnull
        @NonNull
        @Override
        public Image[] newArray(final int size) {
            return new Image[size];
        }
    };

    public Image() {
    }

    protected Image(@Nonnull @NonNull @lombok.NonNull final Parcel in) {
        this.imageUrl = in.readString();
        this.description = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@Nonnull @NonNull @lombok.NonNull final Parcel dest, final int flags) {
        dest.writeString(this.imageUrl);
        dest.writeString(this.description);
    }
}
