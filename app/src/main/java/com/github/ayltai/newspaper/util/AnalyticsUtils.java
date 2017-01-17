package com.github.ayltai.newspaper.util;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;

import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.ShareEvent;
import com.github.ayltai.newspaper.rss.Item;

public final class AnalyticsUtils {
    private AnalyticsUtils() {
    }

    public static ContentViewEvent applyAttributes(@NonNull final ContentViewEvent event, @NonNull final Item item) {
        return event.putContentId(ItemUtils.getId(item))
            .putContentName(item.getTitle())
            .putContentType(item.getClass().getName());
    }

    public static ShareEvent applyAttributes(@NonNull final ShareEvent event, @NonNull final Item item) {
        return event.putContentId(ItemUtils.getId(item))
            .putContentName(item.getTitle())
            .putContentType(item.getClass().getName());
    }

    public static CustomEvent applyAttributes(@NonNull final CustomEvent event, @NonNull final Item item) {
        return event.putCustomAttribute("contentId", ItemUtils.getId(item))
            .putCustomAttribute("contentName", item.getTitle())
            .putCustomAttribute("contentType", item.getClass().getName());
    }

    public static Bundle createBundle(@NonNull final Item item) {
        final Bundle bundle = new Bundle();

        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, ItemUtils.getId(item));
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, item.getTitle());
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, item.getClass().getName());

        return bundle;
    }
}
