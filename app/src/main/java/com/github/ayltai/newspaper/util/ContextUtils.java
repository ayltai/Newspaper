package com.github.ayltai.newspaper.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.setting.Settings;

public final class ContextUtils {
    private ContextUtils() {
    }

    @Nullable
    public static Activity getActivity(@NonNull final Context context) {
        if (context instanceof Activity) return (Activity)context;
        if (context instanceof ContextWrapper) return getActivity(((ContextWrapper)context).getBaseContext());

        return null;
    }

    public static int getColor(@NonNull final Context context, @AttrRes final int attr) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);

        return value.data;
    }

    public static int getResourceId(@NonNull final Context context, @AttrRes final int attr) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);

        return value.resourceId;
    }

    public static void setTheme(@NonNull final Context context) {
        context.setTheme(Settings.isDarkTheme(context) ? R.style.AppTheme_Dark : R.style.AppTheme_Light);
    }
}
