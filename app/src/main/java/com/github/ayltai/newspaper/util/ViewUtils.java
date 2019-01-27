package com.github.ayltai.newspaper.util;

import javax.annotation.Nonnull;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ViewUtils {
    @Nullable
    public Activity getActivity(@Nonnull @NonNull @lombok.NonNull final View view) {
        return ViewUtils.getActivity(view.getContext());
    }

    @Nullable
    public Activity getActivity(@Nonnull @NonNull @lombok.NonNull final Context context) {
        if (context instanceof ContextWrapper) {
            if (context instanceof Activity) return (Activity)context;

            return ViewUtils.getActivity(((ContextWrapper)context).getBaseContext());
        }

        return null;
    }
}
