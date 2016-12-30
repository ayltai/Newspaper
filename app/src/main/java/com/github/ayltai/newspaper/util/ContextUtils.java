package com.github.ayltai.newspaper.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class ContextUtils {
    private ContextUtils() {
    }

    @Nullable
    public static Activity getActivity(@NonNull final Context context) {
        if (context instanceof Activity) return (Activity)context;
        if (context instanceof ContextWrapper) return getActivity(((ContextWrapper)context).getBaseContext());

        return null;
    }
}
