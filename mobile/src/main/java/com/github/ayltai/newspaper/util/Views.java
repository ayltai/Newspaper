package com.github.ayltai.newspaper.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

public final class Views {
    private Views() {
    }

    @Nullable
    public static Activity getActivity(@NonNull final View view) {
        return Views.getActivity(view.getContext());
    }

    @Nullable
    public static Activity getActivity(@NonNull final Context context) {
        if (context instanceof ContextWrapper) {
            if (context instanceof Activity) return (Activity)context;

            return Views.getActivity(((ContextWrapper)context).getBaseContext());
        }

        return null;
    }

    public static ViewGroup.LayoutParams createWrapContentLayoutParams() {
        return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static ViewGroup.LayoutParams createMatchParentLayoutParams() {
        return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }
}
