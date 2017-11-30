package com.github.ayltai.newspaper.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.util.DevUtils;

import retrofit2.HttpException;

public final class NetworkUtils {
    private static final int ERROR_TOO_MANY_REQUESTS = 429;

    private NetworkUtils() {
    }

    public static boolean isOnline(@NonNull final Context context) {
        if (DevUtils.isRunningUnitTest()) return true;

        final ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return manager != null && manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public static boolean shouldRetry(@NonNull final Throwable throwable) {
        return throwable instanceof HttpException && ((HttpException)throwable).code() == NetworkUtils.ERROR_TOO_MANY_REQUESTS;
    }
}
