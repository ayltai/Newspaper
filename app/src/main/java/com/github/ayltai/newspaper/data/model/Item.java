package com.github.ayltai.newspaper.data.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class Item extends RealmObject implements Parcelable {
    //region Constants

    public static final String FIELD_TITLE        = "title";
    public static final String FIELD_DESCRIPTION  = "description";
    public static final String FIELD_URL          = "url";
    public static final String FIELD_SOURCE       = "source.name";
    public static final String FIELD_CATEGORY     = "category.name";
    public static final String FIELD_PUBLISH_DATE = "publishDate";

    //endregion

    @Getter
    private String title;

    @Getter
    private String description;

    @Getter
    @PrimaryKey
    private String url;

    @Getter
    private Date publishDate;

    @Getter
    private Source source;

    @Getter
    private Category category;

    @Getter
    private RealmList<Image> images;

    @Getter
    private RealmList<Video> videos;

    @Getter
    @Setter
    private boolean isRead;

    @Getter
    @Setter
    private boolean isBookmarked;

    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        @Nonnull
        @NonNull
        @Override
        public Item createFromParcel(@Nonnull @NonNull @lombok.NonNull final Parcel source) {
            return new Item(source);
        }

        @Nonnull
        @NonNull
        @Override
        public Item[] newArray(final int size) {
            return new Item[size];
        }
    };

    public Item() {
    }

    protected Item(@Nonnull @NonNull @lombok.NonNull final Parcel in) {
        this.title       = in.readString();
        this.description = in.readString();
        this.url         = in.readString();

        final long publishDate = in.readLong();
        this.publishDate = publishDate == -1 ? null : new Date(publishDate);

        this.source   = in.readParcelable(Source.class.getClassLoader());
        this.category = in.readParcelable(Category.class.getClassLoader());

        final List<Image> images = new ArrayList<>();
        in.readList(images, Image.class.getClassLoader());
        this.images = new RealmList<>();
        this.images.addAll(images);

        final List<Video> videos = new ArrayList<>();
        in.readList(videos, Video.class.getClassLoader());
        this.videos = new RealmList<>();
        this.videos.addAll(videos);

        this.isRead       = in.readByte() != 0;
        this.isBookmarked = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@Nonnull @NonNull @lombok.NonNull final Parcel dest, final int flags) {
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeString(this.url);
        dest.writeLong(this.publishDate != null ? this.publishDate.getTime() : -1);
        dest.writeParcelable(this.source, flags);
        dest.writeParcelable(this.category, flags);
        dest.writeList(this.images);
        dest.writeList(this.videos);
        dest.writeByte(this.isRead ? (byte)1 : (byte)0);
        dest.writeByte(this.isBookmarked ? (byte)1 : (byte)0);
    }
}
