package com.github.ayltai.newspaper.app.data.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.ayltai.newspaper.rss.Enclosure;
import com.github.ayltai.newspaper.rss.RssItem;
import com.github.ayltai.newspaper.util.RealmLists;
import com.github.ayltai.newspaper.util.TestUtils;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@SuppressWarnings("MethodCount")
public class NewsItem extends RealmObject implements Item, Parcelable {
    //region Constants

    public static final String FIELD_TITLE              = "title";
    public static final String FIELD_DESCRIPTION        = "description";
    public static final String FIELD_SOURCE             = "source";
    public static final String FIELD_CATEGORY           = "category";
    public static final String FIELD_LINK               = "link";
    public static final String FIELD_BOOKMARKED         = "bookmarked";
    public static final String FIELD_LAST_ACCESSED_DATE = "lastAccessedDate";

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        }
    };

    //endregion

    //region Fields

    @PrimaryKey
    private String  link;
    private String  title;
    private String  description;
    private boolean isFullDescription;
    private long    publishDate;
    private String  source;
    private String  category;
    private Video   video;
    private boolean bookmarked;
    private long    lastAccessedDate;

    private RealmList<Image> images = new RealmList<>();

    //endregion

    //region Constructors

    public NewsItem() {
    }

    public NewsItem(@NonNull final RssItem rss, final String source, final String category) {
        this.link        = rss.getLink() == null ? null : rss.getLink().trim();
        this.title       = rss.getTitle().trim();
        this.description = rss.getDescription() == null ? null : rss.getDescription().trim();
        this.source      = source;
        this.category    = category;

        if (rss.getPubDate() != null) {
            try {
                this.publishDate = NewsItem.DATE_FORMAT.get().parse(rss.getPubDate().trim().replaceAll("EDT", "+0800")).getTime();
            } catch (final ParseException e) {
                if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
            }
        }

        final Enclosure enclosure = rss.getEnclosure();

        if (enclosure != null) {
            this.images.add(new Image(enclosure.getUrl()));
        }
    }

    //endregion

    //region Properties

    @Nullable
    public String getTitle() {
        return this.title;
    }

    public void setTitle(@Nullable final String title) {
        this.title = title == null ? null : title.trim();
    }

    @Nullable
    public String getDescription() {
        return this.description;
    }

    public void setDescription(@Nullable final String description) {
        this.description = description == null ? null : description.trim();
    }

    public boolean isFullDescription() {
        return this.isFullDescription;
    }

    public void setIsFullDescription(final boolean isFullDescription) {
        this.isFullDescription = isFullDescription;
    }

    @NonNull
    public String getLink() {
        return this.link;
    }

    public void setLink(@Nullable final String link) {
        this.link = link;
    }

    @Nullable
    public Date getPublishDate() {
        return this.publishDate == 0 ? null : new Date(this.publishDate);
    }

    public void setPublishDate(@Nullable final Date publishDate) {
        this.publishDate = publishDate == null ? 0 : publishDate.getTime();
    }

    @NonNull
    public String getSource() {
        return this.source;
    }

    public void setSource(@NonNull final String source) {
        this.source = source.trim();
    }

    @NonNull
    public String getCategory() {
        return this.category;
    }

    public void setCategory(@NonNull final String category) {
        this.category = category;
    }

    @NonNull
    public RealmList<Image> getImages() {
        return this.images;
    }

    @Nullable
    public Video getVideo() {
        return this.video;
    }

    public void setVideo(@Nullable final Video video) {
        this.video = video;
    }

    public boolean isBookmarked() {
        return this.bookmarked;
    }

    public void setBookmarked(final boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    @Nullable
    public Date getLastAccessedDate() {
        return this.lastAccessedDate == 0 ? null : new Date(this.lastAccessedDate);
    }

    public void setLastAccessedDate(@Nullable final Date lastAccessedDate) {
        this.lastAccessedDate = lastAccessedDate == null ? 0 : lastAccessedDate.getTime();
    }

    //endregion

    @Override
    public final int compareTo(@NonNull final Item item) {
        if (this.link.equals(item.getLink())) return 0;

        if (this.publishDate != 0 && item.getPublishDate() != null) return (int)(item.getPublishDate().getTime() - this.publishDate);

        return item.getTitle() == null ? 1 : this.title.compareTo(item.getTitle());
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) return true;

        if (obj instanceof Item) {
            final Item item = (Item)obj;

            return this.link.equals(item.getLink());
        }

        return false;
    }

    @Override
    public final int hashCode() {
        return this.link.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "Item { link = '" + this.link + "', title = '" + this.title + "', description = '" + this.description + "', isFullDescription = " + this.isFullDescription + ", publishDate = " + this.publishDate + ", source = '" + this.source + "', category = '" + this.category + "', video = " + this.video + ", bookmarked = " + this.bookmarked + ", lastAccessedDate = " + this.lastAccessedDate + ", images = " + RealmLists.toString(this.images) + " }";
    }

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull final Parcel dest, final int flags) {
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeInt(this.isFullDescription ? 1 : 0);
        dest.writeString(this.link);
        dest.writeLong(this.publishDate);
        dest.writeString(this.source);
        dest.writeString(this.category);
        dest.writeTypedList(this.images);
        dest.writeParcelable(this.video, 0);
        dest.writeInt(this.bookmarked ? 1 : 0);
        dest.writeLong(this.lastAccessedDate);
    }

    protected NewsItem(@NonNull final Parcel in) {
        this.title             = in.readString();
        this.description       = in.readString();
        this.isFullDescription = in.readInt() == 1;
        this.link              = in.readString();
        this.publishDate       = in.readLong();
        this.source            = in.readString();
        this.category          = in.readString();

        this.images = new RealmList<>();
        this.images.addAll(in.createTypedArrayList(Image.CREATOR));

        this.video            = in.readParcelable(Video.class.getClassLoader());
        this.bookmarked       = in.readInt() == 1;
        this.lastAccessedDate = in.readLong();
    }

    public static final Parcelable.Creator<NewsItem> CREATOR = new Parcelable.Creator<NewsItem>() {
        @NonNull
        @Override
        public NewsItem createFromParcel(@NonNull final Parcel source) {
            return new NewsItem(source);
        }

        @NonNull
        @Override
        public NewsItem[] newArray(final int size) {
            return new NewsItem[size];
        }
    };

    //endregion
}
