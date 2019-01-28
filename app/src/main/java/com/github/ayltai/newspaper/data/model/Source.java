package com.github.ayltai.newspaper.data.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import lombok.Getter;
import lombok.ToString;

@ToString
public class Source extends RealmObject implements Comparable<Source>, Parcelable {
    public static final String FIELD_NAME = "name";

    public static final List<String> DEFAULT_SOURCES = Arrays.asList("蘋果日報", "東方日報", "星島日報", "星島即時", "經濟日報", "成報", "明報", "頭條日報", "頭條即時", "晴報", "信報", "香港電台", "南華早報", "英文虎報", "文匯報");

    private static final Map<String, Integer> SOURCES       = new HashMap<>();
    private static final Map<String, String>  DISPLAY_NAMES = new HashMap<>();

    static {
        int i = 0;

        Source.SOURCES.put("蘋果", i++);
        Source.SOURCES.put("東方", i++);
        Source.SOURCES.put("星島", i++);
        Source.SOURCES.put("經濟", i++);
        Source.SOURCES.put("成報", i++);
        Source.SOURCES.put("明報", i++);
        Source.SOURCES.put("頭條", i++);
        Source.SOURCES.put("晴報", i++);
        Source.SOURCES.put("信報", i++);
        Source.SOURCES.put("香港", i++);
        Source.SOURCES.put("南華", i++);
        Source.SOURCES.put("英文", i++);
        Source.SOURCES.put("文匯", i);

        Source.DISPLAY_NAMES.put("蘋果日報", "蘋果日報");
        Source.DISPLAY_NAMES.put("東方日報", "東方日報");
        Source.DISPLAY_NAMES.put("星島日報", "星島日報");
        Source.DISPLAY_NAMES.put("星島即時", "星島日報");
        Source.DISPLAY_NAMES.put("經濟日報", "經濟日報");
        Source.DISPLAY_NAMES.put("成報", "成報");
        Source.DISPLAY_NAMES.put("明報", "明報");
        Source.DISPLAY_NAMES.put("頭條日報", "頭條日報");
        Source.DISPLAY_NAMES.put("頭條即時", "頭條日報");
        Source.DISPLAY_NAMES.put("晴報", "晴報");
        Source.DISPLAY_NAMES.put("信報", "信報");
        Source.DISPLAY_NAMES.put("香港電台", "香港電台");
        Source.DISPLAY_NAMES.put("南華早報", "南華早報");
        Source.DISPLAY_NAMES.put("英文虎報", "英文虎報");
        Source.DISPLAY_NAMES.put("文匯報", "文匯報");
    }

    @Getter
    @PrimaryKey
    private String name;

    @Getter
    private String contextUrl;

    @Getter
    private String imageUrl;

    @Getter
    private RealmList<Category> categories;

    @Nonnull
    @NonNull
    public String getDisplayName() {
        return Source.DISPLAY_NAMES.get(this.name);
    }

    @Override
    public int compareTo(final Source source) {
        if (source == null) throw new NullPointerException();

        return Source.SOURCES.get(this.name.substring(0, 2)) - Source.SOURCES.get(source.name.substring(0, 2));
    }

    @Override
    public boolean equals(final Object source) {
        if (this == source) return true;
        if (source == null || getClass() != source.getClass()) return false;

        return this.name.substring(0, 2).equals(((Source)source).name.substring(0, 2));
    }

    @Override
    public int hashCode() {
        return this.name.substring(0, 2).hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@Nonnull @NonNull @lombok.NonNull final Parcel dest, final int flags) {
        dest.writeString(this.name);
        dest.writeString(this.contextUrl);
        dest.writeString(this.imageUrl);
        dest.writeTypedList(this.categories);
    }

    public Source() {
    }

    protected Source(Parcel in) {
        this.name       = in.readString();
        this.contextUrl = in.readString();
        this.imageUrl   = in.readString();
        this.categories = new RealmList<>();

        this.categories.addAll(in.createTypedArrayList(Category.CREATOR));
    }

    public static final Creator<Source> CREATOR = new Creator<Source>() {
        @Nonnull
        @NonNull
        @Override
        public Source createFromParcel(@Nonnull @NonNull @lombok.NonNull final Parcel source) {
            return new Source(source);
        }

        @Nonnull
        @NonNull
        @Override
        public Source[] newArray(final int size) {
            return new Source[size];
        }
    };
}
