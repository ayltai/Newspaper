package com.github.ayltai.newspaper.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;

public final class NetworkUtils {
    private NetworkUtils() {
    }

    public static boolean isOnline(@NonNull final Context context) {
        final ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return manager != null && manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
