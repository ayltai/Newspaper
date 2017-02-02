package com.github.ayltai.newspaper.rss;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Item extends RealmObject implements Comparable<Item>, Parcelable {
    //region Constants

    static final String TAG_TITLE         = "title";
    static final String TAG_DESCRIPTION   = "description";
    static final String TAG_LINK          = "link";
    static final String TAG_PUBLISH_DATE  = "pubDate";
    static final String TAG_SOURCE        = "source";
    static final String TAG_MEDIA_CONTENT = "media:content";
    static final String TAG_GUID          = "guid";

    static final String ATTR_URL = "url";

    //endregion

    //region Fields

    String title;

    String description;

    String link;

    long publishDate;

    String source;

    String mediaUrl;

    @PrimaryKey
    String guid;

    //endregion

    public Item() {
    }

    //region Properties

    @Nullable
    public String getTitle() {
        return this.title;
    }

    @Nullable
    public String getDescription() {
        return this.description;
    }

    @Nullable
    public String getLink() {
        return this.link;
    }

    @Nullable
    public Date getPublishDate() {
        if (this.publishDate == 0) return null;

        return new Date(this.publishDate);
    }

    @Nullable
    public String getSource() {
        return this.source;
    }

    @Nullable
    public String getMediaUrl() {
        return this.mediaUrl;
    }

    @Nullable
    public String getGuid() {
        return this.guid;
    }

    //endregion

    @Override
    public final int compareTo(@NonNull final Item item) {
        if (this.publishDate != 0 && item.publishDate != 0) return (int)(item.publishDate - this.publishDate);

        if (this.title != null && item.title != null) return this.title.compareTo(item.title);

        return 0;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) return true;

        if (obj instanceof Item) {
            final Item item = (Item)obj;

            return this.guid == null ? item.guid == null : this.guid.equals(item.guid);
        }

        return false;
    }

    @Override
    public final int hashCode() {
        return this.guid == null ? 0 : this.guid.hashCode();
    }

    //region Parcelable

    @Override
    public final int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(@NonNull final Parcel dest, final int flags) {
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeString(this.link);
        dest.writeLong(this.publishDate);
        dest.writeString(this.source);
        dest.writeString(this.mediaUrl);
        dest.writeString(this.guid);
    }

    protected Item(@NonNull final Parcel in) {
        this.title       = in.readString();
        this.description = in.readString();
        this.link        = in.readString();
        this.publishDate = in.readLong();
        this.source      = in.readString();
        this.mediaUrl    = in.readString();
        this.guid        = in.readString();
    }

    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        @Override
        public Item createFromParcel(@NonNull final Parcel source) {
            return new Item(source);
        }

        @Override
        public Item[] newArray(final int size) {
            return new Item[size];
        }
    };

    //endregion
}
