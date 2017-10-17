package com.github.ayltai.newspaper.net;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class AuthToken {
    @NonNull
    @SerializedName("access_token")
    public abstract String getAccessToken();

    @Nullable
    @SerializedName("refresh_token")
    public abstract String getRefreshToken();

    @Nullable
    @SerializedName("expiry_date")
    public abstract Long getExpiryTime();

    @NonNull
    public static TypeAdapter<AuthToken> createTypeAdapter(@NonNull final Gson gson) {
        return new AutoValue_AuthToken.GsonTypeAdapter(gson);
    }
}
