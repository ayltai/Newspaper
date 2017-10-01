package com.github.ayltai.newspaper.app.data.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Source extends RealmObject implements Parcelable {
    //region Constants

    private static final String SING_TAO_DAILY    = "星島日報";
    private static final String SING_TAO_REALTIME = "星島即時";
    private static final String HEADLINE_DAILY    = "頭條日報";
    private static final String HEADLINE_REALTIME = "頭條即時";

    //endregion

    //region Fields

    @PrimaryKey
    private String              name;
    private RealmList<Category> categories;
    @DrawableRes
    @Ignore
    private int avatar;

    //endregion

    @NonNull
    public static String toDisplayName(@NonNull final String name) {
        if (Source.SING_TAO_REALTIME.equals(name)) return Source.SING_TAO_DAILY;
        if (Source.HEADLINE_REALTIME.equals(name)) return Source.HEADLINE_DAILY;

        return name;
    }

    @NonNull
    public static Collection<String> fromDisplayName(@NonNull final String name) {
        if (name.equals(Source.SING_TAO_DAILY)) return Arrays.asList(name, Source.SING_TAO_REALTIME);
        if (name.equals(Source.HEADLINE_DAILY)) return Arrays.asList(name, Source.HEADLINE_REALTIME);

        return Collections.singletonList(name);
    }

    //region Constructors

    public Source() {
    }

    public Source(@NonNull final String name, @NonNull final RealmList<Category> categories, @DrawableRes final int avatar) {
        this.name       = name;
        this.categories = categories;
        this.avatar     = avatar;
    }

    //endregion

    //region Properties

    @NonNull
    public String getName() {
        return this.name;
    }

    @NonNull
    public RealmList<Category> getCategories() {
        return this.categories;
    }

    @DrawableRes
    public int getAvatar() {
        return avatar;
    }

    //endregion

    @NonNull
    @Override
    public String toString() {
        return "Source { name = '" + name + "', categories = " + categories + " }";
    }

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull final Parcel dest, final int flags) {
        dest.writeString(this.name);
        dest.writeTypedList(this.categories);
        dest.writeInt(this.avatar);
    }

    protected Source(@NonNull final Parcel in) {
        this.name       = in.readString();
        this.categories = new RealmList<>();

        for (final Category category : in.createTypedArrayList(Category.CREATOR)) this.categories.add(category);

        this.avatar = in.readInt();
    }

    public static final Parcelable.Creator<Source> CREATOR = new Parcelable.Creator<Source>() {
        @NonNull
        @Override
        public Source createFromParcel(@NonNull final Parcel source) {
            return new Source(source);
        }

        @NonNull
        @Override
        public Source[] newArray(final int size) {
            return new Source[size];
        }
    };

    //endregion
}
