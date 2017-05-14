package com.github.ayltai.newspaper.model;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Source extends RealmObject implements Parcelable {
    public static final String FIELD_NAME = "name";

    //region Fields

    @PrimaryKey
    private String              name;
    private RealmList<Category> categories;

    //endregion

    //region Constructors

    public Source() {
    }

    public Source(@NonNull final String name, @NonNull final RealmList<Category> categories) {
        this.name       = name;
        this.categories = categories;
    }

    //endregion

    //region Properties

    public String getName() {
        return name;
    }

    public RealmList<Category> getCategories() {
        return categories;
    }

    //endregion

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

        final List<Category> categories = in.createTypedArrayList(Category.CREATOR);
        for (final Category category : categories) this.categories.add(category);
    }

    public static final Creator<Source> CREATOR = new Creator<Source>() {
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
