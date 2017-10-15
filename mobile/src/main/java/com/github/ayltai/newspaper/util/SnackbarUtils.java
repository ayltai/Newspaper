package com.github.ayltai.newspaper.util;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.github.ayltai.newspaper.R;

public final class SnackbarUtils {
    private SnackbarUtils() {
    }

    public static void show(@NonNull final View view, @StringRes final int message, final int duration) {
        final Snackbar snackbar = Snackbar.make(view, message, duration);

        ((TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setTextColor(ContextUtils.getColor(view.getContext(), R.attr.textColorInverse));

        snackbar.show();
    }
}
