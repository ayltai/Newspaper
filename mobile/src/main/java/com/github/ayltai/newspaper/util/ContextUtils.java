package com.github.ayltai.newspaper.util;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.util.TypedValue;

public final class ContextUtils {
    private ContextUtils() {
    }

    public static int getDimensionPixelSize(@NonNull final Context context, @AttrRes final int attribute) {
        final TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attribute, typedValue, true);

        return context.getResources().getDimensionPixelSize(typedValue.resourceId);
    }
}
