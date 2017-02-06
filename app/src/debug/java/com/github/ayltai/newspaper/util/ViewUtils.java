package com.github.ayltai.newspaper.util;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewParent;

public final class ViewUtils {
    private ViewUtils() {
    }

    @NonNull
    public static View getRootView(@NonNull final View view) {
        final ViewParent parent = view.getParent();

        if (parent == null) return view;

        if (parent instanceof View) return ViewUtils.getRootView((View)view.getParent());

        return view;
    }
}
