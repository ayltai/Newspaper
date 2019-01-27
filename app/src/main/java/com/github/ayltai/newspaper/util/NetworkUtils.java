package com.github.ayltai.newspaper.util;

import javax.annotation.Nonnull;

import android.content.Context;
import android.net.ConnectivityManager;

import androidx.annotation.NonNull;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NetworkUtils {
    public boolean isOnline(@Nonnull @NonNull @lombok.NonNull final Context context) {
        if (DevUtils.isRunningUnitTest()) return true;

        final ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager != null && manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
