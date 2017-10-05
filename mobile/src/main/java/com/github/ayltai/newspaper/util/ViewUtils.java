package com.github.ayltai.newspaper.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import io.supercharge.shimmerlayout.ShimmerLayout;

public final class ViewUtils {
    private ViewUtils() {
    }

    @Nullable
    public static Activity getActivity(@NonNull final View view) {
        return ViewUtils.getActivity(view.getContext());
    }

    @Nullable
    public static Activity getActivity(@NonNull final Context context) {
        if (context instanceof ContextWrapper) {
            if (context instanceof Activity) return (Activity)context;

            return ViewUtils.getActivity(((ContextWrapper)context).getBaseContext());
        }

        return null;
    }

    @Nullable
    public static View getRootView(@NonNull final Activity activity) {
        return activity.findViewById(android.R.id.content);
    }

    public static ViewGroup.LayoutParams createWrapContentLayoutParams() {
        return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static ViewGroup.LayoutParams createMatchParentLayoutParams() {
        return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public static void startShimmerAnimation(@NonNull final View view) {
        if (view instanceof ViewGroup) {
            final ViewGroup parent = (ViewGroup)view;

            for (int i = 0; i < parent.getChildCount(); i++) ViewUtils.startShimmerAnimation(parent.getChildAt(i));

            if (view instanceof ShimmerLayout) ((ShimmerLayout)view).startShimmerAnimation();
        }
    }
}
