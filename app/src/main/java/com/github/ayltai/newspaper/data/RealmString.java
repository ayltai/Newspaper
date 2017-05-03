package com.github.ayltai.newspaper.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.realm.RealmObject;

public class RealmString extends RealmObject implements Parcelable {
    String value;

    public RealmString() {
    }

    public RealmString(@Nullable final String value) {
        this.value = value;
    }

    @Nullable
    public String getValue() {
        return this.value;
    }

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull final Parcel dest, final int flags) {
        dest.writeString(this.value);
    }

    protected RealmString(@NonNull final Parcel in) {
        this.value = in.readString();
    }

    public static final Parcelable.Creator<RealmString> CREATOR = new Parcelable.Creator<RealmString>() {
        @NonNull
        @Override
        public RealmString createFromParcel(@NonNull final Parcel source) {
            return new RealmString(source);
        }

        @NonNull
        @Override
        public RealmString[] newArray(final int size) {
            return new RealmString[size];
        }
    };

    //endregion
}
