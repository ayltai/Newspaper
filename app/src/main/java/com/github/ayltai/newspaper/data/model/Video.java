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
public class Video extends RealmObject implements Parcelable {
    @Getter
    private String videoUrl;

    @Getter
    private String imageUrl;

    public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
        @Nonnull
        @NonNull
        @Override
        public Video createFromParcel(@Nonnull @NonNull @lombok.NonNull final Parcel source) {
            return new Video(source);
        }

        @Nonnull
        @NonNull
        @Override
        public Video[] newArray(final int size) {
            return new Video[size];
        }
    };

    public Video() {
    }

    protected Video(@Nonnull @NonNull @lombok.NonNull final Parcel in) {
        this.videoUrl = in.readString();
        this.imageUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@Nonnull @NonNull @lombok.NonNull final Parcel dest, final int flags) {
        dest.writeString(this.videoUrl);
        dest.writeString(this.imageUrl);
    }
}
