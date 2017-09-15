package com.github.ayltai.newspaper.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Source extends RealmObject implements Parcelable {
    //region Fields

    @PrimaryKey
    private String              name;
    private RealmList<Category> categories;

    //endregion

    @NonNull
    public static String toDisplayName(@NonNull final String name) {
        if ("星島即時".equals(name)) return "星島日報";
        if ("頭條即時".equals(name)) return "頭條日報";

        return name;
    }

    //region Constructors

    public Source() {
    }

    public Source(@NonNull final String name, @NonNull final RealmList<Category> categories) {
        this.name       = name;
        this.categories = categories;
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
    }

    protected Source(@NonNull final Parcel in) {
        this.name       = in.readString();
        this.categories = new RealmList<>();

        for (final Category category : in.createTypedArrayList(Category.CREATOR)) this.categories.add(category);
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
