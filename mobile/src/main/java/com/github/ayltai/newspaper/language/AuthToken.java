package com.github.ayltai.newspaper.language;

import java.util.Collection;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class AuthToken {
    //region Constants

    private static final String KEY_ACCESS_TOKEN  = "accessToken";
    private static final String KEY_REFRESH_TOKEN = "refreshToken";
    private static final String KEY_EXPIRY_TIME   = "expiryTime";

    private static final long ONE_HOUR = 60 * 60 * 1000;

    //endregion

    @Nullable
    @SerializedName("access_token")
    public abstract String getAccessToken();

    @Nullable
    @SerializedName("refresh_token")
    public abstract String getRefreshToken();

    @Nullable
    @SerializedName("expiry_date")
    public abstract Long getExpiryTime();

    public boolean isValid() {
        return this.getExpiryTime() != null && System.currentTimeMillis() + AuthToken.ONE_HOUR < this.getExpiryTime();
    }

    @NonNull
    public GoogleCredential toGoogleCredential(@NonNull final Collection<String> scopes) {
        return new GoogleCredential()
            .setAccessToken(this.getAccessToken())
            .setRefreshToken(this.getRefreshToken())
            .setExpirationTimeMilliseconds(this.getExpiryTime())
            .createScoped(scopes);
    }

    public void save(@NonNull final Context context) {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putString(AuthToken.KEY_ACCESS_TOKEN, this.getAccessToken());
        editor.putString(AuthToken.KEY_REFRESH_TOKEN, this.getRefreshToken());
        editor.putLong(AuthToken.KEY_EXPIRY_TIME, this.getExpiryTime() == null ? 0 : this.getExpiryTime());

        editor.apply();
    }

    @NonNull
    public static AuthToken load(@NonNull final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final long              expiryTime  = preferences.getLong(AuthToken.KEY_EXPIRY_TIME, 0);

        return new AutoValue_AuthToken(preferences.getString(AuthToken.KEY_ACCESS_TOKEN, null), preferences.getString(AuthToken.KEY_REFRESH_TOKEN, null), expiryTime == 0 ? null : expiryTime);
    }

    @NonNull
    public static TypeAdapter<AuthToken> createTypeAdapter(@NonNull final Gson gson) {
        return new AutoValue_AuthToken.GsonTypeAdapter(gson);
    }
}
